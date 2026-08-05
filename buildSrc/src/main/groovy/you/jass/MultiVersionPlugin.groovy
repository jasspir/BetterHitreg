package you.jass

import groovy.io.FileType
import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.Internal

class MultiVersionPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        File generated = project.layout.buildDirectory
                .dir("generated-sources/multiversion")
                .get().asFile

        def processTask = project.tasks.register(
                "multiVersion",
                ProcessMultiVersionTask
        ) {
            group = "build"
            description = "Dynamically switch marked version code for a desired version"
            outputDir = generated
            sourceDirs = [
                    project.file("src/main/java"),
                    project.file("src/main/kotlin")
            ]
        }

        project.plugins.withId("idea") {
            project.idea.module {
                generatedSourceDirs += generated
                excludeDirs += generated
            }
        }

        project.plugins.withId("java") {
            def sourceSets = project.extensions.getByType(SourceSetContainer)

            sourceSets.named("main") {
                java.setSrcDirs([generated])
            }

            project.tasks.named("compileJava") {
                dependsOn(processTask)
            }
        }

        project.plugins.withId("org.jetbrains.kotlin.jvm") {
            def sourceSets = project.extensions.getByType(SourceSetContainer)

            sourceSets.named("main") {
                java.srcDir(generated)
            }

            project.tasks.named("compileKotlin") {
                dependsOn(processTask)
            }
        }
    }
}

class ProcessMultiVersionTask extends DefaultTask {
    private List<File> sourceDirs = []

    void setSourceDirs(List<File> dirs) {
        this.sourceDirs = dirs ?: []
    }

    @InputFiles
    List<File> getSourceDirs() {
        return sourceDirs
    }

    @OutputDirectory
    File outputDir

    @Input
    String getMinecraftVersionFromFile() {
        File gp = new File(project.rootDir, "gradle.properties")
        if (!gp.exists()) return null
        gp.readLines()
                .findAll { it?.trim() && !it.trim().startsWith("#") }
                .collectEntries { line ->
                    int idx = line.indexOf('=')
                    if (idx <= 0) return null
                    [(line.substring(0, idx).trim()): line.substring(idx + 1).trim()]
                }["minecraft_version"]
    }

    @TaskAction
    void process() {
        String targetVersionStr = getMinecraftVersionFromFile()
        logger.lifecycle("MultiVersion: reading minecraft_version from gradle.properties = ${targetVersionStr ?: '(none)'}")

        if (outputDir.exists()) outputDir.deleteDir()
        outputDir.mkdirs()

        List<File> files = []
        sourceDirs.each { dir ->
            if (!dir.exists()) return
            dir.eachFileRecurse(FileType.FILES) { f ->
                if (f.name.endsWith(".java") || f.name.endsWith(".kt")) files << f
            }
        }

        if (!targetVersionStr) {
            logger.lifecycle("MultiVersion: no target version found; leaving files unchanged.")
        }

        List<File> processedFiles = []
        files.each { File src ->
            File base = sourceDirs.find { src.absolutePath.startsWith(it.absolutePath) }
            String rel = base ? src.absolutePath.substring(base.absolutePath.length()).replaceFirst("^" + java.util.regex.Pattern.quote(File.separator), "") : src.name
            File dest = new File(outputDir, rel)
            dest.parentFile.mkdirs()

            String content = src.getText("UTF-8")
            String processed = ProcessMultiVersionTask.processTextForTarget(content, targetVersionStr)

            if (processed != content) {
                dest.write(processed, "UTF-8")
                processedFiles << dest
                logger.lifecycle("MultiVersion: patched ${src.path} -> ${dest.path}")
            } else {
                dest.write(content, "UTF-8")
                logger.debug("MultiVersion: no changes for ${src.path}")
            }
        }

        logger.lifecycle("MultiVersion: processed ${files.size()} files, modified ${processedFiles.size()} files; output -> ${outputDir}")
    }

    private static List<Integer> parseVersion(String ver) {
        if (!ver) return []
        ver.split(/\./).collect { try { it.toInteger() } catch(Exception e) { 0 } }
    }

    private static int compareVersion(List<Integer> a, List<Integer> b) {
        int n = Math.max(a.size(), b.size())
        for (int i = 0; i < n; i++) {
            int ai = (i < a.size()) ? a[i] : 0
            int bi = (i < b.size()) ? b[i] : 0
            if (ai < bi) return -1
            if (ai > bi) return 1
        }
        return 0
    }

    private static List<Map> parseMarkers(String expr) {
        if (!expr) return []
        def parts = expr.split(/[&,]/).collect { it?.trim() }.findAll { it }
        def markers = []
        parts.each { p ->
            if (!p) return

            def mRange = (p =~ /^(\d+(?:\.\d+)*?)\s*-\s*(\d+(?:\.\d+)*)$/)

            if (mRange.find()) {
                markers << [type: 'range', start: parseVersion(mRange.group(1)), end: parseVersion(mRange.group(2))]
                return
            }

            def mTrailing = (p =~ /^(\d+(?:\.\d+)*?)([+\-])$/)

            if (mTrailing.find()) {
                def v = parseVersion(mTrailing.group(1))
                def sig = mTrailing.group(2)

                if (sig == '+') {
                    markers << [type: 'atLeast', start: v]
                } else {
                    markers << [type: 'atMost', start: v]
                }

                return
            }

            def mPlain = (p =~ /^(\d+(?:\.\d+)*)$/)

            if (mPlain.find()) {
                markers << [type: 'exact', start: parseVersion(mPlain.group(1))]
                return
            }

            def digits = (p =~ /(\d+(?:\.\d+)*)/)

            if (digits.find()) {
                markers << [type: 'exact', start: parseVersion(digits.group(1))]
            }
        }
        return markers
    }

    private static boolean inRange(List<Integer> target, List<Map> markers) {
        if (!target || !markers) return false

        for (m in markers) {
            switch (m.type) {
                case 'range':
                    if (compareVersion(target, m.start) >= 0 && compareVersion(target, m.end) <= 0) return true
                    break
                case 'atLeast':
                    if (compareVersion(target, m.start) >= 0) return true
                    break
                case 'atMost':
                    if (compareVersion(target, m.start) <= 0) return true
                    break
                case 'exact':
                default:
                    if (compareVersion(target, m.start) == 0) return true
            }
        }

        return false
    }

    private static String processTextForTarget(String inputText, String targetVersionStr) {
        List<Integer> target = targetVersionStr ? parseVersion(targetVersionStr) : null
        def lines = inputText.split('\n', -1)

        def versionPattern = ~/^\s*\/\/\s*(?i:version)\s*(.+)$/
        def indentPattern = ~/^([ \t]*)/

        def blocks = []
        def out = []
        int braceDepth = 0

        lines.each { line ->
            def indentMatcher = (line =~ indentPattern)
            def indent = ""
            if (indentMatcher.find()) indent = indentMatcher.group(1)
            def remainder = line.substring(indent.length())

            def versionMatcher = (remainder =~ versionPattern)
            if (versionMatcher.find()) {
                def expr = versionMatcher.group(1)?.trim()
                def markers = parseMarkers(expr)
                def use = target ? inRange(target, markers) : true
                blocks << [markers: markers, indentlen: indent.length(), braceDepthAtStart: braceDepth, started: false, use: use]
                out << line
                return
            }

            if (blocks.isEmpty()) {
                def codeForCounting = remainder
                if (remainder.startsWith("//")) {
                    codeForCounting = ""
                } else {
                    int idxC = remainder.indexOf("//")
                    if (idxC >= 0) codeForCounting = remainder.substring(0, idxC)
                }

                def openCount = (codeForCounting =~ /\{/).size()
                def closeCount = (codeForCounting =~ /\}/).size()
                braceDepth += openCount - closeCount
                out << line
                return
            }

            def current = blocks[-1]

            def isBlank = line.trim().isEmpty()
            def isVersionLine = (remainder =~ versionPattern).find()

            if ((isBlank || isVersionLine) && indent.length() <= current.indentlen) {
                blocks.remove(blocks.size() - 1)
                def codeForCounting = remainder
                if (remainder.startsWith("//")) {
                    codeForCounting = ""
                } else {
                    int idxC = remainder.indexOf("//")
                    if (idxC >= 0) codeForCounting = remainder.substring(0, idxC)
                }

                def openCount = (codeForCounting =~ /\{/).size()
                def closeCount = (codeForCounting =~ /\}/).size()
                braceDepth += openCount - closeCount
                out << line
                return
            }

            int effectiveIndentLen = indent.length()
            if (remainder.startsWith("//")) {
                def m = (remainder =~ /^\/\/(\s*)/)
                if (m.find()) {
                    effectiveIndentLen = indent.length() + m.group(1).length()
                }
            } else {
                effectiveIndentLen = indent.length()
            }

            boolean eligibleByIndent = effectiveIndentLen >= current.indentlen

            if (!target || !eligibleByIndent) {
                def codeForCounting = remainder
                if (remainder.startsWith("//")) {
                    codeForCounting = ""
                } else {
                    int idxC = remainder.indexOf("//")
                    if (idxC >= 0) codeForCounting = remainder.substring(0, idxC)
                }

                def openCount = (codeForCounting =~ /\{/).size()
                def closeCount = (codeForCounting =~ /\}/).size()
                braceDepth += openCount - closeCount

                out << line

                while (!blocks.isEmpty()) {
                    def top = blocks[-1]
                    if (!top.started && openCount > 0) top.started = true
                    if (top.started && braceDepth == top.braceDepthAtStart) {
                        blocks.remove(blocks.size() - 1)
                    } else {
                        break
                    }
                }
                return
            }

            def use = current.use

            String afterSlashes = null
            if (remainder.startsWith("//")) {
                def m2 = (remainder =~ /^\/\/(\s*)(.*)$/)
                if (m2.find()) {
                    afterSlashes = m2.group(2)
                } else {
                    afterSlashes = remainder.substring(2)
                }
            }

            String outLine
            String codeForCountingForThisLine = ""

            if (use) {
                if (remainder.startsWith("//")) {
                    String codeAfter = afterSlashes.replaceFirst(/^\s*/, "")
                    String pad = " " * current.indentlen
                    outLine = (pad + codeAfter)
                    codeForCountingForThisLine = codeAfter
                } else {
                    outLine = line
                    if (remainder.startsWith("//")) {
                        codeForCountingForThisLine = ""
                    } else {
                        int idxC = remainder.indexOf("//")
                        codeForCountingForThisLine = idxC >= 0 ? remainder.substring(0, idxC) : remainder
                    }
                }
            } else {
                String codePart
                if (remainder.startsWith("//")) {
                    codePart = afterSlashes.replaceFirst(/^\s*/, "")
                } else {
                    codePart = remainder.replaceFirst(/^\s*/, "")
                }

                String pad = " " * current.indentlen
                if (codePart.length() == 0) {
                    outLine = (pad + "//")
                } else {
                    outLine = (pad + "// " + codePart)
                }

                codeForCountingForThisLine = ""
            }

            def openCountLine = (codeForCountingForThisLine =~ /\{/).size()
            def closeCountLine = (codeForCountingForThisLine =~ /\}/).size()
            braceDepth += openCountLine - closeCountLine

            if (!current.started && openCountLine > 0) current.started = true

            out << outLine

            while (!blocks.isEmpty()) {
                def top = blocks[-1]
                if (top.started && braceDepth == top.braceDepthAtStart) {
                    blocks.remove(blocks.size() - 1)
                } else {
                    break
                }
            }
        }

        return out.join('\n')
    }
}

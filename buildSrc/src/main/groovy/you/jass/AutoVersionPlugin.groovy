package you.jass

import org.gradle.api.Plugin
import org.gradle.api.Project
import groovy.json.JsonSlurper
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class AutoVersionPlugin implements Plugin<Project> {
    void apply(Project project) {
        project.extensions.create('autoversion', AutoVersionExtension)

        def autoTask = project.tasks.register('autoVersion') { task ->
            task.group = 'build'
            task.description = 'Dynamically update gradle properties for a desired version'

            task.doLast {
                def ext = project.extensions.autoversion as AutoVersionExtension
                def targetMc = null
                if (ext.minecraftVersion) {
                    targetMc = ext.minecraftVersion.toString()
                    if (targetMc.toLowerCase() == 'auto') {
                        if (project.hasProperty('targetVersion')) {
                            targetMc = project.property('targetVersion').toString()
                        } else {
                            targetMc = null
                        }
                    }
                } else if (project.hasProperty('targetVersion')) {
                    targetMc = project.property('targetVersion').toString()
                }

                boolean needFabricMeta = (ext.yarnMappings?.toString()?.toLowerCase() == 'auto') || (ext.loaderVersion?.toString()?.toLowerCase() == 'auto')
                boolean needFabricApi = (ext.fabricVersion?.toString()?.toLowerCase() == 'auto')

                if ((needFabricMeta || needFabricApi) && !targetMc) {
                    throw new org.gradle.api.GradleException("please set a minecraft version")
                }

                String resolvedMappings = null
                String resolvedLoader = null
                String resolvedFabric = null

                if (needFabricMeta) {
                    project.logger.lifecycle("autoversion: resolving fabric meta ${targetMc}")
                    def meta = fetchFabricMetaFor(targetMc)
                    resolvedMappings = meta.mappings
                    resolvedLoader = meta.loader
                }

                if (needFabricApi) {
                    project.logger.lifecycle("autoversion: resolving fabric api ${targetMc}")
                    resolvedFabric = fetchFabricApiFor(targetMc)
                }

                def backupsDir = project.file("${project.buildDir}/gradle-presets")
                backupsDir.mkdirs()
                def ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                def backupFile = project.file("${backupsDir}/gradle.properties.bak.${ts}")

                def propsFile = project.file('gradle.properties')
                def existing = new Properties()
                if (propsFile.exists()) {
                    propsFile.withInputStream { ins -> existing.load(ins) }
                    propsFile.withInputStream { ins -> backupFile.withOutputStream { out -> out << ins } }
                    project.logger.lifecycle("autoversion: backup ${backupFile.name}")
                }

                def merged = new Properties()
                merged.putAll(existing)

                def putIfNotNull = { Properties p, String key, def value ->
                    if (value != null) {
                        def s = value.toString()
                        if (s?.trim()) p.setProperty(key, s)
                    }
                }

                if (ext.minecraftVersion) {
                    if (ext.minecraftVersion.toString().toLowerCase() == 'auto') {
                        if (targetMc) merged.setProperty('minecraft_version', targetMc)
                    } else {
                        merged.setProperty('minecraft_version', ext.minecraftVersion.toString())
                    }
                }

                if (ext.yarnMappings) {
                    if (ext.yarnMappings.toString().toLowerCase() == 'auto') {
                        if (resolvedMappings) merged.setProperty('yarn_mappings', resolvedMappings)
                    } else {
                        merged.setProperty('yarn_mappings', ext.yarnMappings.toString())
                    }
                }

                //unobfuscated versions have no yarn, drop any stale value carried over from an older target
                if (needFabricMeta && !resolvedMappings) merged.remove('yarn_mappings')

                if (ext.loaderVersion) {
                    if (ext.loaderVersion.toString().toLowerCase() == 'auto') {
                        if (resolvedLoader) merged.setProperty('loader_version', resolvedLoader)
                    } else {
                        merged.setProperty('loader_version', ext.loaderVersion.toString())
                    }
                }

                if (ext.fabricVersion) {
                    if (ext.fabricVersion.toString().toLowerCase() == 'auto') {
                        if (resolvedFabric) merged.setProperty('fabric_version', resolvedFabric)
                    } else {
                        merged.setProperty('fabric_version', ext.fabricVersion.toString())
                    }
                }

                putIfNotNull(merged, 'mod_version', ext.modVersion)
                putIfNotNull(merged, 'maven_group', ext.mavenGroup)
                def archivesBase = ext.archivesBaseName ?: merged.getProperty('archives_base_name') ?: 'mod'
                def mcForName = merged.getProperty('minecraft_version') ?: targetMc ?: 'unknown'
                merged.setProperty('archives_base_name', "${archivesBase}")

                def modVer = merged.getProperty('mod_version') ?: ''
                if (modVer && mcForName && !modVer.contains('+')) {
                    modVer = "${modVer}+${mcForName}"
                    merged.setProperty('mod_version', modVer)
                }

                def sb = new StringBuilder()
                sb << "# Gradle Memory\n"
                sb << "org.gradle.jvmargs=-Xmx1G\n\n"
                sb << "# Mod Properties\n"
                if (merged.getProperty('mod_version')) sb << "mod_version=${merged.getProperty('mod_version')}\n"
                if (merged.getProperty('maven_group')) sb << "maven_group=${merged.getProperty('maven_group')}\n"
                if (merged.getProperty('archives_base_name')) sb << "archives_base_name=${merged.getProperty('archives_base_name')}\n"
                if (merged.getProperty('minecraft_version')) sb << "minecraft_version=${merged.getProperty('minecraft_version')}\n"
                if (merged.getProperty('yarn_mappings')) sb << "yarn_mappings=${merged.getProperty('yarn_mappings')}\n\n"
                sb << "# Fabric Properties\n"
                if (merged.getProperty('loader_version')) sb << "loader_version=${merged.getProperty('loader_version')}\n"
                if (merged.getProperty('fabric_version')) sb << "fabric_version=${merged.getProperty('fabric_version')}\n"

                while (sb.length() > 0 && sb.charAt(sb.length() - 1) == '\n') sb.setLength(sb.length() - 1)
                propsFile.withWriter('UTF-8') { w -> w.write(sb.toString()) }
                project.logger.lifecycle("autoversion: wrote properties ${propsFile.name}")
            }
        }

        project.plugins.withId("java") {
            project.tasks.named("compileJava") {
                dependsOn(autoTask)
            }
        }

        project.plugins.withId("org.jetbrains.kotlin.jvm") {
            project.tasks.named("compileKotlin") {
                dependsOn(autoTask)
            }
        }

        project.tasks.matching { it.name == "processResources" }.configureEach {
            dependsOn(autoTask)
        }

        project.tasks.matching { it.name == "build" }.configureEach {
            dependsOn(autoTask)
        }
    }

    static Map fetchFabricMetaFor(String mcVersion) {
        def enc = URLEncoder.encode(mcVersion, 'UTF-8')
        def url = "https://meta.fabricmc.net/v1/versions/loader/${enc}"
        def txt = new URL(url).getText(requestProperties: ['User-Agent': 'Gradle/AutoVersionPlugin'])
        def arr = new JsonSlurper().parseText(txt)
        if (arr && arr.size() > 0) return [ loader: arr[0].loader.version, mappings: arr[0].mappings.version ]

        //unobfuscated versions (26.1+) have no yarn mappings, the v1 endpoint is empty for them so only resolve the loader
        url = "https://meta.fabricmc.net/v2/versions/loader/${enc}"
        txt = new URL(url).getText(requestProperties: ['User-Agent': 'Gradle/AutoVersionPlugin'])
        arr = new JsonSlurper().parseText(txt)
        if (!arr || arr.size() == 0) throw new org.gradle.api.GradleException("no fabric loader data for ${mcVersion}")
        return [ loader: arr[0].loader.version, mappings: null ]
    }

    static String fetchFabricApiFor(String mcVersion) {
        def qGame = URLEncoder.encode("[\"${mcVersion}\"]", 'UTF-8')
        def qLoaders = URLEncoder.encode("[\"fabric\"]", 'UTF-8')
        def url = "https://api.modrinth.com/v2/project/fabric-api/version?game_versions=${qGame}&loaders=${qLoaders}"
        def txt = new URL(url).getText(requestProperties: ['User-Agent': 'Gradle/AutoVersionPlugin'])
        def arr = new JsonSlurper().parseText(txt)
        if (!arr || arr.size() == 0) throw new org.gradle.api.GradleException("no fabric-api versions for ${mcVersion}")
        def rel = arr.find { it.version_type == 'release' } ?: arr[0]
        return rel.version_number
    }
}

class AutoVersionExtension {
    String modVersion
    String mavenGroup
    String archivesBaseName
    String minecraftVersion
    String yarnMappings
    String loaderVersion
    String fabricVersion
}

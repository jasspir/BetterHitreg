package you.jass.betterhitreg.ui;

//version 1.19.4
//import com.mojang.blaze3d.vertex.PoseStack;

//version 1.20 - 1.21.11
import net.minecraft.client.gui.GuiGraphics;

//version 26.1+
//import net.minecraft.client.gui.GuiGraphicsExtractor;

//version 1.21.9+
import net.minecraft.client.input.MouseButtonEvent;

import net.minecraft.client.gui.screens.Screen;

import net.minecraft.network.chat.Component;
import you.jass.betterhitreg.hitreg.Hitreg;
import you.jass.betterhitreg.settings.Commands;
import you.jass.betterhitreg.settings.Setting;
import you.jass.betterhitreg.settings.Settings;
import you.jass.betterhitreg.settings.Toggle;
import you.jass.betterhitreg.utility.MultiVersion;
import you.jass.betterhitreg.utility.Render;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static you.jass.betterhitreg.utility.MultiVersion.message;

public class UIScreen extends Screen {
    private final List<UIElement> widgets = new ArrayList<>();

    public UIScreen() {
        super(Component.literal("Custom Settings"));
    }

    @Override
    protected void init() {
        super.init();

        if (Settings.isTutorial()) Settings.set("tutorial", "false");
        Settings.load();
        Render.updateColors();

        widgets.clear();

        int panelWidthCenter = width / 2;
        int panelHeightCenter = height / 2;
        int panelWidth = 350;
        int panelHeight = 260;
        int halfPanelWidth  = panelWidth / 2;
        int halfPanelHeight = panelHeight / 2;
        int column1Start = 160;
        int column2Start = -5;
        int horizontalGap = 145;
        int rowStart = 132;
        int sliderWidth = 135;
        int sliderStart = 128;
        int sliderGap = 18;
        int verticalGap = 13;

        Color background = new Color(Render.getColor("background_color", "background_opacity"), true);
        Color border = new Color(Render.getColor("border_color", "border_opacity"), true);
        Color text = new Color(Render.getColor("text_color", "text_opacity"), true);
        Color hovered = new Color(Render.getColor("hovered_color", "hovered_opacity"), true);
        Color highlighted = new Color(Render.getColor("highlighted_color", "highlighted_opacity"), true);
        UITheme checkbox = new UITheme(border, border, text, hovered, highlighted);
        UITheme slider = new UITheme(border.darker().darker(), border.darker(), text, hovered, highlighted);
        UITheme panel = new UITheme(background, background, background, background, background);
        UITheme header = new UITheme(highlighted, highlighted, highlighted, highlighted, highlighted);
        UITheme category = new UITheme(border, border, border, border, border);
        UITheme footer = new UITheme(border.darker(), border.darker(), border.darker(), border.darker(), border.darker());

        widgets.add(new UIPanel(panelWidthCenter - halfPanelWidth, panelHeightCenter - halfPanelHeight, panelWidth, panelHeight, panel, false));

        widgets.add(new UILabel(
                panelWidthCenter,
                panelHeightCenter - halfPanelHeight + 10,
                font, "BetterHitreg v1.0.7 (" + MultiVersion.getVersion() + ")",
                header, true, true
        ));

        widgets.add(new UISlider(
                panelWidthCenter - sliderStart,
                panelHeightCenter - rowStart + verticalGap * 2,
                panelWidthCenter - column1Start,
                sliderWidth - 55, 0, 200, Settings.getHitreg(), sliderGap - 2, 1,
                "Hitreg", "", "ᴍs",
                font, slider, false, false,
                v -> {},
                v -> {
                    Settings.setInt("hitreg", v);
                    message("hitreg §7set to §f" + v + "§7ms", "/hitreg set 0");
                }
        ));

        widgets.add(new UICheckbox(
                panelWidthCenter - column2Start - 20,
                panelHeightCenter - rowStart + verticalGap * 2,
                10, 0,
                font, "",
                checkbox, true,
                Toggle.TOGGLE.toggled(),
                checked -> Toggle.TOGGLE.toggle()
        ));

        widgets.add(new UILabel(
                panelWidthCenter - column1Start,
                panelHeightCenter - rowStart + verticalGap * 3,
                font, "Utility",
                category, false, false
        ));

        widgets.add(new UICheckbox(
                panelWidthCenter - column1Start,
                panelHeightCenter - rowStart + verticalGap * 4,
                10, horizontalGap,
                font, "Safe Regs Only",
                checkbox, true,
                Toggle.SAFE_REGS_ONLY.toggled(),
                checked -> Toggle.SAFE_REGS_ONLY.toggle()
        ));

        widgets.add(new UICheckbox(
                panelWidthCenter - column1Start,
                panelHeightCenter - rowStart + verticalGap * 5,
                10, horizontalGap,
                font, "Ignore Shield Holders",
                checkbox, true,
                Toggle.IGNORE_SHIELD_HOLDERS.toggled(),
                checked -> Toggle.IGNORE_SHIELD_HOLDERS.toggle()
        ));

        widgets.add(new UICheckbox(
                panelWidthCenter - column1Start,
                panelHeightCenter - rowStart + verticalGap * 6,
                10, horizontalGap,
                font, "Alert Delays (" + Hitreg.last100Regs.getAverageDelay() + "ms)",
                checkbox, true,
                Toggle.ALERT_DELAYS.toggled(),
                checked -> Toggle.ALERT_DELAYS.toggle()
        ));

        widgets.add(new UICheckbox(
                panelWidthCenter - column1Start,
                panelHeightCenter - rowStart + verticalGap * 7,
                10, horizontalGap,
                font, "Alert Ghosts (" + Hitreg.last100Regs.getGhostRatio() + "%)",
                checkbox, true,
                Toggle.ALERT_GHOSTS.toggled(),
                checked -> Toggle.ALERT_GHOSTS.toggle()
        ));

        widgets.add(new UICheckbox(
                panelWidthCenter - column1Start,
                panelHeightCenter - rowStart + verticalGap * 8,
                10, horizontalGap,
                font, "Alert Misplaces (" + Hitreg.last100Regs.getInconsistencyRatio() + "%)",
                checkbox, true,
                Toggle.ALERT_INCONSISTENCIES.toggled(),
                checked -> Toggle.ALERT_INCONSISTENCIES.toggle()
        ));

        widgets.add(new UICheckbox(
                panelWidthCenter - column1Start,
                panelHeightCenter - rowStart + verticalGap * 9,
                10, horizontalGap,
                font, "Alert Fight Statistics",
                checkbox, true,
                Toggle.ALERT_FIGHTS.toggled(),
                checked -> Toggle.ALERT_FIGHTS.toggle()
        ));

        widgets.add(new UILabel(
                panelWidthCenter - column1Start,
                panelHeightCenter - rowStart + verticalGap * 10,
                font, "Audio",
                category, false, false
        ));

        widgets.add(new UICheckbox(
                panelWidthCenter - column1Start,
                panelHeightCenter - rowStart + verticalGap * 11,
                10, horizontalGap,
                font, "Mute Other Fights",
                checkbox, true,
                Toggle.SILENCE_OTHER_FIGHTS.toggled(),
                checked -> Toggle.SILENCE_OTHER_FIGHTS.toggle()
        ));

        widgets.add(new UICheckbox(
                panelWidthCenter - column1Start,
                panelHeightCenter - rowStart + verticalGap * 12,
                10, horizontalGap,
                font, "1.8 Hit Sounds",
                checkbox, true,
                Toggle.LEGACY_SOUNDS.toggled(),
                checked -> Toggle.LEGACY_SOUNDS.toggle()
        ));

        widgets.add(new UICheckbox(
                panelWidthCenter - column1Start,
                panelHeightCenter - rowStart + verticalGap * 13,
                10, horizontalGap,
                font, "Mute Non-hit Sounds",
                checkbox, true,
                Toggle.SILENCE_NON_HITS.toggled(),
                checked -> Toggle.SILENCE_NON_HITS.toggle()
        ));

        widgets.add(new UICheckbox(
                panelWidthCenter - column1Start,
                panelHeightCenter - rowStart + verticalGap * 14,
                10, horizontalGap,
                font, "Mute Your Hits",
                checkbox, true,
                Toggle.SILENCE_SELF.toggled(),
                checked -> Toggle.SILENCE_SELF.toggle()
        ));

        widgets.add(new UICheckbox(
                panelWidthCenter - column1Start,
                panelHeightCenter - rowStart + verticalGap * 15,
                10, horizontalGap,
                font, "Mute Their Hits",
                checkbox, true,
                Toggle.SILENCE_THEM.toggled(),
                checked -> Toggle.SILENCE_THEM.toggle()
        ));

        widgets.add(new UISlider(
                panelWidthCenter - sliderStart + 25,
                panelHeightCenter - rowStart + verticalGap * 16,
                panelWidthCenter - column1Start,
                sliderWidth - 55, 0, 100, Settings.getFloat("muffle_amount") * 100, sliderGap - 4, 5,
                "Hit Muffling", "", "%",
                font, slider, true, true,
                v -> {},
                Commands::setMuffle
        ));

        widgets.add(new UISlider(
                panelWidthCenter - sliderStart + 42,
                panelHeightCenter - rowStart + verticalGap * 17,
                panelWidthCenter - column1Start,
                sliderWidth - 72, 0, 100, Settings.getFloat("sharpen_amount") * 100, sliderGap - 4, 5,
                "Hit Sharpening", "", "%",
                font, slider, true, true,
                v -> {},
                Commands::setSharpen
        ));

        widgets.add(new UISlider(
                panelWidthCenter - sliderStart + 22,
                panelHeightCenter - rowStart + verticalGap * 18,
                panelWidthCenter - column1Start,
                sliderWidth - 49, 9, 25, Settings.getInt("metronome"), sliderGap - 7, 1,
                "Metronome", "", "t",
                font, slider, true, true,
                v -> {},
                Commands::setMetronome
        ));

        widgets.add(new UILabel(
                panelWidthCenter - column2Start,
                panelHeightCenter - rowStart + verticalGap * 2,
                font, "Render",
                category, false, false
        ));

        widgets.add(new UICheckbox(
                panelWidthCenter - column2Start,
                panelHeightCenter - rowStart + verticalGap * 3,
                10, horizontalGap,
                font, "Hide Other Fights",
                checkbox, true,
                Toggle.HIDE_OTHER_FIGHTS.toggled(),
                checked -> Toggle.HIDE_OTHER_FIGHTS.toggle()
        ));

        widgets.add(new UICheckbox(
                panelWidthCenter - column2Start,
                panelHeightCenter - rowStart + verticalGap * 4,
                10, horizontalGap,
                font, "Hide Animations",
                checkbox, true,
                Toggle.HIDE_ANIMATIONS.toggled(),
                checked -> Toggle.HIDE_ANIMATIONS.toggle()
        ));

        widgets.add(new UICheckbox(
                panelWidthCenter - column2Start,
                panelHeightCenter - rowStart + verticalGap * 5,
                10, horizontalGap,
                font, "Hide Armor",
                checkbox, true,
                Toggle.HIDE_ARMOR.toggled(),
                checked -> Toggle.HIDE_ARMOR.toggle()
        ));

        widgets.add(new UICheckbox(
                panelWidthCenter - column2Start,
                panelHeightCenter - rowStart + verticalGap * 6,
                10, horizontalGap,
                font, "Hide All Particles",
                checkbox, true,
                Toggle.HIDE_ALL_PARTICLES.toggled(),
                checked -> Toggle.HIDE_ALL_PARTICLES.toggle()
        ));

        widgets.add(new UICheckbox(
                panelWidthCenter - column2Start,
                panelHeightCenter - rowStart + verticalGap * 7,
                10, horizontalGap,
                font, "Hide Other Particles",
                checkbox, true,
                Toggle.HIDE_OTHER_PARTICLES.toggled(),
                checked -> Toggle.HIDE_OTHER_PARTICLES.toggle()
        ));

        widgets.add(new UICheckbox(
                panelWidthCenter - column2Start,
                panelHeightCenter - rowStart + verticalGap * 8,
                10, horizontalGap,
                font, "Always Hit Particles",
                checkbox, true,
                Toggle.PARTICLES_EVERY_HIT.toggled(),
                checked -> Toggle.PARTICLES_EVERY_HIT.toggle()
        ));

        widgets.add(new UICheckbox(
                panelWidthCenter - column2Start,
                panelHeightCenter - rowStart + verticalGap * 9,
                10, horizontalGap,
                font, "Show Target Hitbox",
                checkbox, true,
                Toggle.RENDER_HITBOX.toggled(),
                checked -> Toggle.RENDER_HITBOX.toggle()
        ));

        widgets.add(new UICheckbox(
                panelWidthCenter - column2Start,
                panelHeightCenter - rowStart + verticalGap * 10,
                10, horizontalGap,
                font, "Show Target Cross",
                checkbox, true,
                Toggle.RENDER_CROSS.toggled(),
                checked -> Toggle.RENDER_CROSS.toggle()
        ));

        widgets.add(new UICheckbox(
                panelWidthCenter - column2Start,
                panelHeightCenter - rowStart + verticalGap * 11,
                10, horizontalGap,
                font, "Show Server Hitbox",
                checkbox, true,
                Toggle.RENDER_SERVER_HITBOX.toggled(),
                checked -> Toggle.RENDER_SERVER_HITBOX.toggle()
        ));

        widgets.add(new UICheckbox(
                panelWidthCenter - column2Start,
                panelHeightCenter - rowStart + verticalGap * 12,
                10, horizontalGap,
                font, "Perfect Hit Color",
                checkbox, true,
                Toggle.PERFECT_HIT_COLOR.toggled(),
                checked -> Toggle.PERFECT_HIT_COLOR.toggle()
        ));

        widgets.add(new UICheckbox(
                panelWidthCenter - column2Start,
                panelHeightCenter - rowStart + verticalGap * 13,
                10, horizontalGap,
                font, "Jump Reset Color",
                checkbox, true,
                Toggle.JUMP_RESET_COLOR.toggled(),
                checked -> Toggle.JUMP_RESET_COLOR.toggle()
        ));

        widgets.add(new UILabel(
                panelWidthCenter - column2Start,
                panelHeightCenter - rowStart + verticalGap * 14,
                font, "Hit Range",
                checkbox, false, false
        ));

        widgets.add(new UICheckbox(
                panelWidthCenter - column2Start + 75,
                panelHeightCenter - rowStart + verticalGap * 14,
                10, horizontalGap - 120,
                font, "You",
                checkbox, true,
                Toggle.RENDER_YOUR_REACH.toggled(),
                checked -> Toggle.RENDER_YOUR_REACH.toggle()
        ));

        widgets.add(new UICheckbox(
                panelWidthCenter - column2Start + 115,
                panelHeightCenter - rowStart + verticalGap * 14,
                10, horizontalGap - 115,
                font, "Them",
                checkbox, true,
                Toggle.RENDER_THEIR_REACH.toggled(),
                checked -> Toggle.RENDER_THEIR_REACH.toggle()
        ));

        widgets.add(new UILabel(
                panelWidthCenter - column2Start,
                panelHeightCenter - rowStart + verticalGap * 15,
                font, "Jump Range",
                checkbox, false, false
        ));

        widgets.add(new UICheckbox(
                panelWidthCenter - column2Start + 75,
                panelHeightCenter - rowStart + verticalGap * 15,
                10, horizontalGap - 120,
                font, "You",
                checkbox, true,
                Toggle.RENDER_YOUR_JUMP.toggled(),
                checked -> Toggle.RENDER_YOUR_JUMP.toggle()
        ));

        widgets.add(new UICheckbox(
                panelWidthCenter - column2Start + 115,
                panelHeightCenter - rowStart + verticalGap * 15,
                10, horizontalGap - 115,
                font, "Them",
                checkbox, true,
                Toggle.RENDER_THEIR_JUMP.toggled(),
                checked -> Toggle.RENDER_THEIR_JUMP.toggle()
        ));

        widgets.add(new UICheckbox(
                panelWidthCenter - column2Start,
                panelHeightCenter - rowStart + verticalGap * 16,
                10, horizontalGap,
                font, "Void World",
                checkbox, true,
                Toggle.VOID_WORLD.toggled(),
                checked -> Toggle.VOID_WORLD.toggle()
        ));

        widgets.add(new UICheckbox(
                panelWidthCenter - column2Start,
                panelHeightCenter - rowStart + verticalGap * 17,
                10, horizontalGap,
                font, "Custom Ground",
                checkbox, true,
                Toggle.SOLID_FLOOR.toggled(),
                checked -> Toggle.SOLID_FLOOR.toggle()
        ));

        widgets.add(new UISlider(
                panelWidthCenter - column2Start + 52,
                panelHeightCenter - rowStart + verticalGap * 18,
                panelWidthCenter - column2Start,
                sliderWidth - 50, 0, 16, (float) Setting.GRID_FLOOR.get(), sliderGap - 4, 1,
                "Grid Floor", "", "b",
                font, slider, true, true,
                v -> {},
                Commands::setGridSize
        ));
    }

    //version 1.19.4
//    @Override
//    public void render(PoseStack matrixStack, int mouseX, int mouseY, float delta) {
//        for (UIElement w : widgets) {
//            w.render(matrixStack, mouseX, mouseY);
//        }
//        super.render(matrixStack, mouseX, mouseY, delta);
//    }

    //version 1.20 - 1.21.11
    @Override
    public void render(GuiGraphics ctx, int mouseX, int mouseY, float delta) {
        for (UIElement w : widgets) {
            w.render(ctx, mouseX, mouseY);
        }
        super.render(ctx, mouseX, mouseY, delta);
    }

    //version 26.1+
//    @Override
//    public void extractRenderState(GuiGraphicsExtractor ctx, int mouseX, int mouseY, float delta) {
//        super.extractRenderState(ctx, mouseX, mouseY, delta);
//        for (UIElement w : widgets) {
//            w.render(ctx, mouseX, mouseY);
//        }
//    }

    //version 1.19.4
//    @Override
//    public void renderBackground(PoseStack matrixStack) {}

    //version 1.20 - 1.20.1
//    @Override
//    public void renderBackground(GuiGraphics context) {}

    //version 1.20.2 - 1.21.11
    @Override
    public void renderBackground(GuiGraphics ctx, int mouseX, int mouseY, float delta) {}

    //version 26.1+
//    @Override
//    public void extractBackground(GuiGraphicsExtractor ctx, int mouseX, int mouseY, float delta) {}

    //version 1.21.8-
//    @Override
//    public boolean mouseClicked(double mx, double my, int button) {
//        for (UIElement w : widgets) {
//            if (w.mouseClicked(mx, my, button)) return true;
//        }
//        return super.mouseClicked(mx, my, button);
//    }

    //version 1.21.8-
//    @Override
//    public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
//        for (UIElement w : widgets) {
//            if (w.mouseDragged(mx, my, button, dx, dy)) return true;
//        }
//        return false;
//    }

    //version 1.21.8-
//    @Override
//    public boolean mouseReleased(double mx, double my, int button) {
//        for (UIElement w : widgets) {
//            if (w.mouseReleased(mx, my, button)) return true;
//        }
//        return super.mouseReleased(mx, my, button);
//    }

    //version 1.21.9+
    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        for (UIElement w : widgets) {
            if (w.mouseClicked(click.x(), click.y(), click.button())) return true;
        }
        return super.mouseClicked(click, doubled);
    }

    //version 1.21.9+
    @Override
    public boolean mouseDragged(MouseButtonEvent click, double offsetX, double offsetY) {
        for (UIElement w : widgets) {
            if (w.mouseDragged(click.x(), click.y(), click.button(), offsetX, offsetY)) return true;
        }
        return super.mouseDragged(click, offsetX, offsetY);
    }

    //version 1.21.9+
    @Override
    public boolean mouseReleased(MouseButtonEvent click) {
        for (UIElement w : widgets) {
            if (w.mouseReleased(click.x(), click.y(), click.button())) return true;
        }
        return super.mouseReleased(click);
    }
}
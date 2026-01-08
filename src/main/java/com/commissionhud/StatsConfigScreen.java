package com.commissionhud;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

public class StatsConfigScreen extends Screen {
    private final Screen parent;
    private int dragX, dragY;
    private boolean dragging = false;
    
    public StatsConfigScreen(Screen parent) {
        super(Text.literal("Stats Display Config"));
        this.parent = parent;
    }
    
    @Override
    protected void init() {
        ConfigManager.Config cfg = CommissionHudMod.config.getConfig();
        
        int centerX = width / 2;
        int buttonWidth = 200;
        int buttonHeight = 20;
        int spacing = 24;
        int startY = 35;
        
        // Toggle enabled
        addDrawableChild(ButtonWidget.builder(
            Text.literal("Enabled: " + cfg.statsEnabled),
            button -> {
                cfg.statsEnabled = !cfg.statsEnabled;
                CommissionHudMod.config.save();
                button.setMessage(Text.literal("Enabled: " + cfg.statsEnabled));
            })
            .dimensions(centerX - buttonWidth / 2, startY, buttonWidth, buttonHeight)
            .build());
        
        // Title color picker button
        addDrawableChild(ButtonWidget.builder(
            Text.literal("Title Color: #" + String.format("%06X", cfg.statsTitleColor)),
            button -> {
                if (client != null) {
                    client.setScreen(new ColorPickerScreen(this, ColorPickerScreen.ColorType.STATS_TITLE_COLOR));
                }
            })
            .dimensions(centerX - buttonWidth / 2, startY + spacing, buttonWidth, buttonHeight)
            .build());
        
        // Label color picker button
        addDrawableChild(ButtonWidget.builder(
            Text.literal("Label Color: #" + String.format("%06X", cfg.statsLabelColor)),
            button -> {
                if (client != null) {
                    client.setScreen(new ColorPickerScreen(this, ColorPickerScreen.ColorType.STATS_LABEL_COLOR));
                }
            })
            .dimensions(centerX - buttonWidth / 2, startY + spacing * 2, buttonWidth, buttonHeight)
            .build());
        
        // Back button
        addDrawableChild(ButtonWidget.builder(Text.literal("Back"), button -> close())
            .dimensions(centerX - buttonWidth / 2, height - 28, buttonWidth, buttonHeight)
            .build());
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 12, 0xFFFFFFFF);
        
        ConfigManager.Config cfg = CommissionHudMod.config.getConfig();
        
        // Draw color preview squares
        int colorPreviewX = width / 2 + 105;
        
        // Title color preview
        drawColorPreview(context, colorPreviewX, 35 + 24, cfg.statsTitleColor);
        
        // Label color preview
        drawColorPreview(context, colorPreviewX, 35 + 24 * 2, cfg.statsLabelColor);
        
        // Instructions
        context.drawCenteredTextWithShadow(textRenderer, 
            Text.literal("Drag the preview to reposition"), 
            width / 2, height - 55, 0xFF888888);
        context.drawCenteredTextWithShadow(textRenderer, 
            Text.literal("Stat value colors are automatic based on stat type"), 
            width / 2, height - 43, 0xFF666666);
        
        super.render(context, mouseX, mouseY, delta);
        
        // Render stats preview
        context.getMatrices().pushMatrix();
        context.getMatrices().translate(cfg.statsX, cfg.statsY);
        context.getMatrices().scale(cfg.statsScale, cfg.statsScale);
        
        context.drawText(textRenderer, Text.literal("Stats:"), 0, 0, ensureAlpha(cfg.statsTitleColor), true);
        
        // Example stats
        String miningLabel = "Mining Speed: ";
        String miningValue = "⸕1549";
        int miningLabelWidth = textRenderer.getWidth(miningLabel);
        context.drawText(textRenderer, Text.literal(miningLabel), 0, 12, ensureAlpha(cfg.statsLabelColor), true);
        context.drawText(textRenderer, Text.literal(miningValue), miningLabelWidth, 12, 0xFFFFAA00, true);
        
        String fortuneLabel = "Mining Fortune: ";
        String fortuneValue = "☘850";
        int fortuneLabelWidth = textRenderer.getWidth(fortuneLabel);
        context.drawText(textRenderer, Text.literal(fortuneLabel), 0, 22, ensureAlpha(cfg.statsLabelColor), true);
        context.drawText(textRenderer, Text.literal(fortuneValue), fortuneLabelWidth, 22, 0xFFFFAA00, true);
        
        String pristineLabel = "Pristine: ";
        String pristineValue = "✧5.2";
        int pristineLabelWidth = textRenderer.getWidth(pristineLabel);
        context.drawText(textRenderer, Text.literal(pristineLabel), 0, 32, ensureAlpha(cfg.statsLabelColor), true);
        context.drawText(textRenderer, Text.literal(pristineValue), pristineLabelWidth, 32, 0xFFAA00AA, true);
        
        context.getMatrices().popMatrix();
    }
    
    // Ensure color has proper alpha channel for 1.21.6+ ARGB format
    private static int ensureAlpha(int color) {
        if ((color & 0xFF000000) == 0) {
            return color | 0xFF000000;
        }
        return color;
    }
    
    private void drawColorPreview(DrawContext context, int x, int y, int color) {
        context.fill(x, y + 2, x + 16, y + 18, ensureAlpha(color));
        // Border
        context.fill(x - 1, y + 1, x + 17, y + 2, 0xFF333333);
        context.fill(x - 1, y + 18, x + 17, y + 19, 0xFF333333);
        context.fill(x - 1, y + 1, x, y + 19, 0xFF333333);
        context.fill(x + 16, y + 1, x + 17, y + 19, 0xFF333333);
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        ConfigManager.Config cfg = CommissionHudMod.config.getConfig();
        int previewWidth = 130;
        int previewHeight = 45;
        
        if (mouseX >= cfg.statsX && mouseX <= cfg.statsX + previewWidth * cfg.statsScale && 
            mouseY >= cfg.statsY && mouseY <= cfg.statsY + previewHeight * cfg.statsScale) {
            dragging = true;
            dragX = (int) (mouseX - cfg.statsX);
            dragY = (int) (mouseY - cfg.statsY);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
    
    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        dragging = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }
    
    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (dragging) {
            CommissionHudMod.config.setStatsPosition((int) mouseX - dragX, (int) mouseY - dragY);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }
    
    @Override
    public void close() {
        if (client != null) {
            client.setScreen(parent);
        }
    }
}

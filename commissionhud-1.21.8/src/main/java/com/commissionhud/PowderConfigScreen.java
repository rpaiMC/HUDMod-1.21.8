package com.commissionhud;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

public class PowderConfigScreen extends Screen {
    private final Screen parent;
    private int dragX, dragY;
    private boolean dragging = false;
    
    // Store button references for updating
    private ButtonWidget calcEnabledButton;
    private ButtonWidget calcIntervalButton;
    private ButtonWidget resetTrackingButton;
    
    public PowderConfigScreen(Screen parent) {
        super(Text.literal("Powder Display Config"));
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
            Text.literal("Enabled: " + cfg.powderEnabled),
            button -> {
                cfg.powderEnabled = !cfg.powderEnabled;
                CommissionHudMod.config.save();
                button.setMessage(Text.literal("Enabled: " + cfg.powderEnabled));
            })
            .dimensions(centerX - buttonWidth / 2, startY, buttonWidth, buttonHeight)
            .build());
        
        // Title color picker button
        addDrawableChild(ButtonWidget.builder(
            Text.literal("Title Color: #" + String.format("%06X", cfg.powderTitleColor)),
            button -> {
                if (client != null) {
                    client.setScreen(new ColorPickerScreen(this, ColorPickerScreen.ColorType.POWDER_TITLE_COLOR));
                }
            })
            .dimensions(centerX - buttonWidth / 2, startY + spacing, buttonWidth, buttonHeight)
            .build());
        
        // Label color picker button
        addDrawableChild(ButtonWidget.builder(
            Text.literal("Label Color: #" + String.format("%06X", cfg.powderLabelColor)),
            button -> {
                if (client != null) {
                    client.setScreen(new ColorPickerScreen(this, ColorPickerScreen.ColorType.POWDER_LABEL_COLOR));
                }
            })
            .dimensions(centerX - buttonWidth / 2, startY + spacing * 2, buttonWidth, buttonHeight)
            .build());
        
        // Value color picker button
        addDrawableChild(ButtonWidget.builder(
            Text.literal("Value Color: #" + String.format("%06X", cfg.powderValueColor)),
            button -> {
                if (client != null) {
                    client.setScreen(new ColorPickerScreen(this, ColorPickerScreen.ColorType.POWDER_VALUE_COLOR));
                }
            })
            .dimensions(centerX - buttonWidth / 2, startY + spacing * 3, buttonWidth, buttonHeight)
            .build());
        
        // Separator label will be drawn in render()
        
        // Powder Calculator toggle
        calcEnabledButton = ButtonWidget.builder(
            Text.literal("Powder Calculator: " + (cfg.powderCalcEnabled ? "ON" : "OFF")),
            button -> {
                cfg.powderCalcEnabled = !cfg.powderCalcEnabled;
                CommissionHudMod.config.save();
                button.setMessage(Text.literal("Powder Calculator: " + (cfg.powderCalcEnabled ? "ON" : "OFF")));
                updateCalcButtonsState();
                // Reset tracking when enabled
                if (cfg.powderCalcEnabled) {
                    CommissionHudMod.powderManager.resetTracking();
                }
            })
            .dimensions(centerX - buttonWidth / 2, startY + spacing * 5, buttonWidth, buttonHeight)
            .build();
        addDrawableChild(calcEnabledButton);
        
        // Powder Calculator interval
        calcIntervalButton = ButtonWidget.builder(
            Text.literal("Interval: " + getIntervalDisplayName(cfg.powderCalcInterval)),
            button -> {
                CommissionHudMod.config.cyclePowderCalcInterval();
                button.setMessage(Text.literal("Interval: " + getIntervalDisplayName(CommissionHudMod.config.getPowderCalcInterval())));
            })
            .dimensions(centerX - buttonWidth / 2, startY + spacing * 6, buttonWidth, buttonHeight)
            .build();
        addDrawableChild(calcIntervalButton);
        
        // Reset tracking button
        resetTrackingButton = ButtonWidget.builder(
            Text.literal("Reset Tracking"),
            button -> {
                CommissionHudMod.powderManager.resetTracking();
            })
            .dimensions(centerX - buttonWidth / 2, startY + spacing * 7, buttonWidth, buttonHeight)
            .build();
        addDrawableChild(resetTrackingButton);
        
        updateCalcButtonsState();
        
        // Back button
        addDrawableChild(ButtonWidget.builder(Text.literal("Back"), button -> close())
            .dimensions(centerX - buttonWidth / 2, height - 28, buttonWidth, buttonHeight)
            .build());
    }
    
    private void updateCalcButtonsState() {
        boolean enabled = CommissionHudMod.config.isPowderCalcEnabled();
        calcIntervalButton.active = enabled;
        resetTrackingButton.active = enabled;
    }
    
    private String getIntervalDisplayName(int minutes) {
        if (minutes == 60) {
            return "Per Hour (/hr)";
        } else {
            return "Per " + minutes + " Minutes (/" + minutes + "m)";
        }
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 12, 0xFFFFFF);
        
        ConfigManager.Config cfg = CommissionHudMod.config.getConfig();
        
        // Draw color preview squares
        int colorPreviewX = width / 2 + 105;
        
        // Title color preview
        drawColorPreview(context, colorPreviewX, 35 + 24, cfg.powderTitleColor);
        
        // Label color preview
        drawColorPreview(context, colorPreviewX, 35 + 24 * 2, cfg.powderLabelColor);
        
        // Value color preview
        drawColorPreview(context, colorPreviewX, 35 + 24 * 3, cfg.powderValueColor);
        
        // Separator for calculator section
        context.drawCenteredTextWithShadow(textRenderer, 
            Text.literal("--- Powder Calculator ---"), 
            width / 2, 35 + 24 * 4 + 10, 0xFF888888);
        
        // Instructions
        context.drawCenteredTextWithShadow(textRenderer, 
            Text.literal("Drag the preview to reposition"), 
            width / 2, height - 55, 0xFF888888);
        
        super.render(context, mouseX, mouseY, delta);
        
        // Render powder preview
        context.getMatrices().pushMatrix();
        context.getMatrices().translate(cfg.powderX, cfg.powderY);
        context.getMatrices().scale(cfg.powderScale, cfg.powderScale);
        
        context.drawText(textRenderer, Text.literal("Powder:"), 0, 0, cfg.powderTitleColor, true);
        
        String mithrilLabel = "Mithril: ";
        String mithrilValue = "123,456";
        int mithrilLabelWidth = textRenderer.getWidth(mithrilLabel);
        context.drawText(textRenderer, Text.literal(mithrilLabel), 0, 12, cfg.powderLabelColor, true);
        context.drawText(textRenderer, Text.literal(mithrilValue), mithrilLabelWidth, 12, cfg.powderValueColor, true);
        
        String gemstoneLabel = "Gemstone: ";
        String gemstoneValue = "78,901";
        int gemstoneLabelWidth = textRenderer.getWidth(gemstoneLabel);
        context.drawText(textRenderer, Text.literal(gemstoneLabel), 0, 22, cfg.powderLabelColor, true);
        context.drawText(textRenderer, Text.literal(gemstoneValue), gemstoneLabelWidth, 22, cfg.powderValueColor, true);
        
        int nextY = 32;
        
        // Show calculator preview only when enabled
        if (cfg.powderCalcEnabled) {
            String intervalLabel = CommissionHudMod.config.getPowderCalcIntervalLabel();
            
            String mithrilRateLabel = "Mithril" + intervalLabel + ": ";
            String mithrilRateValue = "+5,432";
            int mithrilRateLabelWidth = textRenderer.getWidth(mithrilRateLabel);
            context.drawText(textRenderer, Text.literal(mithrilRateLabel), 0, nextY, cfg.powderLabelColor, true);
            context.drawText(textRenderer, Text.literal(mithrilRateValue), mithrilRateLabelWidth, nextY, 0xFF55FF55, true);
            nextY += 10;
            
            String gemstoneRateLabel = "Gemstone" + intervalLabel + ": ";
            String gemstoneRateValue = "+2,100";
            int gemstoneRateLabelWidth = textRenderer.getWidth(gemstoneRateLabel);
            context.drawText(textRenderer, Text.literal(gemstoneRateLabel), 0, nextY, cfg.powderLabelColor, true);
            context.drawText(textRenderer, Text.literal(gemstoneRateValue), gemstoneRateLabelWidth, nextY, 0xFF55FF55, true);
        }
        
        context.getMatrices().popMatrix();
    }
    
    private void drawColorPreview(DrawContext context, int x, int y, int color) {
        context.fill(x, y + 2, x + 16, y + 18, 0xFF000000 | color);
        // Border
        context.fill(x - 1, y + 1, x + 17, y + 2, 0xFF333333);
        context.fill(x - 1, y + 18, x + 17, y + 19, 0xFF333333);
        context.fill(x - 1, y + 1, x, y + 19, 0xFF333333);
        context.fill(x + 16, y + 1, x + 17, y + 19, 0xFF333333);
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        ConfigManager.Config cfg = CommissionHudMod.config.getConfig();
        int previewWidth = 120;
        int previewHeight = cfg.powderCalcEnabled ? 55 : 35;
        
        if (mouseX >= cfg.powderX && mouseX <= cfg.powderX + previewWidth * cfg.powderScale && 
            mouseY >= cfg.powderY && mouseY <= cfg.powderY + previewHeight * cfg.powderScale) {
            dragging = true;
            dragX = (int) (mouseX - cfg.powderX);
            dragY = (int) (mouseY - cfg.powderY);
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
            CommissionHudMod.config.setPowderPosition((int) mouseX - dragX, (int) mouseY - dragY);
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

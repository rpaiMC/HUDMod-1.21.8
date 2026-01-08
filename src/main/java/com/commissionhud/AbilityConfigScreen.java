package com.commissionhud;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

public class AbilityConfigScreen extends Screen {
    private final Screen parent;
    private int dragX, dragY;
    private boolean dragging = false;
    
    public AbilityConfigScreen(Screen parent) {
        super(Text.literal("Pickaxe Ability Config"));
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
            Text.literal("Enabled: " + cfg.abilityEnabled),
            button -> {
                cfg.abilityEnabled = !cfg.abilityEnabled;
                CommissionHudMod.config.save();
                button.setMessage(Text.literal("Enabled: " + cfg.abilityEnabled));
            })
            .dimensions(centerX - buttonWidth / 2, startY, buttonWidth, buttonHeight)
            .build());
        
        // Title color picker button
        addDrawableChild(ButtonWidget.builder(
            Text.literal("Title Color: #" + String.format("%06X", cfg.abilityTitleColor)),
            button -> {
                if (client != null) {
                    client.setScreen(new ColorPickerScreen(this, ColorPickerScreen.ColorType.ABILITY_TITLE_COLOR));
                }
            })
            .dimensions(centerX - buttonWidth / 2, startY + spacing, buttonWidth, buttonHeight)
            .build());
        
        // Label color picker button
        addDrawableChild(ButtonWidget.builder(
            Text.literal("Label Color: #" + String.format("%06X", cfg.abilityLabelColor)),
            button -> {
                if (client != null) {
                    client.setScreen(new ColorPickerScreen(this, ColorPickerScreen.ColorType.ABILITY_LABEL_COLOR));
                }
            })
            .dimensions(centerX - buttonWidth / 2, startY + spacing * 2, buttonWidth, buttonHeight)
            .build());
        
        // Cooldown color picker button
        addDrawableChild(ButtonWidget.builder(
            Text.literal("Cooldown Color: #" + String.format("%06X", cfg.abilityValueColor)),
            button -> {
                if (client != null) {
                    client.setScreen(new ColorPickerScreen(this, ColorPickerScreen.ColorType.ABILITY_VALUE_COLOR));
                }
            })
            .dimensions(centerX - buttonWidth / 2, startY + spacing * 3, buttonWidth, buttonHeight)
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
        drawColorPreview(context, colorPreviewX, 35 + 24, cfg.abilityTitleColor);
        
        // Label color preview
        drawColorPreview(context, colorPreviewX, 35 + 24 * 2, cfg.abilityLabelColor);
        
        // Cooldown color preview
        drawColorPreview(context, colorPreviewX, 35 + 24 * 3, cfg.abilityValueColor);
        
        // Instructions
        context.drawCenteredTextWithShadow(textRenderer, 
            Text.literal("Drag the preview to reposition"), 
            width / 2, height - 55, 0xFF888888);
        
        super.render(context, mouseX, mouseY, delta);
        
        // Render ability preview
        context.getMatrices().pushMatrix();
        context.getMatrices().translate(cfg.abilityX, cfg.abilityY);
        context.getMatrices().scale(cfg.abilityScale, cfg.abilityScale);
        
        context.drawText(textRenderer, Text.literal("Pickaxe Ability:"), 0, 0, ensureAlpha(cfg.abilityTitleColor), true);
        
        String nameLabel = "Pickobulus: ";
        String statusAvailable = "Available";
        int nameLabelWidth = textRenderer.getWidth(nameLabel);
        context.drawText(textRenderer, Text.literal(nameLabel), 0, 12, ensureAlpha(cfg.abilityLabelColor), true);
        context.drawText(textRenderer, Text.literal(statusAvailable), nameLabelWidth, 12, 0xFF55FF55, true);
        
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
        int previewWidth = 120;
        int previewHeight = 25;
        
        if (mouseX >= cfg.abilityX && mouseX <= cfg.abilityX + previewWidth * cfg.abilityScale && 
            mouseY >= cfg.abilityY && mouseY <= cfg.abilityY + previewHeight * cfg.abilityScale) {
            dragging = true;
            dragX = (int) (mouseX - cfg.abilityX);
            dragY = (int) (mouseY - cfg.abilityY);
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
            CommissionHudMod.config.setAbilityPosition((int) mouseX - dragX, (int) mouseY - dragY);
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

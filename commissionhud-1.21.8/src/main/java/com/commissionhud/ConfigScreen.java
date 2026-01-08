package com.commissionhud;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

public class ConfigScreen extends Screen {
    private final Screen parent;
    private int dragX, dragY;
    private boolean dragging = false;
    
    public ConfigScreen() {
        super(Text.literal("Commission HUD Config"));
        this.parent = null;
    }
    
    @Override
    public void tick() {
        super.tick();
    }
    
    @Override
    protected void init() {
        // Get fresh config values each time init is called
        ConfigManager.Config cfg = CommissionHudMod.config.getConfig();
        
        int centerX = width / 2;
        int buttonWidth = 200;
        int buttonHeight = 20;
        int spacing = 24;
        int startY = 35;
        
        // Toggle enabled
        addDrawableChild(ButtonWidget.builder(
            Text.literal("Enabled: " + cfg.enabled),
            button -> {
                cfg.enabled = !cfg.enabled;
                CommissionHudMod.config.save();
                button.setMessage(Text.literal("Enabled: " + cfg.enabled));
            })
            .dimensions(centerX - buttonWidth / 2, startY, buttonWidth, buttonHeight)
            .build());
        
        // Toggle percentage display
        addDrawableChild(ButtonWidget.builder(
            Text.literal("Show Progress: " + cfg.showPercentage),
            button -> {
                cfg.showPercentage = !cfg.showPercentage;
                CommissionHudMod.config.save();
                button.setMessage(Text.literal("Show Progress: " + cfg.showPercentage));
            })
            .dimensions(centerX - buttonWidth / 2, startY + spacing, buttonWidth, buttonHeight)
            .build());
        
        // Progress format toggle (percentage vs fraction)
        addDrawableChild(ButtonWidget.builder(
            Text.literal("Format: " + cfg.progressFormat.getDisplayName()),
            button -> {
                CommissionHudMod.config.cycleProgressFormat();
                button.setMessage(Text.literal("Format: " + CommissionHudMod.config.getProgressFormat().getDisplayName()));
            })
            .dimensions(centerX - buttonWidth / 2, startY + spacing * 2, buttonWidth, buttonHeight)
            .build());
        
        // Display mode toggle
        addDrawableChild(ButtonWidget.builder(
            Text.literal("Display: " + cfg.displayMode.getDisplayName()),
            button -> {
                CommissionHudMod.config.cycleDisplayMode();
                button.setMessage(Text.literal("Display: " + CommissionHudMod.config.getDisplayMode().getDisplayName()));
            })
            .dimensions(centerX - buttonWidth / 2, startY + spacing * 3, buttonWidth, buttonHeight)
            .build());
        
        // Title color picker button
        addDrawableChild(ButtonWidget.builder(
            Text.literal("Title Color: #" + String.format("%06X", cfg.titleColor)),
            button -> {
                if (client != null) {
                    client.setScreen(new ColorPickerScreen(this, ColorPickerScreen.ColorType.COMMISSION_TITLE_COLOR));
                }
            })
            .dimensions(centerX - buttonWidth / 2, startY + spacing * 4, buttonWidth, buttonHeight)
            .build());
        
        // Text color picker button
        addDrawableChild(ButtonWidget.builder(
            Text.literal("Text Color: #" + String.format("%06X", cfg.color)),
            button -> {
                if (client != null) {
                    client.setScreen(new ColorPickerScreen(this, ColorPickerScreen.ColorType.TEXT_COLOR));
                }
            })
            .dimensions(centerX - buttonWidth / 2, startY + spacing * 5, buttonWidth, buttonHeight)
            .build());
        
        // Progress bar color picker button
        addDrawableChild(ButtonWidget.builder(
            Text.literal("Progress Bar Color: #" + String.format("%06X", cfg.progressBarColor)),
            button -> {
                if (client != null) {
                    client.setScreen(new ColorPickerScreen(this, ColorPickerScreen.ColorType.PROGRESS_BAR_COLOR));
                }
            })
            .dimensions(centerX - buttonWidth / 2, startY + spacing * 6, buttonWidth, buttonHeight)
            .build());
        
        // Powder display config button
        addDrawableChild(ButtonWidget.builder(
            Text.literal("Powder Display Settings..."),
            button -> {
                if (client != null) {
                    client.setScreen(new PowderConfigScreen(this));
                }
            })
            .dimensions(centerX - buttonWidth / 2, startY + spacing * 7, buttonWidth, buttonHeight)
            .build());
        
        // Ability display config button
        addDrawableChild(ButtonWidget.builder(
            Text.literal("Pickaxe Ability Settings..."),
            button -> {
                if (client != null) {
                    client.setScreen(new AbilityConfigScreen(this));
                }
            })
            .dimensions(centerX - buttonWidth / 2, startY + spacing * 8, buttonWidth, buttonHeight)
            .build());
        
        // Stats display config button
        addDrawableChild(ButtonWidget.builder(
            Text.literal("Stats Display Settings..."),
            button -> {
                if (client != null) {
                    client.setScreen(new StatsConfigScreen(this));
                }
            })
            .dimensions(centerX - buttonWidth / 2, startY + spacing * 9, buttonWidth, buttonHeight)
            .build());
        
        // Position & Scale settings button
        addDrawableChild(ButtonWidget.builder(
            Text.literal("Position & Scale Settings..."),
            button -> {
                if (client != null) {
                    client.setScreen(new PositionScaleScreen(this));
                }
            })
            .dimensions(centerX - buttonWidth / 2, startY + spacing * 10, buttonWidth, buttonHeight)
            .build());
        
        // Done button
        addDrawableChild(ButtonWidget.builder(Text.literal("Done"), button -> close())
            .dimensions(centerX - buttonWidth / 2, height - 28, buttonWidth, buttonHeight)
            .build());
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 12, 0xFFFFFF);
        
        ConfigManager.Config cfg = CommissionHudMod.config.getConfig();
        
        // Draw color preview squares next to the color buttons
        int colorPreviewX = width / 2 + 105;
        
        // Title color preview
        int titleColorY = 35 + 24 * 4;
        drawColorPreview(context, colorPreviewX, titleColorY, cfg.titleColor);
        
        // Text color preview
        int textColorY = 35 + 24 * 5;
        drawColorPreview(context, colorPreviewX, textColorY, cfg.color);
        
        // Progress bar color preview
        int barColorY = 35 + 24 * 6;
        drawColorPreview(context, colorPreviewX, barColorY, cfg.progressBarColor);
        
        // Instructions
        context.drawCenteredTextWithShadow(textRenderer, 
            Text.literal("Drag the preview to reposition the HUD"), 
            width / 2, height - 55, 0xFF888888);
        
        // Show current location if in mining islands only mode
        if (cfg.displayMode == ConfigManager.DisplayMode.MINING_ISLANDS_ONLY) {
            String location = CommissionHudMod.locationDetector.getCurrentLocation();
            boolean inMining = CommissionHudMod.locationDetector.isInMiningIsland();
            
            String locationText = location.isEmpty() ? "Unknown" : location;
            int locationColor = inMining ? 0xFF55FF55 : 0xFF5555;
            
            context.drawCenteredTextWithShadow(textRenderer,
                Text.literal("Location: " + locationText + (inMining ? " ✓" : " ✗")),
                width / 2, height - 43, locationColor);
        }
        
        super.render(context, mouseX, mouseY, delta);
        
        // Get actual commission count (minimum 2 for preview)
        int commissionCount = Math.max(2, CommissionHudMod.commissionManager.getActiveCommissions().size());
        if (commissionCount > 4) commissionCount = 4; // Cap at 4
        
        // Render HUD preview with dynamic number of example commissions
        context.getMatrices().pushMatrix();
        context.getMatrices().translate(cfg.x, cfg.y);
        context.getMatrices().scale(cfg.scale, cfg.scale);
        
        int y = 0;
        context.drawText(textRenderer, Text.literal("Commissions:"), 0, y, cfg.titleColor, true);
        y += 12;
        
        // Example commission data
        String[][] exampleCommissions = {
            {"Mithril Miner", "45", "158/350"},
            {"Goblin Slayer", "80", "80/100"},
            {"Titanium Miner", "100", "15/15"},
            {"Golden Goblin", "25", "1/4"}
        };
        int[] exampleColors = {cfg.color, 0xFFFFFF55, 0xFF55FF55, cfg.color};
        
        for (int i = 0; i < commissionCount; i++) {
            String name = exampleCommissions[i][0];
            String percent = exampleCommissions[i][1];
            String fraction = exampleCommissions[i][2];
            int color = exampleColors[i];
            int progress = Integer.parseInt(percent);
            
            String commissionText = "• " + name;
            if (cfg.showPercentage) {
                commissionText += cfg.progressFormat == ConfigManager.ProgressFormat.PERCENTAGE 
                    ? ": " + percent + "%" 
                    : ": " + fraction;
            }
            context.drawText(textRenderer, Text.literal(commissionText), 0, y, color, true);
            
            if (cfg.showPercentage) {
                context.fill(0, y + 9, 100, y + 11, 0x88000000);
                int barColor = progress >= 100 ? 0xFF55FF55 : cfg.progressBarColor;
                context.fill(0, y + 9, progress, y + 11, barColor);
            }
            y += cfg.showPercentage ? 14 : 10;
        }
        
        context.getMatrices().popMatrix();
    }
    
    private void drawColorPreview(DrawContext context, int x, int y, int color) {
        // Ensure color has full alpha
        context.fill(x, y + 2, x + 16, y + 18, color | 0xFF000000);
        // Border
        context.fill(x - 1, y + 1, x + 17, y + 2, 0xFF333333);
        context.fill(x - 1, y + 18, x + 17, y + 19, 0xFF333333);
        context.fill(x - 1, y + 1, x, y + 19, 0xFF333333);
        context.fill(x + 16, y + 1, x + 17, y + 19, 0xFF333333);
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        ConfigManager.Config cfg = CommissionHudMod.config.getConfig();
        int previewWidth = 150;
        int previewHeight = 50;
        
        if (mouseX >= cfg.x && mouseX <= cfg.x + previewWidth * cfg.scale && 
            mouseY >= cfg.y && mouseY <= cfg.y + previewHeight * cfg.scale) {
            dragging = true;
            dragX = (int) (mouseX - cfg.x);
            dragY = (int) (mouseY - cfg.y);
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
            CommissionHudMod.config.setPosition((int) mouseX - dragX, (int) mouseY - dragY);
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

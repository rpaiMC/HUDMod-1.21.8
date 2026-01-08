package com.commissionhud;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

public class PositionScaleScreen extends Screen {
    private final Screen parent;
    
    // Dragging state
    private enum DragTarget { NONE, COMMISSION, POWDER, ABILITY, STATS }
    private DragTarget dragging = DragTarget.NONE;
    private int dragOffsetX, dragOffsetY;
    
    public PositionScaleScreen(Screen parent) {
        super(Text.literal("Position & Scale Settings"));
        this.parent = parent;
    }
    
    @Override
    protected void init() {
        ConfigManager.Config cfg = CommissionHudMod.config.getConfig();
        
        int centerX = width / 2;
        int buttonWidth = 100;
        int buttonHeight = 20;
        int sliderWidth = 100;
        
        // Row 1: Commission and Powder
        int row1Y = height - 95;
        int leftX = centerX - 160;
        int rightX = centerX + 160;
        
        // Commission scale
        addDrawableChild(new SliderWidget(leftX - sliderWidth / 2, row1Y, sliderWidth, buttonHeight,
            Text.literal("Comm: " + String.format("%.1f", cfg.scale)), (cfg.scale - 0.5) / 1.5) {
            @Override
            protected void updateMessage() {
                cfg.scale = (float) (0.5 + value * 1.5);
                setMessage(Text.literal("Comm: " + String.format("%.1f", cfg.scale)));
            }
            @Override
            protected void applyValue() { CommissionHudMod.config.save(); }
        });
        addDrawableChild(ButtonWidget.builder(Text.literal("Reset"), button -> {
            CommissionHudMod.config.setPosition(10, 10);
        }).dimensions(leftX + sliderWidth / 2 + 5, row1Y, 60, buttonHeight).build());
        
        // Powder scale
        addDrawableChild(new SliderWidget(rightX - sliderWidth / 2, row1Y, sliderWidth, buttonHeight,
            Text.literal("Powder: " + String.format("%.1f", cfg.powderScale)), (cfg.powderScale - 0.5) / 1.5) {
            @Override
            protected void updateMessage() {
                cfg.powderScale = (float) (0.5 + value * 1.5);
                setMessage(Text.literal("Powder: " + String.format("%.1f", cfg.powderScale)));
            }
            @Override
            protected void applyValue() { CommissionHudMod.config.save(); }
        });
        addDrawableChild(ButtonWidget.builder(Text.literal("Reset"), button -> {
            CommissionHudMod.config.setPowderPosition(10, 150);
        }).dimensions(rightX + sliderWidth / 2 + 5, row1Y, 60, buttonHeight).build());
        
        // Row 2: Ability and Stats
        int row2Y = height - 70;
        
        // Ability scale
        addDrawableChild(new SliderWidget(leftX - sliderWidth / 2, row2Y, sliderWidth, buttonHeight,
            Text.literal("Ability: " + String.format("%.1f", cfg.abilityScale)), (cfg.abilityScale - 0.5) / 1.5) {
            @Override
            protected void updateMessage() {
                cfg.abilityScale = (float) (0.5 + value * 1.5);
                setMessage(Text.literal("Ability: " + String.format("%.1f", cfg.abilityScale)));
            }
            @Override
            protected void applyValue() { CommissionHudMod.config.save(); }
        });
        addDrawableChild(ButtonWidget.builder(Text.literal("Reset"), button -> {
            CommissionHudMod.config.setAbilityPosition(10, 200);
        }).dimensions(leftX + sliderWidth / 2 + 5, row2Y, 60, buttonHeight).build());
        
        // Stats scale
        addDrawableChild(new SliderWidget(rightX - sliderWidth / 2, row2Y, sliderWidth, buttonHeight,
            Text.literal("Stats: " + String.format("%.1f", cfg.statsScale)), (cfg.statsScale - 0.5) / 1.5) {
            @Override
            protected void updateMessage() {
                cfg.statsScale = (float) (0.5 + value * 1.5);
                setMessage(Text.literal("Stats: " + String.format("%.1f", cfg.statsScale)));
            }
            @Override
            protected void applyValue() { CommissionHudMod.config.save(); }
        });
        addDrawableChild(ButtonWidget.builder(Text.literal("Reset"), button -> {
            CommissionHudMod.config.setStatsPosition(10, 250);
        }).dimensions(rightX + sliderWidth / 2 + 5, row2Y, 60, buttonHeight).build());
        
        // Done button
        addDrawableChild(ButtonWidget.builder(Text.literal("Done"), button -> close())
            .dimensions(centerX - 50, height - 25, 100, buttonHeight)
            .build());
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 10, 0xFFFFFF);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("Drag previews to reposition"), width / 2, 22, 0xFF888888);
        
        ConfigManager.Config cfg = CommissionHudMod.config.getConfig();
        
        super.render(context, mouseX, mouseY, delta);
        
        // Render all previews
        renderCommissionPreview(context, cfg);
        renderPowderPreview(context, cfg);
        renderAbilityPreview(context, cfg);
        renderStatsPreview(context, cfg);
    }
    
    private void renderCommissionPreview(DrawContext context, ConfigManager.Config cfg) {
        // Get actual commission count (minimum 2 for preview)
        int commissionCount = Math.max(2, CommissionHudMod.commissionManager.getActiveCommissions().size());
        if (commissionCount > 4) commissionCount = 4; // Cap at 4
        
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
    
    private void renderPowderPreview(DrawContext context, ConfigManager.Config cfg) {
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
    
    private void renderAbilityPreview(DrawContext context, ConfigManager.Config cfg) {
        context.getMatrices().pushMatrix();
        context.getMatrices().translate(cfg.abilityX, cfg.abilityY);
        context.getMatrices().scale(cfg.abilityScale, cfg.abilityScale);
        
        context.drawText(textRenderer, Text.literal("Pickaxe Ability:"), 0, 0, cfg.abilityTitleColor, true);
        
        String nameLabel = "Pickobulus: ";
        String statusText = "Available";
        int nameLabelWidth = textRenderer.getWidth(nameLabel);
        context.drawText(textRenderer, Text.literal(nameLabel), 0, 12, cfg.abilityLabelColor, true);
        context.drawText(textRenderer, Text.literal(statusText), nameLabelWidth, 12, 0xFF55FF55, true);
        
        context.getMatrices().popMatrix();
    }
    
    private void renderStatsPreview(DrawContext context, ConfigManager.Config cfg) {
        context.getMatrices().pushMatrix();
        context.getMatrices().translate(cfg.statsX, cfg.statsY);
        context.getMatrices().scale(cfg.statsScale, cfg.statsScale);
        
        context.drawText(textRenderer, Text.literal("Stats:"), 0, 0, cfg.statsTitleColor, true);
        
        String miningLabel = "Mining Speed: ";
        String miningValue = "⸕1549";
        int miningLabelWidth = textRenderer.getWidth(miningLabel);
        context.drawText(textRenderer, Text.literal(miningLabel), 0, 12, cfg.statsLabelColor, true);
        context.drawText(textRenderer, Text.literal(miningValue), miningLabelWidth, 12, 0xFFAA00, true);
        
        String fortuneLabel = "Mining Fortune: ";
        String fortuneValue = "☘850";
        int fortuneLabelWidth = textRenderer.getWidth(fortuneLabel);
        context.drawText(textRenderer, Text.literal(fortuneLabel), 0, 22, cfg.statsLabelColor, true);
        context.drawText(textRenderer, Text.literal(fortuneValue), fortuneLabelWidth, 22, 0xFFAA00, true);
        
        context.getMatrices().popMatrix();
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        ConfigManager.Config cfg = CommissionHudMod.config.getConfig();
        
        // Get actual commission count for hitbox calculation
        int commissionCount = Math.max(2, CommissionHudMod.commissionManager.getActiveCommissions().size());
        if (commissionCount > 4) commissionCount = 4;
        int lineHeight = cfg.showPercentage ? 14 : 10;
        int commissionHeight = 12 + (commissionCount * lineHeight);
        
        // Check commission preview
        int commissionWidth = (int)(120 * cfg.scale);
        int scaledCommissionHeight = (int)(commissionHeight * cfg.scale);
        if (mouseX >= cfg.x && mouseX <= cfg.x + commissionWidth &&
            mouseY >= cfg.y && mouseY <= cfg.y + scaledCommissionHeight) {
            dragging = DragTarget.COMMISSION;
            dragOffsetX = (int)(mouseX - cfg.x);
            dragOffsetY = (int)(mouseY - cfg.y);
            return true;
        }
        
        // Check powder preview
        int powderWidth = (int)(120 * cfg.powderScale);
        int powderHeight = cfg.powderCalcEnabled ? (int)(55 * cfg.powderScale) : (int)(35 * cfg.powderScale);
        if (mouseX >= cfg.powderX && mouseX <= cfg.powderX + powderWidth &&
            mouseY >= cfg.powderY && mouseY <= cfg.powderY + powderHeight) {
            dragging = DragTarget.POWDER;
            dragOffsetX = (int)(mouseX - cfg.powderX);
            dragOffsetY = (int)(mouseY - cfg.powderY);
            return true;
        }
        
        // Check ability preview
        int abilityWidth = (int)(120 * cfg.abilityScale);
        int abilityHeight = (int)(25 * cfg.abilityScale);
        if (mouseX >= cfg.abilityX && mouseX <= cfg.abilityX + abilityWidth &&
            mouseY >= cfg.abilityY && mouseY <= cfg.abilityY + abilityHeight) {
            dragging = DragTarget.ABILITY;
            dragOffsetX = (int)(mouseX - cfg.abilityX);
            dragOffsetY = (int)(mouseY - cfg.abilityY);
            return true;
        }
        
        // Check stats preview
        int statsWidth = (int)(130 * cfg.statsScale);
        int statsHeight = (int)(35 * cfg.statsScale);
        if (mouseX >= cfg.statsX && mouseX <= cfg.statsX + statsWidth &&
            mouseY >= cfg.statsY && mouseY <= cfg.statsY + statsHeight) {
            dragging = DragTarget.STATS;
            dragOffsetX = (int)(mouseX - cfg.statsX);
            dragOffsetY = (int)(mouseY - cfg.statsY);
            return true;
        }
        
        return super.mouseClicked(mouseX, mouseY, button);
    }
    
    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        dragging = DragTarget.NONE;
        return super.mouseReleased(mouseX, mouseY, button);
    }
    
    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        switch (dragging) {
            case COMMISSION:
                CommissionHudMod.config.setPosition((int)mouseX - dragOffsetX, (int)mouseY - dragOffsetY);
                return true;
            case POWDER:
                CommissionHudMod.config.setPowderPosition((int)mouseX - dragOffsetX, (int)mouseY - dragOffsetY);
                return true;
            case ABILITY:
                CommissionHudMod.config.setAbilityPosition((int)mouseX - dragOffsetX, (int)mouseY - dragOffsetY);
                return true;
            case STATS:
                CommissionHudMod.config.setStatsPosition((int)mouseX - dragOffsetX, (int)mouseY - dragOffsetY);
                return true;
            default:
                return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
        }
    }
    
    @Override
    public void close() {
        if (client != null) {
            client.setScreen(parent);
        }
    }
}

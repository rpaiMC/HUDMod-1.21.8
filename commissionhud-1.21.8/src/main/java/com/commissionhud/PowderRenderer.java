package com.commissionhud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

public class PowderRenderer {
    public static void render(DrawContext context, PowderManager powderManager) {
        if (!powderManager.hasPowderData()) {
            return;
        }
        
        ConfigManager.Config cfg = CommissionHudMod.config.getConfig();
        MinecraftClient client = MinecraftClient.getInstance();
        
        context.getMatrices().pushMatrix();
        context.getMatrices().translate(cfg.powderX, cfg.powderY);
        context.getMatrices().scale(cfg.powderScale, cfg.powderScale);
        
        int y = 0;
        
        // Title
        context.drawText(client.textRenderer, Text.literal("Powder:"), 0, y, ensureAlpha(cfg.powderTitleColor), true);
        y += 12;
        
        // Mithril Powder
        String mithrilLabel = "Mithril: ";
        String mithrilValue = powderManager.getMithrilPowder();
        int mithrilLabelWidth = client.textRenderer.getWidth(mithrilLabel);
        context.drawText(client.textRenderer, Text.literal(mithrilLabel), 0, y, ensureAlpha(cfg.powderLabelColor), true);
        context.drawText(client.textRenderer, Text.literal(mithrilValue), mithrilLabelWidth, y, ensureAlpha(cfg.powderValueColor), true);
        y += 10;
        
        // Gemstone Powder
        String gemstoneLabel = "Gemstone: ";
        String gemstoneValue = powderManager.getGemstonePowder();
        int gemstoneLabelWidth = client.textRenderer.getWidth(gemstoneLabel);
        context.drawText(client.textRenderer, Text.literal(gemstoneLabel), 0, y, ensureAlpha(cfg.powderLabelColor), true);
        context.drawText(client.textRenderer, Text.literal(gemstoneValue), gemstoneLabelWidth, y, ensureAlpha(cfg.powderValueColor), true);
        y += 10;
        
        // Glacite Powder (only show if non-zero)
        String glaciteValue = powderManager.getGlacitePowder();
        boolean hasGlacite = !glaciteValue.equals("0");
        if (hasGlacite) {
            String glaciteLabel = "Glacite: ";
            int glaciteLabelWidth = client.textRenderer.getWidth(glaciteLabel);
            context.drawText(client.textRenderer, Text.literal(glaciteLabel), 0, y, ensureAlpha(cfg.powderLabelColor), true);
            context.drawText(client.textRenderer, Text.literal(glaciteValue), glaciteLabelWidth, y, ensureAlpha(cfg.powderValueColor), true);
            y += 10;
        }
        
        // Powder Calculator rates (only show when enabled and tracking)
        if (cfg.powderCalcEnabled && powderManager.isTracking()) {
            int interval = cfg.powderCalcInterval;
            String intervalLabel = CommissionHudMod.config.getPowderCalcIntervalLabel();
            
            // Show rates after a brief delay (1 second minimum)
            if (powderManager.getTrackingDurationMs() >= 1000) {
                // Mithril rate
                String mithrilRateLabel = "Mithril" + intervalLabel + ": ";
                String mithrilRateValue = "+" + powderManager.getMithrilRate(interval);
                int mithrilRateLabelWidth = client.textRenderer.getWidth(mithrilRateLabel);
                context.drawText(client.textRenderer, Text.literal(mithrilRateLabel), 0, y, ensureAlpha(cfg.powderLabelColor), true);
                context.drawText(client.textRenderer, Text.literal(mithrilRateValue), mithrilRateLabelWidth, y, 0xFF55FF55, true);
                y += 10;
                
                // Gemstone rate
                String gemstoneRateLabel = "Gemstone" + intervalLabel + ": ";
                String gemstoneRateValue = "+" + powderManager.getGemstoneRate(interval);
                int gemstoneRateLabelWidth = client.textRenderer.getWidth(gemstoneRateLabel);
                context.drawText(client.textRenderer, Text.literal(gemstoneRateLabel), 0, y, ensureAlpha(cfg.powderLabelColor), true);
                context.drawText(client.textRenderer, Text.literal(gemstoneRateValue), gemstoneRateLabelWidth, y, 0xFF55FF55, true);
                y += 10;
                
                // Glacite rate (only show if glacite powder exists)
                if (hasGlacite) {
                    String glaciteRateLabel = "Glacite" + intervalLabel + ": ";
                    String glaciteRateValue = "+" + powderManager.getGlaciteRate(interval);
                    int glaciteRateLabelWidth = client.textRenderer.getWidth(glaciteRateLabel);
                    context.drawText(client.textRenderer, Text.literal(glaciteRateLabel), 0, y, ensureAlpha(cfg.powderLabelColor), true);
                    context.drawText(client.textRenderer, Text.literal(glaciteRateValue), glaciteRateLabelWidth, y, 0xFF55FF55, true);
                }
            }
        }
        
        context.getMatrices().popMatrix();
    }
    
    // Ensure color has proper alpha channel for 1.21.6+ ARGB format
    private static int ensureAlpha(int color) {
        if ((color & 0xFF000000) == 0) {
            return color | 0xFF000000;
        }
        return color;
    }
}

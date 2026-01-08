package com.commissionhud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import java.util.List;

public class CommissionRenderer {
    public static void render(DrawContext context, List<CommissionManager.Commission> commissions) {
        if (commissions.isEmpty()) {
            return;
        }
        
        ConfigManager.Config cfg = CommissionHudMod.config.getConfig();
        MinecraftClient client = MinecraftClient.getInstance();
        
        context.getMatrices().pushMatrix();
        context.getMatrices().translate(cfg.x, cfg.y);
        context.getMatrices().scale(cfg.scale, cfg.scale);
        
        int y = 0;
        
        // Title
        context.drawText(client.textRenderer, Text.literal("Commissions:"), 0, y, cfg.titleColor, true);
        y += 12;
        
        // Draw each commission
        for (CommissionManager.Commission commission : commissions) {
            String text = "• " + commission.name;
            if (cfg.showPercentage) {
                if (cfg.progressFormat == ConfigManager.ProgressFormat.PERCENTAGE) {
                    text += ": " + commission.percentage + "%";
                } else {
                    text += ": " + commission.current + "/" + commission.total;
                }
            }
            
            // Color based on completion
            int color = cfg.color;
            if (commission.percentage >= 100) {
                color = 0xFF55FF55; // Green for complete
            } else if (commission.percentage >= 75) {
                color = 0xFFFFFF55; // Yellow for almost done
            }
            
            context.drawText(client.textRenderer, Text.literal(text), 0, y, color, true);
            
            // Draw progress bar
            if (cfg.showPercentage) {
                int barWidth = 100;
                int barHeight = 2;
                int barY = y + 9;
                
                // Background
                context.fill(0, barY, barWidth, barY + barHeight, 0x88000000);
                
                // Progress - use custom color, green override for completed
                int progressWidth = (int) (barWidth * (commission.percentage / 100.0f));
                int progressColor = commission.percentage >= 100 ? 0xFF55FF55 : cfg.progressBarColor;
                context.fill(0, barY, progressWidth, barY + barHeight, progressColor);
            }
            
            y += cfg.showPercentage ? 14 : 10;
        }
        
        context.getMatrices().popMatrix();
    }
}

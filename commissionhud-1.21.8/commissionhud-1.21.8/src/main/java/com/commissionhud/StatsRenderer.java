package com.commissionhud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import java.util.Map;

public class StatsRenderer {
    
    // Common stat icons to check for duplicates
    private static final String[] STAT_ICONS = {
        "⸕", "☘", "✧", "❤", "❈", "❂", "❁", "✦", "☣", "☠", 
        "⚔", "✎", "⫽", "๑", "✯", "♣", "α", "☂", "⸎", "ʬ",
        "♨", "❣", "☯", "Ⓟ"
    };
    
    public static void render(DrawContext context, StatsManager statsManager) {
        if (!statsManager.hasStats()) {
            return;
        }
        
        ConfigManager.Config cfg = CommissionHudMod.config.getConfig();
        MinecraftClient client = MinecraftClient.getInstance();
        
        context.getMatrices().pushMatrix();
        context.getMatrices().translate(cfg.statsX, cfg.statsY);
        context.getMatrices().scale(cfg.statsScale, cfg.statsScale);
        
        int y = 0;
        
        // Title
        context.drawText(client.textRenderer, Text.literal("Stats:"), 0, y, cfg.statsTitleColor, true);
        y += 12;
        
        // Render each stat
        Map<String, String> stats = statsManager.getStats();
        for (Map.Entry<String, String> entry : stats.entrySet()) {
            String statName = entry.getKey();
            String statValue = entry.getValue();
            
            // Get color for this stat
            int statColor = statsManager.getColorForStat(statName);
            
            // Check if value already contains an icon - if so, don't add another
            String valueText = statValue;
            if (!containsIcon(statValue)) {
                String icon = statsManager.getIconForStat(statName);
                if (!icon.isEmpty()) {
                    valueText = icon + statValue;
                }
            }
            
            // Build display string
            String labelText = statName + ": ";
            
            int labelWidth = client.textRenderer.getWidth(labelText);
            
            // Draw label in configured color
            context.drawText(client.textRenderer, Text.literal(labelText), 0, y, cfg.statsLabelColor, true);
            
            // Draw value with stat-specific color
            context.drawText(client.textRenderer, Text.literal(valueText), labelWidth, y, statColor, true);
            
            y += 10;
        }
        
        context.getMatrices().popMatrix();
    }
    
    private static boolean containsIcon(String text) {
        for (String icon : STAT_ICONS) {
            if (text.contains(icon)) {
                return true;
            }
        }
        return false;
    }
}

package com.commissionhud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

public class PickaxeAbilityRenderer {
    public static void render(DrawContext context, PickaxeAbilityManager abilityManager) {
        if (!abilityManager.hasAbilityData()) {
            return;
        }
        
        ConfigManager.Config cfg = CommissionHudMod.config.getConfig();
        MinecraftClient client = MinecraftClient.getInstance();
        
        context.getMatrices().pushMatrix();
        context.getMatrices().translate(cfg.abilityX, cfg.abilityY);
        context.getMatrices().scale(cfg.abilityScale, cfg.abilityScale);
        
        int y = 0;
        
        // Title
        context.drawText(client.textRenderer, Text.literal("Pickaxe Ability:"), 0, y, cfg.abilityTitleColor, true);
        y += 12;
        
        // Ability name and status
        String nameText = abilityManager.getAbilityName() + ": ";
        String statusText = abilityManager.getAbilityStatus();
        
        int nameWidth = client.textRenderer.getWidth(nameText);
        
        // Draw ability name
        context.drawText(client.textRenderer, Text.literal(nameText), 0, y, cfg.abilityLabelColor, true);
        
        // Draw status with color based on availability
        int statusColor = abilityManager.isAvailable() ? 0xFF55FF55 : cfg.abilityValueColor; // Green if available
        context.drawText(client.textRenderer, Text.literal(statusText), nameWidth, y, statusColor, true);
        
        context.getMatrices().popMatrix();
    }
}

package com.commissionhud;

import com.mojang.brigadier.Command;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class CommissionHudMod implements ClientModInitializer {
    public static final String MOD_ID = "commissionhud";
    public static final ConfigManager config = new ConfigManager();
    public static final CommissionManager commissionManager = new CommissionManager();
    public static final LocationDetector locationDetector = new LocationDetector();
    public static final PowderManager powderManager = new PowderManager();
    public static final PickaxeAbilityManager abilityManager = new PickaxeAbilityManager();
    public static final StatsManager statsManager = new StatsManager();
    
    @Override
    public void onInitializeClient() {
        config.load();
        
        // Register command
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(literal("chud")
                .executes(context -> {
                    MinecraftClient client = MinecraftClient.getInstance();
                    client.send(() -> {
                        client.setScreen(new ConfigScreen());
                    });
                    return Command.SINGLE_SUCCESS;
                }));
        });
        
        // Register HUD rendering using HudRenderCallback (1.21.8 API)
        HudRenderCallback.EVENT.register((context, tickCounter) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.world == null) return;
            
            // Don't render if a config screen is open
            if (!shouldDisplayHud()) {
                return;
            }
            
            // Check if any config screen is open
            if (client.currentScreen instanceof ConfigScreen || 
                client.currentScreen instanceof PowderConfigScreen ||
                client.currentScreen instanceof AbilityConfigScreen ||
                client.currentScreen instanceof StatsConfigScreen ||
                client.currentScreen instanceof PositionScaleScreen) {
                return;
            }
            
            // Commission HUD
            if (config.isEnabled()) {
                CommissionRenderer.render(context, commissionManager.getActiveCommissions());
            }
            
            // Powder HUD
            powderManager.update();
            if (config.isPowderEnabled()) {
                PowderRenderer.render(context, powderManager);
            }
            
            // Ability HUD
            abilityManager.update();
            if (config.isAbilityEnabled()) {
                PickaxeAbilityRenderer.render(context, abilityManager);
            }
            
            // Stats HUD
            statsManager.update();
            if (config.isStatsEnabled()) {
                StatsRenderer.render(context, statsManager);
            }
        });
        
        System.out.println("CommissionHud mod initialized! (1.21.8)");
    }
    
    private boolean shouldDisplayHud() {
        ConfigManager.Config cfg = config.getConfig();
        
        if (cfg.displayMode == ConfigManager.DisplayMode.EVERYWHERE) {
            return true;
        }
        
        // Mining Islands Only mode
        return locationDetector.isInMiningIsland();
    }
}

package com.commissionhud.mixin;

import com.commissionhud.ColorPickerScreen;
import com.commissionhud.ConfigScreen;
import com.commissionhud.PowderConfigScreen;
import com.commissionhud.AbilityConfigScreen;
import com.commissionhud.StatsConfigScreen;
import com.commissionhud.PositionScaleScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public class ScreenMixin {
    
    @Inject(method = "applyBlur", at = @At("HEAD"), cancellable = true)
    private void commissionhud$disableBlurForColorPicker(CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.currentScreen instanceof ColorPickerScreen || 
            client.currentScreen instanceof ConfigScreen ||
            client.currentScreen instanceof PowderConfigScreen ||
            client.currentScreen instanceof AbilityConfigScreen ||
            client.currentScreen instanceof StatsConfigScreen ||
            client.currentScreen instanceof PositionScaleScreen) {
            ci.cancel();
        }
    }
}

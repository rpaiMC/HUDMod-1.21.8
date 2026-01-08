package com.commissionhud;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.*;
import java.nio.file.*;

public class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = Paths.get("config", "commissionhud.json");
    
    private Config config = new Config();
    
    public enum DisplayMode {
        EVERYWHERE("Everywhere"),
        MINING_ISLANDS_ONLY("Mining Islands Only");
        
        private final String displayName;
        
        DisplayMode(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public DisplayMode next() {
            DisplayMode[] values = values();
            return values[(this.ordinal() + 1) % values.length];
        }
    }
    
    public void load() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            if (Files.exists(CONFIG_PATH)) {
                String json = Files.readString(CONFIG_PATH);
                config = GSON.fromJson(json, Config.class);
                // Handle null values from old configs
                if (config.displayMode == null) {
                    config.displayMode = DisplayMode.EVERYWHERE;
                }
                if (config.progressFormat == null) {
                    config.progressFormat = ProgressFormat.PERCENTAGE;
                }
            } else {
                save();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void save() {
        try {
            Files.writeString(CONFIG_PATH, GSON.toJson(config));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public Config getConfig() { return config; }
    public boolean isEnabled() { return config.enabled; }
    public float getScale() { return config.scale; }
    public int getX() { return config.x; }
    public int getY() { return config.y; }
    public int getColor() { return config.color; }
    public boolean showPercentage() { return config.showPercentage; }
    public DisplayMode getDisplayMode() { return config.displayMode; }
    
    public void setEnabled(boolean enabled) { config.enabled = enabled; save(); }
    public void setScale(float scale) { config.scale = scale; save(); }
    public void setPosition(int x, int y) { config.x = x; config.y = y; save(); }
    public int getTitleColor() { return config.titleColor; }
    public void setTitleColor(int color) { config.titleColor = color; save(); }
    public void setColor(int color) { config.color = color; save(); }
    public void setShowPercentage(boolean show) { config.showPercentage = show; save(); }
    public int getProgressBarColor() { return config.progressBarColor; }
    public void setProgressBarColor(int color) { config.progressBarColor = color; save(); }
    public void setDisplayMode(DisplayMode mode) { config.displayMode = mode; save(); }
    
    public void cycleDisplayMode() {
        config.displayMode = config.displayMode.next();
        save();
    }
    
    public ProgressFormat getProgressFormat() { return config.progressFormat; }
    public void setProgressFormat(ProgressFormat format) { config.progressFormat = format; save(); }
    
    public void cycleProgressFormat() {
        config.progressFormat = config.progressFormat.next();
        save();
    }
    
    // Powder settings
    public boolean isPowderEnabled() { return config.powderEnabled; }
    public void setPowderEnabled(boolean enabled) { config.powderEnabled = enabled; save(); }
    public int getPowderX() { return config.powderX; }
    public int getPowderY() { return config.powderY; }
    public void setPowderPosition(int x, int y) { config.powderX = x; config.powderY = y; save(); }
    public float getPowderScale() { return config.powderScale; }
    public void setPowderScale(float scale) { config.powderScale = scale; save(); }
    public int getPowderTitleColor() { return config.powderTitleColor; }
    public void setPowderTitleColor(int color) { config.powderTitleColor = color; save(); }
    public int getPowderLabelColor() { return config.powderLabelColor; }
    public void setPowderLabelColor(int color) { config.powderLabelColor = color; save(); }
    public int getPowderValueColor() { return config.powderValueColor; }
    public void setPowderValueColor(int color) { config.powderValueColor = color; save(); }
    
    // Powder calculator settings
    public boolean isPowderCalcEnabled() { return config.powderCalcEnabled; }
    public void setPowderCalcEnabled(boolean enabled) { config.powderCalcEnabled = enabled; save(); }
    public int getPowderCalcInterval() { return config.powderCalcInterval; }
    public void setPowderCalcInterval(int minutes) { config.powderCalcInterval = minutes; save(); }
    
    public void cyclePowderCalcInterval() {
        // Cycle through: 10 -> 30 -> 60 -> 10
        if (config.powderCalcInterval == 10) {
            config.powderCalcInterval = 30;
        } else if (config.powderCalcInterval == 30) {
            config.powderCalcInterval = 60;
        } else {
            config.powderCalcInterval = 10;
        }
        save();
    }
    
    public String getPowderCalcIntervalLabel() {
        if (config.powderCalcInterval == 60) {
            return "/hr";
        } else {
            return "/" + config.powderCalcInterval + "m";
        }
    }
    
    // Pickaxe ability settings
    public boolean isAbilityEnabled() { return config.abilityEnabled; }
    public void setAbilityEnabled(boolean enabled) { config.abilityEnabled = enabled; save(); }
    public int getAbilityX() { return config.abilityX; }
    public int getAbilityY() { return config.abilityY; }
    public void setAbilityPosition(int x, int y) { config.abilityX = x; config.abilityY = y; save(); }
    public float getAbilityScale() { return config.abilityScale; }
    public void setAbilityScale(float scale) { config.abilityScale = scale; save(); }
    public int getAbilityTitleColor() { return config.abilityTitleColor; }
    public void setAbilityTitleColor(int color) { config.abilityTitleColor = color; save(); }
    public int getAbilityLabelColor() { return config.abilityLabelColor; }
    public void setAbilityLabelColor(int color) { config.abilityLabelColor = color; save(); }
    public int getAbilityValueColor() { return config.abilityValueColor; }
    public void setAbilityValueColor(int color) { config.abilityValueColor = color; save(); }
    
    // Stats display settings
    public boolean isStatsEnabled() { return config.statsEnabled; }
    public void setStatsEnabled(boolean enabled) { config.statsEnabled = enabled; save(); }
    public int getStatsX() { return config.statsX; }
    public int getStatsY() { return config.statsY; }
    public void setStatsPosition(int x, int y) { config.statsX = x; config.statsY = y; save(); }
    public float getStatsScale() { return config.statsScale; }
    public void setStatsScale(float scale) { config.statsScale = scale; save(); }
    public int getStatsTitleColor() { return config.statsTitleColor; }
    public void setStatsTitleColor(int color) { config.statsTitleColor = color; save(); }
    public int getStatsLabelColor() { return config.statsLabelColor; }
    public void setStatsLabelColor(int color) { config.statsLabelColor = color; save(); }
    
    public static class Config {
        public boolean enabled = true;
        public float scale = 1.0f;
        public int x = 10;
        public int y = 10;
        // All colors in ARGB format for 1.21.6+ compatibility
        // Using (int) cast for values > 0x7FFFFFFF
        public int titleColor = (int) 0xFFFFFFFF; // White - for "Commissions:" title
        public int color = (int) 0xFFFFFFFF; // White - for commission text
        public int progressBarColor = (int) 0xFFFFAA00; // Gold/Orange
        public boolean showPercentage = true;
        public DisplayMode displayMode = DisplayMode.EVERYWHERE;
        public ProgressFormat progressFormat = ProgressFormat.PERCENTAGE;
        
        // Powder display settings
        public boolean powderEnabled = true;
        public int powderX = 10;
        public int powderY = 150;
        public float powderScale = 1.0f;
        public int powderTitleColor = (int) 0xFFFFFFFF; // White
        public int powderLabelColor = (int) 0xFFAAAAAA; // Gray
        public int powderValueColor = (int) 0xFF55FFFF; // Cyan
        
        // Powder calculator settings
        public boolean powderCalcEnabled = false;
        public int powderCalcInterval = 60; // Default to 1 hour (60 minutes)
        
        // Pickaxe ability display settings
        public boolean abilityEnabled = true;
        public int abilityX = 10;
        public int abilityY = 200;
        public float abilityScale = 1.0f;
        public int abilityTitleColor = (int) 0xFFFFFFFF; // White
        public int abilityLabelColor = (int) 0xFFAAAAAA; // Gray
        public int abilityValueColor = (int) 0xFFFF5555; // Red for cooldown
        
        // Stats display settings
        public boolean statsEnabled = true;
        public int statsX = 10;
        public int statsY = 250;
        public float statsScale = 1.0f;
        public int statsTitleColor = (int) 0xFFFFAA00; // Gold (matches in-game)
        public int statsLabelColor = (int) 0xFFAAAAAA; // Gray
    }
    
    public enum ProgressFormat {
        PERCENTAGE("Percentage (20%)"),
        FRACTION("Fraction (20/100)");
        
        private final String displayName;
        
        ProgressFormat(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public ProgressFormat next() {
            ProgressFormat[] values = values();
            return values[(this.ordinal() + 1) % values.length];
        }
    }
}

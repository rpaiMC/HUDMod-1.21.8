package com.commissionhud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardEntry;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.text.Text;
import java.util.Collection;
import java.util.regex.Pattern;

public class LocationDetector {
    
    // Mining island identifiers that appear in scoreboard or tab list
    private static final String[] MINING_LOCATIONS = {
        "Dwarven Mines",
        "Crystal Hollows",
        "Glacite Tunnels",
        "Glacite Mineshafts",
        "Mineshaft",
        "Dwarven Base Camp",
        "The Lift",
        "Goblin Burrows",
        "Palace Bridge",
        "Royal Palace",
        "Grand Library",
        "Forge Basin",
        "Far Reserve",
        "Upper Mines",
        "Royal Mines",
        "Cliffside Veins",
        "Rampart's Quarry",
        "Miner's Mountain",
        "Great Ice Wall",
        "Hanging Court",
        "Aristocrat Passage",
        "Lava Springs",
        "The Mist",
        "Miners Guild",
        // Crystal Hollows areas
        "Jungle",
        "Mithril Deposits",
        "Goblin Holdout",
        "Precursor Remnants",
        "Magma Fields",
        "Crystal Nucleus",
        "Fairy Grotto",
        "Khazad-dûm",
        "Lost Precursor City",
        "Jungle Temple",
        "Goblin Queen's Den",
        "Mines of Divan",
        // Glacite areas
        "Glacite Lake",
        "Fossil Research Center",
        "Tungsten Mines",
        "Umber Mines",
        "Base Camp"
    };
    
    private static final Pattern LOCATION_PATTERN = Pattern.compile("⏣\\s*(.+)");
    
    private String currentLocation = "";
    private long lastCheck = 0;
    private static final long CHECK_INTERVAL = 1000; // Check every 1 second
    private boolean cachedInMiningIsland = false;
    
    public boolean isInMiningIsland() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastCheck > CHECK_INTERVAL) {
            updateLocation();
            lastCheck = currentTime;
        }
        return cachedInMiningIsland;
    }
    
    public String getCurrentLocation() {
        return currentLocation;
    }
    
    private void updateLocation() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null || client.player == null) {
            cachedInMiningIsland = false;
            currentLocation = "";
            return;
        }
        
        // Method 1: Check scoreboard for location
        String scoreboardLocation = getLocationFromScoreboard();
        if (!scoreboardLocation.isEmpty()) {
            currentLocation = scoreboardLocation;
            cachedInMiningIsland = checkIfMiningLocation(scoreboardLocation);
            return;
        }
        
        // Method 2: Check tab list for location/area info
        String tabLocation = getLocationFromTabList();
        if (!tabLocation.isEmpty()) {
            currentLocation = tabLocation;
            cachedInMiningIsland = checkIfMiningLocation(tabLocation);
            return;
        }
        
        // If we can't determine location, default to not in mining island
        cachedInMiningIsland = false;
    }
    
    private String getLocationFromScoreboard() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) return "";
        
        Scoreboard scoreboard = client.world.getScoreboard();
        if (scoreboard == null) return "";
        
        // Look for the sidebar objective (usually where Hypixel displays info)
        ScoreboardObjective sidebarObjective = scoreboard.getObjectiveForSlot(net.minecraft.scoreboard.ScoreboardDisplaySlot.SIDEBAR);
        if (sidebarObjective == null) return "";
        
        // Get all score entries
        Collection<ScoreboardEntry> entries = scoreboard.getScoreboardEntries(sidebarObjective);
        
        for (ScoreboardEntry entry : entries) {
            // Get the display name from the entry
            String line = entry.name().getString();
            
            // Clean up color codes
            String cleanLine = line.replaceAll("§.", "").trim();
            
            // Look for location indicator (⏣ symbol)
            if (cleanLine.contains("⏣")) {
                // Extract location name after the symbol
                int index = cleanLine.indexOf("⏣");
                if (index >= 0 && index < cleanLine.length() - 1) {
                    return cleanLine.substring(index + 1).trim();
                }
            }
            
            // Also check for area names directly
            for (String location : MINING_LOCATIONS) {
                if (cleanLine.toLowerCase().contains(location.toLowerCase())) {
                    return location;
                }
            }
        }
        
        return "";
    }
    
    private String getLocationFromTabList() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.getNetworkHandler() == null) return "";
        
        Collection<PlayerListEntry> playerList = client.getNetworkHandler().getPlayerList();
        
        for (PlayerListEntry entry : playerList) {
            if (entry.getDisplayName() == null) continue;
            
            String displayText = entry.getDisplayName().getString();
            String cleanText = displayText.replaceAll("§.", "").trim();
            
            // Look for location indicator
            if (cleanText.contains("⏣")) {
                int index = cleanText.indexOf("⏣");
                if (index >= 0 && index < cleanText.length() - 1) {
                    return cleanText.substring(index + 1).trim();
                }
            }
            
            // Check for "Area:" prefix
            if (cleanText.toLowerCase().startsWith("area:")) {
                return cleanText.substring(5).trim();
            }
            
            // Check for mining location names
            for (String location : MINING_LOCATIONS) {
                if (cleanText.equalsIgnoreCase(location) || 
                    cleanText.toLowerCase().contains(location.toLowerCase())) {
                    return location;
                }
            }
        }
        
        return "";
    }
    
    private boolean checkIfMiningLocation(String location) {
        if (location == null || location.isEmpty()) {
            return false;
        }
        
        String lowerLocation = location.toLowerCase();
        
        // Check against known mining locations
        for (String miningLocation : MINING_LOCATIONS) {
            if (lowerLocation.contains(miningLocation.toLowerCase())) {
                return true;
            }
        }
        
        // Additional checks for generic mining area indicators
        if (lowerLocation.contains("mine") || 
            lowerLocation.contains("hollows") ||
            lowerLocation.contains("glacite") ||
            lowerLocation.contains("dwarven") ||
            lowerLocation.contains("crystal")) {
            return true;
        }
        
        return false;
    }
}

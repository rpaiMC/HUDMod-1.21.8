package com.commissionhud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import java.util.*;

public class StatsManager {
    private long lastUpdate = 0;
    private static final long UPDATE_INTERVAL = 500; // Update every 0.5 seconds
    
    // Map of stat name to value (e.g., "Mining Speed" -> "1549")
    private Map<String, String> stats = new LinkedHashMap<>();
    
    // Map of stat names to their icons
    private static final Map<String, String> STAT_ICONS = new HashMap<>();
    
    static {
        // Combat stats
        STAT_ICONS.put("Health", "❤");
        STAT_ICONS.put("Defense", "❈");
        STAT_ICONS.put("True Defense", "❂");
        STAT_ICONS.put("Strength", "❁");
        STAT_ICONS.put("Speed", "✦");
        STAT_ICONS.put("Crit Chance", "☣");
        STAT_ICONS.put("Crit Damage", "☠");
        STAT_ICONS.put("Bonus Attack Speed", "⚔");
        STAT_ICONS.put("Attack Speed", "⚔");
        STAT_ICONS.put("Intelligence", "✎");
        STAT_ICONS.put("Ferocity", "⫽");
        STAT_ICONS.put("Ability Damage", "๑");
        STAT_ICONS.put("Magic Find", "✯");
        STAT_ICONS.put("Pet Luck", "♣");
        STAT_ICONS.put("Sea Creature Chance", "α");
        
        // Mining stats
        STAT_ICONS.put("Mining Speed", "⸕");
        STAT_ICONS.put("Mining Fortune", "☘");
        STAT_ICONS.put("Pristine", "✧");
        STAT_ICONS.put("Breaking Power", "Ⓟ");
        
        // Farming stats
        STAT_ICONS.put("Farming Fortune", "☘");
        
        // Foraging stats
        STAT_ICONS.put("Foraging Fortune", "☘");
        
        // Fishing stats
        STAT_ICONS.put("Fishing Speed", "☂");
        
        // Misc stats
        STAT_ICONS.put("Soulflow", "⸎");
        STAT_ICONS.put("Overflow Mana", "ʬ");
        STAT_ICONS.put("Mending", "☄");
        STAT_ICONS.put("Vitality", "♨");
        STAT_ICONS.put("Health Regen", "❣");
        STAT_ICONS.put("Wisdom", "☯");
    }
    
    // Map of stat names to their colors (as hex)
    private static final Map<String, Integer> STAT_COLORS = new HashMap<>();
    
    static {
        // Combat stats - all with 0xFF alpha prefix for ARGB format (1.21.6+)
        // Using (int) cast for values > 0x7FFFFFFF
        STAT_COLORS.put("Health", (int) 0xFFFF5555); // Red
        STAT_COLORS.put("Defense", (int) 0xFF55FF55); // Green
        STAT_COLORS.put("True Defense", (int) 0xFFFFFFFF); // White
        STAT_COLORS.put("Strength", (int) 0xFFFF5555); // Red
        STAT_COLORS.put("Speed", (int) 0xFFFFFFFF); // White
        STAT_COLORS.put("Crit Chance", (int) 0xFF5555FF); // Blue
        STAT_COLORS.put("Crit Damage", (int) 0xFF5555FF); // Blue
        STAT_COLORS.put("Bonus Attack Speed", (int) 0xFFFFFF55); // Yellow
        STAT_COLORS.put("Attack Speed", (int) 0xFFFFFF55); // Yellow
        STAT_COLORS.put("Intelligence", (int) 0xFF55FFFF); // Aqua
        STAT_COLORS.put("Ferocity", (int) 0xFFFF5555); // Red
        STAT_COLORS.put("Ability Damage", (int) 0xFFFF5555); // Red
        STAT_COLORS.put("Magic Find", (int) 0xFF55FFFF); // Aqua
        STAT_COLORS.put("Pet Luck", (int) 0xFFFF55FF); // Light Purple
        STAT_COLORS.put("Sea Creature Chance", (int) 0xFF00AAAA); // Dark Aqua
        
        // Mining stats
        STAT_COLORS.put("Mining Speed", (int) 0xFFFFAA00); // Gold
        STAT_COLORS.put("Mining Fortune", (int) 0xFFFFAA00); // Gold
        STAT_COLORS.put("Pristine", (int) 0xFFAA00AA); // Dark Purple
        STAT_COLORS.put("Breaking Power", (int) 0xFF00AA00); // Dark Green
        
        // Farming stats
        STAT_COLORS.put("Farming Fortune", (int) 0xFFFFAA00); // Gold
        
        // Foraging stats
        STAT_COLORS.put("Foraging Fortune", (int) 0xFFFFAA00); // Gold
        
        // Fishing stats
        STAT_COLORS.put("Fishing Speed", (int) 0xFF55FFFF); // Aqua
        
        // Misc stats
        STAT_COLORS.put("Soulflow", (int) 0xFF00AAAA); // Dark Aqua
        STAT_COLORS.put("Overflow Mana", (int) 0xFF00AAAA); // Dark Aqua
        STAT_COLORS.put("Mending", (int) 0xFF55FF55); // Green
        STAT_COLORS.put("Vitality", (int) 0xFFAA0000); // Dark Red
        STAT_COLORS.put("Health Regen", (int) 0xFFFF5555); // Red
        STAT_COLORS.put("Wisdom", (int) 0xFF00AAAA); // Dark Aqua
    }
    
    public void update() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastUpdate < UPDATE_INTERVAL) {
            return;
        }
        lastUpdate = currentTime;
        
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null || client.player == null || client.getNetworkHandler() == null) {
            return;
        }
        
        Collection<PlayerListEntry> playerList = client.getNetworkHandler().getPlayerList();
        
        // Convert to list and sort to maintain consistent order
        List<PlayerListEntry> sortedList = new ArrayList<>(playerList);
        sortedList.sort((a, b) -> {
            String nameA = a.getProfile().getName();
            String nameB = b.getProfile().getName();
            return nameA.compareTo(nameB);
        });
        
        // Build a list of clean text entries
        List<String> tabLines = new ArrayList<>();
        for (PlayerListEntry entry : sortedList) {
            if (entry.getDisplayName() == null) {
                tabLines.add("");
                continue;
            }
            String displayText = entry.getDisplayName().getString();
            String cleanText = displayText.replaceAll("§[0-9a-fk-or]", "").trim();
            tabLines.add(cleanText);
        }
        
        // Find the Stats header index
        int statsIndex = -1;
        for (int i = 0; i < tabLines.size(); i++) {
            String line = tabLines.get(i).toLowerCase();
            if (line.startsWith("stats") || line.equals("stats:")) {
                statsIndex = i;
                break;
            }
        }
        
        if (statsIndex == -1) {
            return; // No stats section found
        }
        
        // Clear old stats
        stats.clear();
        
        // Read lines after "Stats:" header until we hit an empty line or another section
        for (int i = statsIndex + 1; i < tabLines.size() && i < statsIndex + 15; i++) {
            String line = tabLines.get(i);
            
            // Stop if we hit an empty line or another section header
            if (line.isEmpty() || isNewSection(line)) {
                break;
            }
            
            // Parse stat line - format is usually "StatName: IconValue" or "StatName: Value"
            // Example: "Mining Speed: ⸕1549" or "Mining Speed: 1549"
            int colonIndex = line.indexOf(':');
            if (colonIndex > 0) {
                String statName = line.substring(0, colonIndex).trim();
                String statValue = line.substring(colonIndex + 1).trim();
                
                // Remove any icon characters from the value to get just the number
                // But keep the original for display
                if (!statName.isEmpty() && !statValue.isEmpty()) {
                    stats.put(statName, statValue);
                }
            }
        }
    }
    
    private boolean isNewSection(String line) {
        String lower = line.toLowerCase();
        return lower.endsWith(":") && !lower.contains(" ") && line.length() < 20;
    }
    
    public Map<String, String> getStats() {
        return stats;
    }
    
    public boolean hasStats() {
        return !stats.isEmpty();
    }
    
    public String getIconForStat(String statName) {
        // First try exact match
        if (STAT_ICONS.containsKey(statName)) {
            return STAT_ICONS.get(statName);
        }
        
        // Then try partial match
        for (Map.Entry<String, String> entry : STAT_ICONS.entrySet()) {
            if (statName.toLowerCase().contains(entry.getKey().toLowerCase())) {
                return entry.getValue();
            }
        }
        
        return ""; // No icon found
    }
    
    public int getColorForStat(String statName) {
        // First try exact match
        if (STAT_COLORS.containsKey(statName)) {
            return STAT_COLORS.get(statName);
        }
        
        // Then try partial match
        for (Map.Entry<String, Integer> entry : STAT_COLORS.entrySet()) {
            if (statName.toLowerCase().contains(entry.getKey().toLowerCase())) {
                return entry.getValue();
            }
        }
        
        return (int) 0xFFAAAAAA; // Default gray (ARGB format)
    }
    
    public void clear() {
        stats.clear();
    }
}

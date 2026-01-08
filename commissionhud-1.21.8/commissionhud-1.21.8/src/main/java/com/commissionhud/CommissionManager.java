package com.commissionhud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import java.util.*;
import java.util.regex.*;

public class CommissionManager {
    private List<Commission> activeCommissions = new ArrayList<>();
    private long lastUpdate = 0;
    private static final long UPDATE_INTERVAL = 500; // Update every 0.5 seconds
    
    // General pattern to match ANY line with "text: XX%" format
    // This will catch all commission formats including location-prefixed ones
    private static final Pattern PERCENTAGE_PATTERN = Pattern.compile("^\\s*(.+?):\\s*(\\d+(?:\\.\\d+)?)%\\s*$");
    
    // Track if we're in the commissions section of tab list
    private boolean inCommissionsSection = false;
    
    // Commission totals for fraction display
    // Dwarven Mines
    private static final Map<String, Integer> COMMISSION_TOTALS = new HashMap<>();
    static {
        // Dwarven Mines commissions
        COMMISSION_TOTALS.put("glacite walker slayer", 50);
        COMMISSION_TOTALS.put("goblin slayer", 100);
        COMMISSION_TOTALS.put("mithril miner", 350);
        COMMISSION_TOTALS.put("titanium miner", 15);
        COMMISSION_TOTALS.put("treasure hoarder puncher", 10);
        COMMISSION_TOTALS.put("goblin raid slayer", 20);
        COMMISSION_TOTALS.put("golden goblin slayer", 1);
        COMMISSION_TOTALS.put("lucky raffle", 20);
        COMMISSION_TOTALS.put("star sentry puncher", 10);
        COMMISSION_TOTALS.put("2x mithril powder collector", 500);
        
        // Area-specific Mithril/Titanium (Dwarven Mines)
        // These show as "[Area] Mithril" or "[Area] Titanium" in tab
        COMMISSION_TOTALS.put("mithril", 250); // For area-specific mithril
        COMMISSION_TOTALS.put("titanium", 10); // For area-specific titanium
        
        // Crystal Hollows commissions
        COMMISSION_TOTALS.put("automaton slayer", 13);
        COMMISSION_TOTALS.put("sludge slayer", 25);
        COMMISSION_TOTALS.put("team treasurite member slayer", 13);
        COMMISSION_TOTALS.put("yog slayer", 13);
        COMMISSION_TOTALS.put("chest looter", 3);
        COMMISSION_TOTALS.put("thyst slayer", 5);
        COMMISSION_TOTALS.put("hard stone miner", 1000);
        COMMISSION_TOTALS.put("boss corleone slayer", 1);
        
        // Gemstone collectors (Crystal Hollows) - all 1000
        COMMISSION_TOTALS.put("ruby collector", 1000);
        COMMISSION_TOTALS.put("amber collector", 1000);
        COMMISSION_TOTALS.put("sapphire collector", 1000);
        COMMISSION_TOTALS.put("jade collector", 1000);
        COMMISSION_TOTALS.put("amethyst collector", 1000);
        COMMISSION_TOTALS.put("topaz collector", 1000);
    }
    
    // Section headers to detect when we leave the commissions section
    private static final Set<String> SECTION_HEADERS = new HashSet<>(Arrays.asList(
        "players", "info", "profile", "skills", "stats", 
        "forges", "powders", "daily quests", "active effects",
        "cookie buff", "upgrades", "pet", "area", "server",
        "pickaxe ability", "event", "winter", "spooky",
        "objectives", "quests", "party", "guild", "skyblock"
    ));
    
    public List<Commission> getActiveCommissions() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastUpdate > UPDATE_INTERVAL) {
            updateCommissions();
            lastUpdate = currentTime;
        }
        return new ArrayList<>(activeCommissions);
    }
    
    /**
     * Clears all stored commissions. Call this when you want to reset.
     */
    public void clearCommissions() {
        activeCommissions.clear();
    }
    
    private void updateCommissions() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null || client.player == null || client.getNetworkHandler() == null) {
            return;
        }
        
        List<Commission> newCommissions = new ArrayList<>();
        inCommissionsSection = false;
        boolean foundCommissionsHeader = false;
        
        // Get player list entries (Tab list)
        Collection<PlayerListEntry> playerList = client.getNetworkHandler().getPlayerList();
        
        // Convert to list and sort by name to maintain order
        List<PlayerListEntry> sortedList = new ArrayList<>(playerList);
        sortedList.sort((a, b) -> {
            String nameA = a.getProfile().getName();
            String nameB = b.getProfile().getName();
            return nameA.compareTo(nameB);
        });
        
        for (PlayerListEntry entry : sortedList) {
            if (entry.getDisplayName() == null) {
                continue;
            }
            
            String displayText = entry.getDisplayName().getString();
            
            // Remove all color/formatting codes
            String cleanText = displayText.replaceAll("§[0-9a-fk-or]", "").trim();
            
            // Skip empty lines
            if (cleanText.isEmpty()) {
                continue;
            }
            
            String lowerText = cleanText.toLowerCase();
            
            // Check if this is the Commissions header
            if (lowerText.equals("commissions") || lowerText.equals("commissions:")) {
                inCommissionsSection = true;
                foundCommissionsHeader = true;
                continue;
            }
            
            // Check if we've hit another section header (end of commissions)
            if (isNewSection(lowerText)) {
                if (inCommissionsSection) {
                    inCommissionsSection = false;
                }
                continue;
            }
            
            // If we're in the commissions section, parse the commission
            if (inCommissionsSection) {
                Commission commission = parseCommissionLine(cleanText);
                if (commission != null) {
                    newCommissions.add(commission);
                }
            }
        }
        
        // IMPORTANT: Only update activeCommissions if we actually found the commissions section
        // This way, when the player leaves the mining area, the last known commissions persist
        if (foundCommissionsHeader) {
            activeCommissions = newCommissions;
        }
        // If no commissions header was found, we keep the old activeCommissions
        // This allows the HUD to display even when not in mining areas
    }
    
    private boolean isNewSection(String lowerText) {
        // Check exact matches for section headers
        for (String header : SECTION_HEADERS) {
            if (lowerText.equals(header) || lowerText.equals(header + ":")) {
                return true;
            }
        }
        
        // Check if line starts with a known header followed by colon
        // but NOT if it contains a percentage (which would be a commission)
        if (!lowerText.contains("%")) {
            for (String header : SECTION_HEADERS) {
                if (lowerText.startsWith(header + ":")) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    private Commission parseCommissionLine(String text) {
        // Match pattern: "Commission Name: XX%" or "Location Name Commission: XX%"
        Matcher matcher = PERCENTAGE_PATTERN.matcher(text);
        if (matcher.find()) {
            String name = matcher.group(1).trim();
            String percentStr = matcher.group(2);
            
            // Validate it looks like a real commission (not too short, not a header)
            if (name.length() < 2) {
                return null;
            }
            
            // Skip if it matches a known non-commission pattern
            String lowerName = name.toLowerCase();
            if (lowerName.equals("pet") || lowerName.equals("area") || 
                lowerName.equals("server") || lowerName.equals("profile") ||
                lowerName.contains("sb level") || lowerName.contains("bank") ||
                lowerName.contains("interest") || lowerName.contains("xp")) {
                return null;
            }
            
            try {
                double percent = Double.parseDouble(percentStr);
                return new Commission(name, (int) Math.round(percent));
            } catch (NumberFormatException e) {
                return null;
            }
        }
        
        // Also check for "DONE" status
        if (text.toLowerCase().contains("done")) {
            // Extract the name part before "DONE" or ": DONE"
            String name = text.replaceAll("(?i):?\\s*done\\s*$", "").trim();
            if (name.length() >= 2 && !isNewSection(name.toLowerCase())) {
                return new Commission(name, 100);
            }
        }
        
        return null;
    }
    
    public static class Commission {
        public final String name;
        public final int percentage;
        public final int total;
        public final int current;
        
        public Commission(String name, int percentage) {
            this.name = name;
            this.percentage = percentage;
            this.total = findTotal(name);
            this.current = (int) Math.round(this.total * (percentage / 100.0));
        }
        
        private static int findTotal(String commissionName) {
            String lowerName = commissionName.toLowerCase();
            
            // Direct match first
            if (COMMISSION_TOTALS.containsKey(lowerName)) {
                return COMMISSION_TOTALS.get(lowerName);
            }
            
            // Check if any key is contained in the name (for area-prefixed commissions)
            // e.g., "Lava Springs Titanium" should match "titanium"
            for (Map.Entry<String, Integer> entry : COMMISSION_TOTALS.entrySet()) {
                if (lowerName.contains(entry.getKey())) {
                    return entry.getValue();
                }
            }
            
            // Check if name ends with a known commission type
            for (Map.Entry<String, Integer> entry : COMMISSION_TOTALS.entrySet()) {
                if (lowerName.endsWith(entry.getKey())) {
                    return entry.getValue();
                }
            }
            
            // Default to 100 if unknown
            return 100;
        }
        
        @Override
        public String toString() {
            return name + ": " + percentage + "%";
        }
    }
}

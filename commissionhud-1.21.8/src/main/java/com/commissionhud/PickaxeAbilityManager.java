package com.commissionhud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import java.util.*;

public class PickaxeAbilityManager {
    private long lastUpdate = 0;
    private static final long UPDATE_INTERVAL = 250; // Update every 0.25 seconds for responsive cooldown
    
    // Stored ability info
    private String abilityName = "";
    private String abilityStatus = ""; // "Available" or cooldown time like "32s"
    private boolean foundAbility = false;
    
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
        
        // Find the Pickaxe Ability header index
        int abilityIndex = -1;
        for (int i = 0; i < tabLines.size(); i++) {
            String line = tabLines.get(i).toLowerCase();
            if (line.startsWith("pickaxe ability")) {
                abilityIndex = i;
                break;
            }
        }
        
        if (abilityIndex == -1) {
            return; // No pickaxe ability section found
        }
        
        foundAbility = true;
        
        // Read the line after "Pickaxe Ability" header
        if (abilityIndex + 1 < tabLines.size()) {
            String abilityLine = tabLines.get(abilityIndex + 1);
            
            // Parse "AbilityName: Status" format
            int colonIndex = abilityLine.indexOf(':');
            if (colonIndex > 0) {
                abilityName = abilityLine.substring(0, colonIndex).trim();
                abilityStatus = abilityLine.substring(colonIndex + 1).trim();
            }
        }
    }
    
    public String getAbilityName() {
        return abilityName;
    }
    
    public String getAbilityStatus() {
        return abilityStatus;
    }
    
    public boolean isAvailable() {
        return abilityStatus.equalsIgnoreCase("Available");
    }
    
    public boolean hasAbilityData() {
        return foundAbility && !abilityName.isEmpty();
    }
    
    public void clear() {
        abilityName = "";
        abilityStatus = "";
        foundAbility = false;
    }
}

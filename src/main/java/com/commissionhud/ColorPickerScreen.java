package com.commissionhud;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import java.awt.Color;

public class ColorPickerScreen extends Screen {
    private final Screen parent;
    private final ColorType colorType;
    private final int currentColor;
    
    // Hue bar properties (vertical bar on the right)
    private int hueBarX, hueBarY;
    private int hueBarWidth = 20;
    private int hueBarHeight = 120;
    
    // Saturation/Brightness square (main picker area)
    private int sbSquareX, sbSquareY;
    private int sbSquareSize = 120;
    
    // Resolution for gradients (lower = better performance)
    private static final int SB_RESOLUTION = 16; // 16x16 grid = 256 cells
    private static final int HUE_RESOLUTION = 24; // 24 color bands
    
    // Preview box
    private int previewX, previewY;
    private int previewSize = 25;
    
    // Current color state (HSB model)
    private float hue = 0f;
    private float saturation = 1f;
    private float brightness = 1f;
    
    // Selected color (RGB)
    private int selectedColor;
    
    // Hex input field
    private TextFieldWidget hexInput;
    
    // Dragging states
    private boolean draggingSquare = false;
    private boolean draggingHueBar = false;
    
    public enum ColorType {
        COMMISSION_TITLE_COLOR("Commission Title Color"),
        TEXT_COLOR("Text Color"),
        PROGRESS_BAR_COLOR("Progress Bar Color"),
        POWDER_TITLE_COLOR("Powder Title Color"),
        POWDER_LABEL_COLOR("Powder Label Color"),
        POWDER_VALUE_COLOR("Powder Value Color"),
        ABILITY_TITLE_COLOR("Ability Title Color"),
        ABILITY_LABEL_COLOR("Ability Label Color"),
        ABILITY_VALUE_COLOR("Ability Cooldown Color"),
        STATS_TITLE_COLOR("Stats Title Color"),
        STATS_LABEL_COLOR("Stats Label Color");
        
        private final String displayName;
        
        ColorType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    public ColorPickerScreen(Screen parent, ColorType colorType) {
        super(Text.literal("Color Picker - " + colorType.getDisplayName()));
        this.parent = parent;
        this.colorType = colorType;
        
        // Get current color based on type
        switch (colorType) {
            case COMMISSION_TITLE_COLOR:
                this.currentColor = CommissionHudMod.config.getTitleColor();
                break;
            case TEXT_COLOR:
                this.currentColor = CommissionHudMod.config.getColor();
                break;
            case PROGRESS_BAR_COLOR:
                this.currentColor = CommissionHudMod.config.getProgressBarColor();
                break;
            case POWDER_TITLE_COLOR:
                this.currentColor = CommissionHudMod.config.getPowderTitleColor();
                break;
            case POWDER_LABEL_COLOR:
                this.currentColor = CommissionHudMod.config.getPowderLabelColor();
                break;
            case POWDER_VALUE_COLOR:
                this.currentColor = CommissionHudMod.config.getPowderValueColor();
                break;
            case ABILITY_TITLE_COLOR:
                this.currentColor = CommissionHudMod.config.getAbilityTitleColor();
                break;
            case ABILITY_LABEL_COLOR:
                this.currentColor = CommissionHudMod.config.getAbilityLabelColor();
                break;
            case ABILITY_VALUE_COLOR:
                this.currentColor = CommissionHudMod.config.getAbilityValueColor();
                break;
            case STATS_TITLE_COLOR:
                this.currentColor = CommissionHudMod.config.getStatsTitleColor();
                break;
            case STATS_LABEL_COLOR:
                this.currentColor = CommissionHudMod.config.getStatsLabelColor();
                break;
            default:
                this.currentColor = 0xFFFFFF;
        }
        this.selectedColor = currentColor;
        
        // Convert current color to HSB
        float[] hsb = Color.RGBtoHSB(
            (currentColor >> 16) & 0xFF,
            (currentColor >> 8) & 0xFF,
            currentColor & 0xFF,
            null
        );
        this.hue = hsb[0];
        this.saturation = hsb[1];
        this.brightness = hsb[2];
    }
    
    public ColorPickerScreen(Screen parent) {
        this(parent, ColorType.TEXT_COLOR);
    }
    
    @Override
    protected void init() {
        // Calculate centered positions
        int totalWidth = sbSquareSize + 15 + hueBarWidth;
        int startX = (width - totalWidth) / 2;
        int startY = height / 2 - 80;
        
        // SB square on the left
        sbSquareX = startX;
        sbSquareY = startY;
        
        // Hue bar on the right
        hueBarX = startX + sbSquareSize + 15;
        hueBarY = startY;
        
        // Preview below
        previewX = startX;
        previewY = startY + sbSquareSize + 15;
        
        // Hex input field
        hexInput = new TextFieldWidget(textRenderer, startX + 70, previewY + 3, 70, 16, Text.literal("Hex"));
        hexInput.setMaxLength(7);
        hexInput.setText(String.format("#%06X", selectedColor));
        hexInput.setChangedListener(this::onHexChanged);
        addDrawableChild(hexInput);
        
        // Buttons
        int buttonY = previewY + 35;
        addDrawableChild(ButtonWidget.builder(Text.literal("Confirm"), b -> {
            applyColor();
            close();
        }).dimensions(startX, buttonY, 70, 20).build());
        
        addDrawableChild(ButtonWidget.builder(Text.literal("Cancel"), b -> close())
            .dimensions(startX + 80, buttonY, 70, 20).build());
    }
    
    private void applyColor() {
        switch (colorType) {
            case COMMISSION_TITLE_COLOR:
                CommissionHudMod.config.setTitleColor(selectedColor);
                break;
            case TEXT_COLOR:
                CommissionHudMod.config.setColor(selectedColor);
                break;
            case PROGRESS_BAR_COLOR:
                CommissionHudMod.config.setProgressBarColor(selectedColor);
                break;
            case POWDER_TITLE_COLOR:
                CommissionHudMod.config.setPowderTitleColor(selectedColor);
                break;
            case POWDER_LABEL_COLOR:
                CommissionHudMod.config.setPowderLabelColor(selectedColor);
                break;
            case POWDER_VALUE_COLOR:
                CommissionHudMod.config.setPowderValueColor(selectedColor);
                break;
            case ABILITY_TITLE_COLOR:
                CommissionHudMod.config.setAbilityTitleColor(selectedColor);
                break;
            case ABILITY_LABEL_COLOR:
                CommissionHudMod.config.setAbilityLabelColor(selectedColor);
                break;
            case ABILITY_VALUE_COLOR:
                CommissionHudMod.config.setAbilityValueColor(selectedColor);
                break;
            case STATS_TITLE_COLOR:
                CommissionHudMod.config.setStatsTitleColor(selectedColor);
                break;
            case STATS_LABEL_COLOR:
                CommissionHudMod.config.setStatsLabelColor(selectedColor);
                break;
        }
    }
    
    private void onHexChanged(String hex) {
        try {
            String cleanHex = hex.startsWith("#") ? hex.substring(1) : hex;
            if (cleanHex.length() == 6) {
                int color = Integer.parseInt(cleanHex, 16);
                selectedColor = color;
                
                float[] hsb = Color.RGBtoHSB(
                    (color >> 16) & 0xFF,
                    (color >> 8) & 0xFF,
                    color & 0xFF,
                    null
                );
                hue = hsb[0];
                saturation = hsb[1];
                brightness = hsb[2];
            }
        } catch (NumberFormatException ignored) {}
    }
    
    private void updateColorFromHSB() {
        int rgb = Color.HSBtoRGB(hue, saturation, brightness);
        selectedColor = rgb & 0xFFFFFF;
        
        if (hexInput != null) {
            hexInput.setText(String.format("#%06X", selectedColor));
        }
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        
        // Background panel
        int panelPadding = 10;
        int panelLeft = sbSquareX - panelPadding;
        int panelTop = sbSquareY - 25;
        int panelRight = hueBarX + hueBarWidth + panelPadding;
        int panelBottom = previewY + 60;
        
        context.fill(panelLeft, panelTop, panelRight, panelBottom, 0xDD222222);
        drawBorder(context, panelLeft, panelTop, panelRight - panelLeft, panelBottom - panelTop, 0xFF4a4a4a);
        
        // Title
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, panelTop + 8, 0xFFFFFFFF);
        
        // Draw the SB square (low resolution for performance)
        drawSBSquare(context);
        
        // Draw hue bar (low resolution for performance)
        drawHueBar(context);
        
        // Draw selectors
        drawSBSelector(context);
        drawHueSelector(context);
        
        // Draw preview
        drawPreview(context);
        
        super.render(context, mouseX, mouseY, delta);
    }
    
    private void drawSBSquare(DrawContext context) {
        // Low resolution grid - only SB_RESOLUTION^2 draw calls (256 with 16x16)
        int cellSize = sbSquareSize / SB_RESOLUTION;
        
        int baseColor = Color.HSBtoRGB(hue, 1f, 1f) & 0xFFFFFF;
        int baseR = (baseColor >> 16) & 0xFF;
        int baseG = (baseColor >> 8) & 0xFF;
        int baseB = baseColor & 0xFF;
        
        for (int row = 0; row < SB_RESOLUTION; row++) {
            float brightnessAtRow = 1f - (float) row / SB_RESOLUTION;
            
            for (int col = 0; col < SB_RESOLUTION; col++) {
                float satAtCol = (float) col / SB_RESOLUTION;
                
                int r = (int) ((255 + (baseR - 255) * satAtCol) * brightnessAtRow);
                int g = (int) ((255 + (baseG - 255) * satAtCol) * brightnessAtRow);
                int b = (int) ((255 + (baseB - 255) * satAtCol) * brightnessAtRow);
                
                int color = 0xFF000000 | (r << 16) | (g << 8) | b;
                
                int x1 = sbSquareX + col * cellSize;
                int y1 = sbSquareY + row * cellSize;
                int x2 = (col == SB_RESOLUTION - 1) ? sbSquareX + sbSquareSize : x1 + cellSize;
                int y2 = (row == SB_RESOLUTION - 1) ? sbSquareY + sbSquareSize : y1 + cellSize;
                
                context.fill(x1, y1, x2, y2, color);
            }
        }
        
        // Border
        drawBorder(context, sbSquareX - 1, sbSquareY - 1, sbSquareSize + 2, sbSquareSize + 2, 0xFF555555);
    }
    
    private void drawHueBar(DrawContext context) {
        // Low resolution hue bar - only HUE_RESOLUTION draw calls
        int bandHeight = hueBarHeight / HUE_RESOLUTION;
        
        for (int i = 0; i < HUE_RESOLUTION; i++) {
            float h = (float) i / HUE_RESOLUTION;
            int color = Color.HSBtoRGB(h, 1f, 1f);
            
            int y1 = hueBarY + i * bandHeight;
            int y2 = (i == HUE_RESOLUTION - 1) ? hueBarY + hueBarHeight : y1 + bandHeight;
            
            context.fill(hueBarX, y1, hueBarX + hueBarWidth, y2, 0xFF000000 | color);
        }
        
        // Border
        drawBorder(context, hueBarX - 1, hueBarY - 1, hueBarWidth + 2, hueBarHeight + 2, 0xFF555555);
    }
    
    private void drawSBSelector(DrawContext context) {
        int selX = sbSquareX + (int) (saturation * sbSquareSize);
        int selY = sbSquareY + (int) ((1f - brightness) * sbSquareSize);
        
        // Clamp to bounds
        selX = Math.max(sbSquareX, Math.min(sbSquareX + sbSquareSize - 1, selX));
        selY = Math.max(sbSquareY, Math.min(sbSquareY + sbSquareSize - 1, selY));
        
        // Crosshair selector
        context.fill(selX - 5, selY, selX + 6, selY + 1, 0xFF000000);
        context.fill(selX, selY - 5, selX + 1, selY + 6, 0xFF000000);
        context.fill(selX - 4, selY, selX + 5, selY + 1, 0xFFFFFFFF);
        context.fill(selX, selY - 4, selX + 1, selY + 5, 0xFFFFFFFF);
    }
    
    private void drawHueSelector(DrawContext context) {
        int selY = hueBarY + (int) (hue * hueBarHeight);
        selY = Math.max(hueBarY, Math.min(hueBarY + hueBarHeight - 1, selY));
        
        // Arrow indicators
        context.fill(hueBarX - 4, selY - 2, hueBarX, selY + 3, 0xFFFFFFFF);
        context.fill(hueBarX + hueBarWidth, selY - 2, hueBarX + hueBarWidth + 4, selY + 3, 0xFFFFFFFF);
    }
    
    private void drawPreview(DrawContext context) {
        // Old color
        context.fill(previewX, previewY, previewX + previewSize, previewY + previewSize, 0xFF000000 | currentColor);
        drawBorder(context, previewX - 1, previewY - 1, previewSize + 2, previewSize + 2, 0xFF555555);
        
        // New color
        int newX = previewX + previewSize + 5;
        context.fill(newX, previewY, newX + previewSize, previewY + previewSize, 0xFF000000 | selectedColor);
        drawBorder(context, newX - 1, previewY - 1, previewSize + 2, previewSize + 2, 0xFF555555);
    }
    
    private void drawBorder(DrawContext context, int x, int y, int w, int h, int color) {
        context.fill(x, y, x + w, y + 1, color);
        context.fill(x, y + h - 1, x + w, y + h, color);
        context.fill(x, y, x + 1, y + h, color);
        context.fill(x + w - 1, y, x + w, y + h, color);
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            // Check SB square
            if (mouseX >= sbSquareX && mouseX < sbSquareX + sbSquareSize &&
                mouseY >= sbSquareY && mouseY < sbSquareY + sbSquareSize) {
                draggingSquare = true;
                updateFromSquare(mouseX, mouseY);
                return true;
            }
            
            // Check hue bar
            if (mouseX >= hueBarX && mouseX < hueBarX + hueBarWidth &&
                mouseY >= hueBarY && mouseY < hueBarY + hueBarHeight) {
                draggingHueBar = true;
                updateFromHueBar(mouseY);
                return true;
            }
        }
        
        return super.mouseClicked(mouseX, mouseY, button);
    }
    
    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        draggingSquare = false;
        draggingHueBar = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }
    
    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (draggingSquare) {
            updateFromSquare(mouseX, mouseY);
            return true;
        }
        if (draggingHueBar) {
            updateFromHueBar(mouseY);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }
    
    private void updateFromSquare(double mouseX, double mouseY) {
        saturation = (float) Math.max(0, Math.min(1, (mouseX - sbSquareX) / sbSquareSize));
        brightness = (float) Math.max(0, Math.min(1, 1 - (mouseY - sbSquareY) / sbSquareSize));
        updateColorFromHSB();
    }
    
    private void updateFromHueBar(double mouseY) {
        hue = (float) Math.max(0, Math.min(1, (mouseY - hueBarY) / hueBarHeight));
        updateColorFromHSB();
    }
    
    @Override
    public void close() {
        if (client != null) {
            client.setScreen(parent);
        }
    }
    
    @Override
    public boolean shouldPause() {
        return false;
    }
}

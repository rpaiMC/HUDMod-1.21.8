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
    
    // Color wheel properties
    private int wheelCenterX, wheelCenterY;
    private int wheelRadius = 70;
    
    // Brightness slider properties
    private int sliderX, sliderY;
    private int sliderWidth = 15;
    private int sliderHeight = 140;
    
    // Preview box
    private int previewX, previewY;
    private int previewSize = 30;
    
    // Current color state (HSB model for easier manipulation)
    private float hue = 0f;
    private float saturation = 1f;
    private float brightness = 1f;
    
    // Selected color (RGB)
    private int selectedColor;
    
    // Hex input field
    private TextFieldWidget hexInput;
    
    // Dragging states
    private boolean draggingWheel = false;
    private boolean draggingSlider = false;
    
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
    
    // Constructor for backwards compatibility
    public ColorPickerScreen(Screen parent) {
        this(parent, ColorType.TEXT_COLOR);
    }
    
    @Override
    protected void init() {
        // Calculate centered positions
        int contentWidth = wheelRadius * 2 + 40 + sliderWidth;
        int contentStartX = (width - contentWidth) / 2;
        
        // Center the color wheel
        wheelCenterX = contentStartX + wheelRadius;
        wheelCenterY = height / 2 - 20;
        
        // Slider to the right of wheel
        sliderX = contentStartX + wheelRadius * 2 + 30;
        sliderY = wheelCenterY - sliderHeight / 2;
        
        // Preview box centered below
        previewX = width / 2 - previewSize - 5;
        previewY = wheelCenterY + wheelRadius + 25;
        
        // Hex input field centered
        hexInput = new TextFieldWidget(textRenderer, width / 2 - 40, previewY + previewSize + 15, 80, 18, Text.literal("Hex"));
        hexInput.setMaxLength(7);
        hexInput.setText(String.format("#%06X", selectedColor));
        hexInput.setChangedListener(this::onHexChanged);
        addDrawableChild(hexInput);
        
        // Confirm and Cancel buttons - centered below hex input
        int buttonY = previewY + previewSize + 45;
        addDrawableChild(ButtonWidget.builder(Text.literal("Confirm"), b -> {
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
            close();
        }).dimensions(width / 2 - 102, buttonY, 100, 20).build());
        
        addDrawableChild(ButtonWidget.builder(Text.literal("Cancel"), b -> close())
            .dimensions(width / 2 + 2, buttonY, 100, 20).build());
    }
    
    private void onHexChanged(String hex) {
        try {
            String cleanHex = hex.startsWith("#") ? hex.substring(1) : hex;
            if (cleanHex.length() == 6) {
                int color = Integer.parseInt(cleanHex, 16);
                setColorFromRGB(color, false);
            }
        } catch (NumberFormatException ignored) {
        }
    }
    
    private void setColorFromRGB(int rgb) {
        setColorFromRGB(rgb, true);
    }
    
    private void setColorFromRGB(int rgb, boolean updateTextField) {
        this.selectedColor = rgb;
        
        float[] hsb = Color.RGBtoHSB(
            (rgb >> 16) & 0xFF,
            (rgb >> 8) & 0xFF,
            rgb & 0xFF,
            null
        );
        this.hue = hsb[0];
        this.saturation = hsb[1];
        this.brightness = hsb[2];
        
        if (updateTextField && hexInput != null) {
            hexInput.setText(String.format("#%06X", selectedColor));
        }
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
        // Use the normal background rendering (blur is disabled via mixin)
        renderBackground(context, mouseX, mouseY, delta);
        
        // Draw a panel behind the color picker elements
        int panelPadding = 15;
        int panelLeft = wheelCenterX - wheelRadius - panelPadding;
        int panelTop = wheelCenterY - wheelRadius - 30;
        int panelRight = sliderX + sliderWidth + panelPadding;
        int panelBottom = previewY + previewSize + 10;
        
        // Draw solid background panel
        context.fill(panelLeft, panelTop, panelRight, panelBottom, 0xCC222222);
        // Panel border
        drawBorder(context, panelLeft, panelTop, panelRight - panelLeft, panelBottom - panelTop, 0xFF4a4a4a);
        
        // Title
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 15, 0xFFFFFF);
        
        // Labels - centered above their elements
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("Hue & Saturation"), wheelCenterX, wheelCenterY - wheelRadius - 15, 0xFFCCCCCC);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("Bright"), sliderX + sliderWidth / 2, sliderY - 12, 0xFFCCCCCC);
        context.drawTextWithShadow(textRenderer, Text.literal("Preview:"), previewX - 50, previewY + 10, 0xFFCCCCCC);
        
        // Draw color wheel
        drawColorWheel(context);
        
        // Draw brightness slider
        drawBrightnessSlider(context);
        
        // Draw color preview
        drawColorPreview(context);
        
        // Draw wheel selector
        drawWheelSelector(context);
        
        // Draw slider selector
        drawSliderSelector(context);
        
        // Render buttons and text field on top
        super.render(context, mouseX, mouseY, delta);
    }
    
    private void drawColorWheel(DrawContext context) {
        // Draw color wheel
        for (int y = -wheelRadius; y <= wheelRadius; y++) {
            for (int x = -wheelRadius; x <= wheelRadius; x++) {
                double distance = Math.sqrt(x * x + y * y);
                if (distance <= wheelRadius) {
                    float h = (float) ((Math.atan2(y, x) / (2 * Math.PI)) + 0.5);
                    float s = (float) (distance / wheelRadius);
                    
                    int color = Color.HSBtoRGB(h, s, brightness);
                    context.fill(wheelCenterX + x, wheelCenterY + y, wheelCenterX + x + 1, wheelCenterY + y + 1, 0xFF000000 | color);
                }
            }
        }
        
        // Draw clean circular border
        for (int i = 0; i < 360; i++) {
            double angle = Math.toRadians(i);
            int x = (int) (wheelCenterX + (wheelRadius + 1) * Math.cos(angle));
            int y = (int) (wheelCenterY + (wheelRadius + 1) * Math.sin(angle));
            context.fill(x, y, x + 1, y + 1, 0xFF555555);
        }
    }
    
    private void drawBrightnessSlider(DrawContext context) {
        // Draw gradient
        for (int y = 0; y < sliderHeight; y++) {
            float b = 1.0f - (float) y / sliderHeight;
            int color = Color.HSBtoRGB(hue, saturation, b);
            context.fill(sliderX, sliderY + y, sliderX + sliderWidth, sliderY + y + 1, 0xFF000000 | color);
        }
        
        // Draw border
        drawBorder(context, sliderX - 1, sliderY - 1, sliderWidth + 2, sliderHeight + 2, 0xFF555555);
    }
    
    private void drawColorPreview(DrawContext context) {
        // Current/old color
        context.fill(previewX, previewY, previewX + previewSize, previewY + previewSize, 0xFF000000 | currentColor);
        drawBorder(context, previewX - 1, previewY - 1, previewSize + 2, previewSize + 2, 0xFF555555);
        
        // New/selected color
        int newPreviewX = previewX + previewSize + 10;
        context.fill(newPreviewX, previewY, newPreviewX + previewSize, previewY + previewSize, 0xFF000000 | selectedColor);
        drawBorder(context, newPreviewX - 1, previewY - 1, previewSize + 2, previewSize + 2, 0xFF555555);
        
        // Labels
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("Old"), previewX + previewSize / 2, previewY + previewSize + 2, 0xFF888888);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("New"), newPreviewX + previewSize / 2, previewY + previewSize + 2, 0xFF888888);
    }
    
    private void drawBorder(DrawContext context, int x, int y, int w, int h, int color) {
        context.fill(x, y, x + w, y + 1, color); // top
        context.fill(x, y + h - 1, x + w, y + h, color); // bottom
        context.fill(x, y, x + 1, y + h, color); // left
        context.fill(x + w - 1, y, x + w, y + h, color); // right
    }
    
    private void drawWheelSelector(DrawContext context) {
        double angle = (hue - 0.5) * 2 * Math.PI;
        double distance = saturation * wheelRadius;
        
        int selectorX = (int) (wheelCenterX + distance * Math.cos(angle));
        int selectorY = (int) (wheelCenterY + distance * Math.sin(angle));
        
        // Draw selector - white circle with black outline
        for (int i = 0; i < 360; i++) {
            double a = Math.toRadians(i);
            int x5 = (int) (selectorX + 5 * Math.cos(a));
            int y5 = (int) (selectorY + 5 * Math.sin(a));
            int x4 = (int) (selectorX + 4 * Math.cos(a));
            int y4 = (int) (selectorY + 4 * Math.sin(a));
            context.fill(x5, y5, x5 + 1, y5 + 1, 0xFF000000);
            context.fill(x4, y4, x4 + 1, y4 + 1, 0xFFFFFFFF);
        }
    }
    
    private void drawSliderSelector(DrawContext context) {
        int selectorY = sliderY + (int) ((1.0f - brightness) * sliderHeight);
        
        // Draw arrow indicators
        // Left arrow
        context.fill(sliderX - 6, selectorY - 1, sliderX - 1, selectorY + 2, 0xFFFFFFFF);
        // Right arrow
        context.fill(sliderX + sliderWidth + 1, selectorY - 1, sliderX + sliderWidth + 6, selectorY + 2, 0xFFFFFFFF);
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            double distFromCenter = Math.sqrt(Math.pow(mouseX - wheelCenterX, 2) + Math.pow(mouseY - wheelCenterY, 2));
            
            if (distFromCenter <= wheelRadius) {
                draggingWheel = true;
                updateColorFromWheel(mouseX, mouseY);
                return true;
            }
            
            if (mouseX >= sliderX && mouseX <= sliderX + sliderWidth &&
                mouseY >= sliderY && mouseY <= sliderY + sliderHeight) {
                draggingSlider = true;
                updateBrightnessFromSlider(mouseY);
                return true;
            }
        }
        
        return super.mouseClicked(mouseX, mouseY, button);
    }
    
    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        draggingWheel = false;
        draggingSlider = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }
    
    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (draggingWheel) {
            updateColorFromWheel(mouseX, mouseY);
            return true;
        }
        
        if (draggingSlider) {
            updateBrightnessFromSlider(mouseY);
            return true;
        }
        
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }
    
    private void updateColorFromWheel(double mouseX, double mouseY) {
        double dx = mouseX - wheelCenterX;
        double dy = mouseY - wheelCenterY;
        double distance = Math.sqrt(dx * dx + dy * dy);
        
        if (distance > wheelRadius) {
            distance = wheelRadius;
        }
        
        hue = (float) ((Math.atan2(dy, dx) / (2 * Math.PI)) + 0.5);
        saturation = (float) (distance / wheelRadius);
        
        updateColorFromHSB();
    }
    
    private void updateBrightnessFromSlider(double mouseY) {
        double y = Math.max(sliderY, Math.min(sliderY + sliderHeight, mouseY));
        brightness = 1.0f - (float) (y - sliderY) / sliderHeight;
        
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

package com.JFroggy;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.awt.Color;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnimationColorConfigEntry
{
    private String name; // New field for the configuration name
    private int animationId;
    private Color color;
    private int tickDuration;
    private int opacity;

    // Manually added getters and setters to resolve compilation issues
    public int getAnimationId() {
        return animationId;
    }

    public Color getColor() {
        return color;
    }

    public int getTickDuration() {
        return tickDuration;
    }

    public int getOpacity() {
        return opacity;
    }

    public void setOpacity(int opacity) {
        this.opacity = opacity;
    }
}

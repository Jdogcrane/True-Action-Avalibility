package com.JFroggy;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.awt.Color;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InteractionConfigEntry
{
    private String name;
    private int targetId; // NPC ID, Object ID, Item ID, etc.
    private Color color;
    private int tickDuration;
    private int opacity;

    // Manually added getters and setters to resolve compilation issues, similar to AnimationColorConfigEntry
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getTargetId() {
        return targetId;
    }

    public void setTargetId(int targetId) {
        this.targetId = targetId;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public int getTickDuration() {
        return tickDuration;
    }

    public void setTickDuration(int tickDuration) {
        this.tickDuration = tickDuration;
    }

    public int getOpacity() {
        return opacity;
    }

    public void setOpacity(int opacity) {
        this.opacity = opacity;
    }
}

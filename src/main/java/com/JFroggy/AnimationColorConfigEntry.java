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
}

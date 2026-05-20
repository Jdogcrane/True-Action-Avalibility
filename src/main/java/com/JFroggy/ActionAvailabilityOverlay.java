package com.JFroggy;

import lombok.Setter;
import net.runelite.api.Client;
import net.runelite.api.Point;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.awt.*;

@Slf4j
public class ActionAvailabilityOverlay extends Overlay
{
    private final Client client;
    private final ActionAvailabilityConfig config;
    @Setter
    private Color currentColor = Color.BLACK;
    @Setter
    private boolean showDot = false;
    @Setter
    private String countdownText = null;
    @Setter
    private Color countdownColor = Color.WHITE;
    @Setter
    private int currentRenderDotSize; // New field for dynamic dot size during lerp-out

    @Inject
    public ActionAvailabilityOverlay(Client client, ActionAvailabilityConfig config)
    {
        this.client = client;
        this.config = config;
        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_WIDGETS);
        this.currentRenderDotSize = config.dotSize(); // Initialize with configured size
    }

    public boolean getShowDot() {
        return showDot;
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        if (!showDot || client.getMouseCanvasPosition() == null)
        {
            return null;
        }

        Point mousePos = client.getMouseCanvasPosition();
        if (mousePos == null)
        {
            return null;
        }

        // Use currentRenderDotSize for drawing, which can be dynamically changed for lerp-out
        int dotSizeToRender = currentRenderDotSize;
        int outlineWidth = config.outlineWidth();
        Color outlineColor = config.outlineColor();

        // Calculate the top-left corner of the dot based on configurable offsets
        int dotX = mousePos.getX() + config.dotOffsetX();
        int dotY = mousePos.getY() + config.dotOffsetY();

        boolean renderDot = config.countdownDisplayMode() == ActionAvailabilityConfig.CountdownDisplayMode.DOT_ONLY ||
                            config.countdownDisplayMode() == ActionAvailabilityConfig.CountdownDisplayMode.BOTH;
        boolean renderCountdown = config.countdownDisplayMode() == ActionAvailabilityConfig.CountdownDisplayMode.COUNTDOWN_ONLY ||
                                  config.countdownDisplayMode() == ActionAvailabilityConfig.CountdownDisplayMode.BOTH;

        if (renderDot && dotSizeToRender > 0) // Only render dot if its size is positive
        {
            // Draw outline if configured
            if (outlineWidth > 0)
            {
                graphics.setColor(outlineColor);
                // Draw the outline slightly larger than the dot itself, centered around the dot's position
                graphics.fillOval(dotX - outlineWidth, dotY - outlineWidth, dotSizeToRender + (outlineWidth * 2), dotSizeToRender + (outlineWidth * 2));
            }

            // Draw the main dot
            graphics.setColor(currentColor);
            graphics.fillOval(dotX, dotY, dotSizeToRender, dotSizeToRender);
        }


        // Draw countdown text if available and enabled
        if (renderCountdown && countdownText != null && !countdownText.isEmpty())
        {
            graphics.setColor(countdownColor);
            graphics.setFont(new Font("Arial", Font.BOLD, config.countdownFontSize()));
            // Position the text next to the dot, or where the dot would be if only countdown is shown
            int textX = renderDot ? dotX + dotSizeToRender + 5 : dotX;
            int textY = renderDot ? dotY + dotSizeToRender / 2 + graphics.getFontMetrics().getAscent() / 2 : dotY + graphics.getFontMetrics().getAscent();
            graphics.drawString(countdownText, textX, textY);
        }

        return null;
    }
}

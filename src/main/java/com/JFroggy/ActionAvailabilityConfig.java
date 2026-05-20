package com.JFroggy;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;

import java.awt.Color;

@ConfigGroup("actionavailability")
public interface ActionAvailabilityConfig extends Config
{
	@ConfigSection(
		name = "General",
		description = "General plugin settings",
		position = 0
	)
	String generalSection = "generalSection";

	@ConfigSection(
		name = "Dot Styling",
		description = "Configure the appearance of the action availability dot",
		position = 1
	)
	String dotStylingSection = "dotStylingSection";

	@ConfigSection(
		name = "Animation Color Configurations",
		description = "Configurations for player animation colors",
		position = 2
	)
	String animationColorConfigSection = "animationColorConfigSection";

	int idleDotDisplayDuration();

	boolean enableDefaultIdleDot();

	Color defaultIdleDotColor();

	int defaultIdleDotOpacity();

	enum CountdownDisplayMode
	{
		DOT_ONLY,
		COUNTDOWN_ONLY,
		BOTH
	}


	@ConfigItem(
		keyName = "debugAnimationId",
		name = "Debug Animation ID",
		description = "Logs the current animation ID to the console and in-game chat",
		section = generalSection
	)
	default boolean debugAnimationId()
	{
		return false;
	}

	@ConfigItem(
		keyName = "autoDetectActionLock",
		name = "Auto Detect Action Lock",
		description = "Automatically detects if the player is locked in an action.",
		section = generalSection
	)
	default boolean autoDetectActionLock()
	{
		return false;
	}

	@ConfigItem(
		keyName = "autoDetectActionLock",
		name = "",
		description = "",
		hidden = true,
		section = generalSection
	)
	void setAutoDetectActionLock(boolean enabled);

	@Range(
		min = 1,
		max = 20
	)
	@ConfigItem(
		keyName = "dotSize",
		name = "Dot Size",
		description = "The size of the action availability dot",
		section = dotStylingSection
	)
	default int dotSize()
	{
		return 5;
	}

	@Range(
		min = -50,
		max = 50
	)
	@ConfigItem(
		keyName = "dotOffsetX",
		name = "Dot Offset X",
		description = "X-axis offset for the dot relative to the mouse cursor.",
		section = dotStylingSection
	)
	default int dotOffsetX()
	{
		return 10; // Reverting to the original offset
	}

	@ConfigItem(
		keyName = "dotOffsetX",
		name = "",
		description = "",
		hidden = true,
		section = dotStylingSection
	)
	void setDotOffsetX(int offset);

	@Range(
		min = -50,
		max = 50
	)
	@ConfigItem(
		keyName = "dotOffsetY",
		name = "Dot Offset Y",
		description = "Y-axis offset for the dot relative to the mouse cursor.",
		section = dotStylingSection
	)
	default int dotOffsetY()
	{
		return 10; // Reverting to the original offset
	}

	@ConfigItem(
		keyName = "dotOffsetY",
		name = "",
		description = "",
		hidden = true,
		section = dotStylingSection
	)
	void setDotOffsetY(int offset);

	@Range(
		min = 0,
		max = 5
	)
	@ConfigItem(
		keyName = "outlineWidth",
		name = "Outline Width",
		description = "The width of the dot's outline (0 for no outline)",
		section = dotStylingSection
	)
	default int outlineWidth()
	{
		return 1;
	}

	@ConfigItem(
		keyName = "outlineColor",
		name = "Outline Color",
		description = "The color of the dot's outline",
		section = dotStylingSection
	)
	default Color outlineColor()
	{
		return Color.BLACK;
	}

	@ConfigItem(
		keyName = "countdownDisplayMode",
		name = "Countdown Display",
		description = "Choose whether to display the dot, countdown, or both.",
		section = dotStylingSection
	)
	default CountdownDisplayMode countdownDisplayMode()
	{
		return CountdownDisplayMode.BOTH;
	}

	@ConfigItem(
		keyName = "countdownDisplayMode",
		name = "",
		description = "",
		hidden = true,
		section = dotStylingSection
	)
	void setCountdownDisplayMode(CountdownDisplayMode selectedItem);

	@ConfigItem(
		keyName = "countdownColor",
		name = "Countdown Color",
		description = "The color of the countdown text.",
		section = dotStylingSection
	)
	default Color countdownColor()
	{
		return Color.WHITE;
	}

	@ConfigItem(
		keyName = "countdownColor",
		name = "",
		description = "",
		hidden = true,
		section = dotStylingSection
	)
	void setCountdownColor(Color newColor);

	@ConfigItem(
		keyName = "countdownZeroColor",
		name = "Countdown Zero Color",
		description = "The color of the countdown text when it hits 0.",
		section = dotStylingSection
	)
	default Color countdownZeroColor()
	{
		return Color.RED;
	}

	@ConfigItem(
		keyName = "countdownZeroColor",
		name = "",
		description = "",
		hidden = true,
		section = dotStylingSection
	)
	void setCountdownZeroColor(Color newColor);

	@Range(
		min = 10,
		max = 30
	)
	@ConfigItem(
		keyName = "countdownFontSize",
		name = "Countdown Font Size",
		description = "The font size of the countdown text.",
		section = dotStylingSection
	)
	default int countdownFontSize()
	{
		return 12;
	}

	@Range(
		min = 0,
		max = 10
	)
	@ConfigItem(
		keyName = "dotInactivityDuration",
		name = "Green Dot Inactivity Duration (ticks)",
		description = "How many ticks the green dot remains at full size after its active duration, before fading out.",
		section = dotStylingSection
	)
	default int dotInactivityDuration()
	{
		return 0; // Default to 0 ticks of inactivity
	}

	@Range(
		min = 0,
		max = 10
	)
	@ConfigItem(
		keyName = "dotFadeOutDuration",
		name = "Green Dot Fade Out Duration (ticks)",
		description = "How many ticks the green dot takes to fade out after inactivity period.",
		section = dotStylingSection
	)
	default int dotFadeOutDuration()
	{
		return 3; // Default to 3 ticks for fading out
	}

	@Range(
		min = 0,
		max = 10
	)
	@ConfigItem(
		keyName = "greenDotDisplayDuration",
		name = "Green Dot Active Duration (ticks)",
		description = "How many ticks the green dot is displayed at full size after being triggered (0 for infinite).",
		section = animationColorConfigSection
	)
	default int greenDotDisplayDuration()
	{
		return 0; // Default to infinite
	}

	@ConfigItem(
		keyName = "animationColorConfigs",
		name = "Animation Color Configurations",
		description = "The JSON string storing animation color configurations",
		hidden = true,
		section = animationColorConfigSection
	)
	default String animationColorConfigs()
	{
		return "[]";
	}

	@ConfigItem(
		keyName = "animationColorConfigs",
		name = "",
		description = "",
		hidden = true,
		section = animationColorConfigSection
	)
	void setAnimationColorConfigs(String json);

	@ConfigItem(
		keyName = "enableGreenDot",
		name = "Enable Green Dot",
		description = "Enables the green dot (ready indicator) when no specific animation is active or after movement.",
		section = animationColorConfigSection
	)
	default boolean enableGreenDot()
	{
		return false;
	}

	@ConfigItem(
		keyName = "greenDotColor",
		name = "Green Dot Color",
		description = "The color of the green dot (ready indicator).",
		section = animationColorConfigSection
	)
	default Color greenDotColor()
	{
		return Color.GREEN; // Changed default to green
	}

	@ConfigItem(
		keyName = "greenDotOpacity",
		name = "Green Dot Opacity",
		description = "The opacity of the green dot (0-255).",
		section = animationColorConfigSection
	)
	default int greenDotOpacity()
	{
		return 100;
	}
}

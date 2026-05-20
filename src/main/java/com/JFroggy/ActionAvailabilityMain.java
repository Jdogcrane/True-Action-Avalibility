package com.JFroggy;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Provides;
import javax.inject.Inject;

import lombok.Getter;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Player;
import net.runelite.api.coords.WorldPoint; // Import WorldPoint
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.ImageUtil;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Slf4j
@PluginDescriptor(
	name = "Action Availability"
)
public class ActionAvailabilityMain extends Plugin
{
	@Inject
	private Client client;

	@Inject
    ActionAvailabilityConfig config;

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private Gson gson;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private ActionAvailabilityOverlay playerDotOverlay;

	private NavigationButton navButton;
	private ActionAvailabilityPanel panel;

    // Public getter for player animation configurations
    @Getter
    private List<AnimationColorConfigEntry> animationColorConfigs;
	private final Type animationColorConfigListType = new TypeToken<ArrayList<AnimationColorConfigEntry>>(){}.getType();

	private int specificAnimationTicksRemaining = 0; // Renamed from currentAnimationTicksRemaining
	private int currentActiveDotId = -2; // Renamed from currentDotAnimationId (-1 for default idle, -2 for no dot, other for specific animation)

	// Fields for tracking player animation history
	private int lastAnimationId = -1;
	private int ticksSinceLastAnimationChange = 0;
	private final LinkedList<AnimationHistoryEntry> recentAnimationHistory = new LinkedList<>();
	private static final int MAX_RECENT_ANIMATIONS_HISTORY = 3;

	// Fields for tracking player movement
	private WorldPoint previousWorldPoint;
	private boolean isPlayerMoving;

	// Fields for idle dot lifecycle
	private int idleDotActiveDurationRemaining = 0; // How long the idle dot stays fully visible
	private int idleDotInactivityDurationRemaining = 0; // How long the idle dot stays inactive before fading
	private int idleDotFadeOutDurationRemaining = 0; // How long the idle dot takes to fade out
	private int idleDotFadeOutStartSize = 0; // Starting size for idle dot fade out

	@Value
	public static class AnimationHistoryEntry
	{
		int animationId;
		int durationTicks;
	}

	@Override
	protected void startUp()
	{
		log.debug("Action Availability started!");

		final BufferedImage icon = ImageUtil.loadImageResource(getClass(), "/action_availability_icon.png");

		animationColorConfigs = loadAnimationColorConfigs();

		panel = new ActionAvailabilityPanel(this);

		navButton = NavigationButton.builder()
			.tooltip("Action Availability")
			.icon(icon)
			.priority(100)
			.panel(panel)
			.build();

		clientToolbar.addNavigation(navButton);
		overlayManager.add(playerDotOverlay);
	}

	@Override
	protected void shutDown()
	{
		clientToolbar.removeNavigation(navButton);
		overlayManager.remove(playerDotOverlay);

		log.debug("Action Availability stopped!");
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		if (gameStateChanged.getGameState() == GameState.LOGGED_IN)
		{
			// Initialize previousWorldPoint when logged in
			Player localPlayer = client.getLocalPlayer();
			if (localPlayer != null)
			{
				previousWorldPoint = localPlayer.getWorldLocation();
			}
		}
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		ticksSinceLastAnimationChange++;

		// --- Priority 1: Handle Idle Dot Fade-Out ---
		if (idleDotFadeOutDurationRemaining > 0)
		{
			idleDotFadeOutDurationRemaining--;
			if (config.dotFadeOutDuration() > 0)
			{
				double scale = (double) idleDotFadeOutDurationRemaining / config.dotFadeOutDuration();
				playerDotOverlay.setCurrentRenderDotSize((int) (idleDotFadeOutStartSize * scale));
			}
			if (idleDotFadeOutDurationRemaining == 0)
			{
				playerDotOverlay.setShowDot(false);
				playerDotOverlay.setCountdownText(null);
				currentActiveDotId = -2; // <--- Set to -2 when fade-out completes
			}
			return; // Skip other dot logic during fade-out
		}
		// If fade-out just finished (idleDotFadeOutDurationRemaining was 1, now 0), and currentActiveDotId is -2,
		// we should not proceed to other idle dot logic.
		if (currentActiveDotId == -2) {
		    return;
		}


		// --- Priority 2: Handle Idle Dot Inactivity ---
		if (idleDotInactivityDurationRemaining > 0)
		{
			idleDotInactivityDurationRemaining--;
			if (idleDotInactivityDurationRemaining == 0)
			{
				// Inactivity period ended, start fade-out
				idleDotFadeOutStartSize = config.dotSize();
				idleDotFadeOutDurationRemaining = config.dotFadeOutDuration();
				if (idleDotFadeOutDurationRemaining == 0) { // If fade-out duration is also 0
				    playerDotOverlay.setShowDot(false);
				    currentActiveDotId = -2; // <--- Set to -2 immediately if no fade-out
				} else {
				    playerDotOverlay.setShowDot(true); // Ensure dot is visible for fade-out
				}
				playerDotOverlay.setCountdownText(null); // Clear countdown during fade-out
			}
			return; // Skip other dot logic during inactivity
		}
		// If inactivity just finished and led to currentActiveDotId = -2, we should not proceed.
		if (currentActiveDotId == -2) {
		    return;
		}


		// --- Priority 3: Handle Idle Dot Active Duration ---
		if (currentActiveDotId == -1 && idleDotActiveDurationRemaining > 0) // Removed config.idleDotDisplayDuration() > 0
		{
			idleDotActiveDurationRemaining--;
			if (idleDotActiveDurationRemaining == 0)
			{
				// Active duration ended, start inactivity or fade-out
				idleDotInactivityDurationRemaining = config.dotInactivityDuration();
				if (config.dotInactivityDuration() == 0)
				{
					// No inactivity period, immediately start fade-out
					idleDotFadeOutStartSize = config.dotSize();
					idleDotFadeOutDurationRemaining = config.dotFadeOutDuration();
					if (idleDotFadeOutDurationRemaining == 0) { // If fade-out duration is also 0
					    playerDotOverlay.setShowDot(false);
					    currentActiveDotId = -2; // <--- Set to -2 immediately if no fade-out
					} else {
					    playerDotOverlay.setShowDot(true); // Ensure dot is visible for fade-out
					}
				}
				playerDotOverlay.setCountdownText(null); // Clear countdown
			}
			return; // Skip other dot logic
		}
		// If active duration just finished and led to currentActiveDotId = -2, we should not proceed.
		if (currentActiveDotId == -2) {
		    return;
		}

		// --- Priority 4: Handle Specific Animation Dot Countdown and Expiry ---
		if (currentActiveDotId > 0) // Specific animation dot is active
		{
			if (specificAnimationTicksRemaining > 0 && specificAnimationTicksRemaining != Integer.MAX_VALUE) // Only countdown if not infinite
			{
				specificAnimationTicksRemaining--;
				if (specificAnimationTicksRemaining == 0)
				{
					// Countdown hit 0, show "0" in zero color for one tick, then immediately switch to idle dot
					playerDotOverlay.setCountdownColor(config.countdownZeroColor());
					playerDotOverlay.setCountdownText("0");
					applyIdleDot(); // Switch to green idle dot
					return; // Exit, as we've transitioned to idle dot
				}
				else // Still counting down
				{
					playerDotOverlay.setCountdownColor(config.countdownColor());
					playerDotOverlay.setCountdownText(String.valueOf(specificAnimationTicksRemaining));
				}
			}
			return; // Skip other dot logic if a specific dot is active
		}

		// --- Priority 5: Movement Override ---
		Player localPlayer = client.getLocalPlayer();
		if (localPlayer != null)
		{
			WorldPoint currentPlayerPoint = localPlayer.getWorldLocation();
			if (previousWorldPoint != null && !previousWorldPoint.equals(currentPlayerPoint))
			{
				isPlayerMoving = true;
				// If player is moving, immediately switch to idle dot
				if (currentActiveDotId != -1) // Only override if not already idle dot
				{
					applyIdleDot();
				}
			}
			else
			{
				isPlayerMoving = false;
			}
			previousWorldPoint = currentPlayerPoint;
		}
		else
		{
			isPlayerMoving = false;
			previousWorldPoint = null; // Reset if player is not available
		}

		// --- Priority 6: Default to Idle Dot if nothing else is active ---
		if (currentActiveDotId == -2 && config.enableDefaultIdleDot())
		{
			applyIdleDot();
		}
		else if (currentActiveDotId == -2) // No dot active and default idle is disabled
		{
			playerDotOverlay.setShowDot(false);
			playerDotOverlay.setCountdownText(null);
		}
	}

	@Subscribe
	public void onAnimationChanged(AnimationChanged event)
	{
		Player localPlayer = client.getLocalPlayer();
		if (localPlayer == null || event.getActor() != localPlayer)
		{
			return;
		}

		int newAnimationId = localPlayer.getAnimation();

		if (lastAnimationId != -1)
		{
			addRecentAnimationHistoryEntry(new AnimationHistoryEntry(lastAnimationId, ticksSinceLastAnimationChange));
		}

		// If the previous animation had a -1 duration (infinite ticks) and a new animation starts,
		// it means the previous animation has ended. Transition to idle dot.
		if (currentActiveDotId > 0 && specificAnimationTicksRemaining == Integer.MAX_VALUE)
		{
			applyIdleDot();
		}

		lastAnimationId = newAnimationId;
		ticksSinceLastAnimationChange = 0;

		if (config.debugAnimationId())
		{
			client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Animation ID: " + newAnimationId, null);
		}

		// Apply player animation config for the new animation
		applyPlayerAnimationConfig(newAnimationId);
	}


	// Method to load player animation configurations
	private List<AnimationColorConfigEntry> loadAnimationColorConfigs()
	{
		String json = config.animationColorConfigs();
		if (json == null || json.isEmpty() || json.equals("[]"))
		{
			animationColorConfigs = new ArrayList<>();
		}
		else
		{
			animationColorConfigs = gson.fromJson(json, animationColorConfigListType);
			animationColorConfigs.forEach(entry -> {
				if (entry.getOpacity() == 0 && entry.getColor() != null) {
					entry.setOpacity(entry.getColor().getAlpha());
					if (entry.getOpacity() == 0) entry.setOpacity(255);
				}
			});
		}
		return animationColorConfigs;
	}

    // Method to save player animation configurations (used for initial add)
	public void saveAnimationColorConfigs(List<AnimationColorConfigEntry> configs)
	{
		this.animationColorConfigs = configs;
		saveCurrentAnimationColorConfigs();
	}

	// Method to save the current internal list of player animation configurations
	public void saveCurrentAnimationColorConfigs()
	{
		String json = gson.toJson(this.animationColorConfigs, animationColorConfigListType);
		config.setAnimationColorConfigs(json);
	}

	private void addRecentAnimationHistoryEntry(AnimationHistoryEntry entry)
	{
		recentAnimationHistory.removeIf(e -> e.getAnimationId() == entry.getAnimationId());
		recentAnimationHistory.addFirst(entry);

		while (recentAnimationHistory.size() > MAX_RECENT_ANIMATIONS_HISTORY)
		{
			recentAnimationHistory.removeLast();
		}
		if (panel != null)
		{
			panel.updateRecentAnimations();
		}
	}

	public List<AnimationHistoryEntry> getRecentAnimationHistory()
	{
		return new ArrayList<>(recentAnimationHistory);
	}

	// New method to recheck and apply player animation config for the current animation
	public void recheckCurrentPlayerAnimation()
	{
		Player localPlayer = client.getLocalPlayer();
		if (localPlayer == null)
		{
			return;
		}
		applyPlayerAnimationConfig(localPlayer.getAnimation());
	}

	// Helper method to apply the idle dot state (green color, no countdown)
	private void applyIdleDot()
	{
		// Reset all idle dot lifecycle states
		idleDotActiveDurationRemaining = 0;
		idleDotInactivityDurationRemaining = 0;
		idleDotFadeOutDurationRemaining = 0;
		idleDotFadeOutStartSize = 0;

		playerDotOverlay.setCurrentRenderDotSize(config.dotSize());

		Color idleColor = config.defaultIdleDotColor();
		int idleOpacity = config.defaultIdleDotOpacity();
		playerDotOverlay.setCurrentColor(new Color(idleColor.getRed(), idleColor.getGreen(), idleColor.getBlue(), idleOpacity));
		playerDotOverlay.setShowDot(true); // Always show when transitioning to idle state
		currentActiveDotId = -1; // Indicate default idle dot

		// Handle idle dot display duration
		if (config.idleDotDisplayDuration() > 0)
		{
			idleDotActiveDurationRemaining = config.idleDotDisplayDuration();
		}
		else if (config.idleDotDisplayDuration() == -1) // Infinite duration
		{
			idleDotActiveDurationRemaining = Integer.MAX_VALUE;
		}
		else // config.idleDotDisplayDuration() == 0
		{
			// Active duration is 0, immediately transition to inactivity or fade-out
			idleDotInactivityDurationRemaining = config.dotInactivityDuration();
			if (config.dotInactivityDuration() == 0)
			{
				idleDotFadeOutStartSize = config.dotSize();
				idleDotFadeOutDurationRemaining = config.dotFadeOutDuration();
				if (idleDotFadeOutDurationRemaining == 0) {
				    // If all durations are 0, hide the dot immediately
				    playerDotOverlay.setShowDot(false);
				    currentActiveDotId = -2;
				}
			}
		}

		playerDotOverlay.setCountdownText(null); // Clear countdown for idle dot
		playerDotOverlay.setCountdownColor(config.countdownColor()); // Reset countdown color
	}

	// Helper method to apply player animation configuration
	private void applyPlayerAnimationConfig(int animationId)
	{
		// Reset all idle dot lifecycle states
		idleDotActiveDurationRemaining = 0;
		idleDotInactivityDurationRemaining = 0;
		idleDotFadeOutDurationRemaining = 0;
		idleDotFadeOutStartSize = 0;

		playerDotOverlay.setCurrentRenderDotSize(config.dotSize());

		AnimationColorConfigEntry matchedEntry = null;
		for (AnimationColorConfigEntry entry : animationColorConfigs)
		{
			if (entry.getAnimationId() == animationId)
			{
				matchedEntry = entry;
				break;
			}
		}

		if (matchedEntry != null)
		{
			// Apply specific animation dot
			Color dotColor = new Color(matchedEntry.getColor().getRed(), matchedEntry.getColor().getGreen(), matchedEntry.getColor().getBlue(), matchedEntry.getOpacity());
			int dotDuration = matchedEntry.getTickDuration();

			playerDotOverlay.setCurrentColor(dotColor);
			playerDotOverlay.setShowDot(true);
			currentActiveDotId = animationId; // Store the animation ID that set this dot
			playerDotOverlay.setCountdownColor(config.countdownColor()); // Set initial countdown color

			if (dotDuration > 0)
			{
				specificAnimationTicksRemaining = dotDuration;
				if (config.countdownDisplayMode() == ActionAvailabilityConfig.CountdownDisplayMode.COUNTDOWN_ONLY || config.countdownDisplayMode() == ActionAvailabilityConfig.CountdownDisplayMode.BOTH)
				{
					playerDotOverlay.setCountdownText(String.valueOf(dotDuration));
				} else {
					playerDotOverlay.setCountdownText(null);
				}
			}
			else if (dotDuration == -1) // Fallback to actual animation duration
			{
				// The dot should remain the animation's color until onAnimationChanged detects a new animation.
				// We set it to MAX_VALUE so it doesn't count down in onGameTick.
				specificAnimationTicksRemaining = Integer.MAX_VALUE;
				playerDotOverlay.setCountdownText(null); // No countdown for -1 duration
			}
			else // dotDuration == 0 or other invalid, treat as no specific duration
			{
				specificAnimationTicksRemaining = 0; // Will immediately transition to idle dot on next tick
				playerDotOverlay.setCountdownText(null);
			}
		}
		else // No matched entry for the current animationId
		{
			// If no specific animation matches, and no dot is currently active, then transition to idle dot.
			if (currentActiveDotId == -2)
			{
				applyIdleDot();
			}
		}
	}

	@Provides
	ActionAvailabilityConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ActionAvailabilityConfig.class);
	}
}

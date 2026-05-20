package com.JFroggy;

import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.components.ColorJButton;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ActionAvailabilityPanel extends PluginPanel
{
    private final ActionAvailabilityMain plugin;
    private final JTextField nameField; // New field for configuration name
    private final JTextField animationIdField;
    private final ColorJButton colorButton;
    private final JTextField tickDurationField;
    private final JPanel configListPanel;
    private final JPanel recentAnimationsPanel; // Panel for recent animations content
    private final JLabel recentAnimationsLabel; // Label to display recent IDs

    public ActionAvailabilityPanel(ActionAvailabilityMain plugin)
    {
        this.plugin = plugin;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // --- Recent Player Animations Section ---
        // Wrapper panel for centering
        JPanel recentAnimationsWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        recentAnimationsPanel = new JPanel();
        recentAnimationsPanel.setLayout(new BoxLayout(recentAnimationsPanel, BoxLayout.Y_AXIS));
        recentAnimationsPanel.setBorder(BorderFactory.createTitledBorder("Recent Player Animations (ID: Ticks)"));
        recentAnimationsPanel.setToolTipText("Shows the last 3 unique animation IDs and how many ticks they lasted.");
        // Set fixed size for recent animations panel
        recentAnimationsPanel.setPreferredSize(new Dimension(200, 100));
        recentAnimationsPanel.setMinimumSize(new Dimension(200, 100));
        recentAnimationsPanel.setMaximumSize(new Dimension(200, 100)); // Also set max to prevent expansion

        recentAnimationsLabel = new JLabel("No recent animations.");
        recentAnimationsPanel.add(recentAnimationsLabel);
        recentAnimationsWrapper.add(recentAnimationsPanel); // Add content panel to wrapper dimension
        add(recentAnimationsWrapper); // Add wrapper to main panel
        add(Box.createVerticalStrut(10)); // Spacer

        // --- Input Panel for adding new Player Animation configurations ---
        JPanel playerAnimInputPanel = new JPanel(new GridBagLayout());
        playerAnimInputPanel.setBorder(BorderFactory.createTitledBorder("Player Animation Config"));
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(5, 5, 5, 5);

        // Name
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0;
        JLabel nameLabel = new JLabel("Name:");
        nameLabel.setToolTipText("Enter a name for this configuration.");
        playerAnimInputPanel.add(nameLabel, c);
        c.gridx = 1;
        c.weightx = 1.0;
        nameField = new JTextField(10);
        nameField.setToolTipText("e.g., 'Attack', 'Idle', 'Woodcutting'");
        playerAnimInputPanel.add(nameField, c);

        // Animation ID
        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 0;
        JLabel animationIdLabel = new JLabel("Animation ID:");
        animationIdLabel.setToolTipText("Enter the animation ID to configure.");
        playerAnimInputPanel.add(animationIdLabel, c);
        c.gridx = 1;
        c.weightx = 1.0;
        animationIdField = new JTextField(10);
        animationIdField.setToolTipText("Enter the animation ID (e.g., -1 for idle).");
        playerAnimInputPanel.add(animationIdField, c);

        // Color
        c.gridx = 0;
        c.gridy = 2;
        c.weightx = 0;
        JLabel colorLabel = new JLabel("Color:");
        colorLabel.setToolTipText("Choose the color for the dot when this animation is active.");
        playerAnimInputPanel.add(colorLabel, c);
        c.gridx = 1;
        c.weightx = 1.0;
        colorButton = new ColorJButton("", Color.RED); // Default color for new config
        colorButton.setBackground(Color.RED);
        colorButton.setToolTipText("Click to change color for new configuration.");
        colorButton.addActionListener(e ->
        {
            Color newColor = JColorChooser.showDialog(this, "Choose Color", colorButton.getBackground());
            if (newColor != null)
            {
                colorButton.setBackground(newColor);
            }
        });
        playerAnimInputPanel.add(colorButton, c);

        // Tick Duration
        c.gridx = 0;
        c.gridy = 3;
        c.weightx = 0;
        JLabel tickDurationLabel = new JLabel("Tick Duration:");
        tickDurationLabel.setToolTipText("Enter how many game ticks the dot should be visible for this animation.");
        playerAnimInputPanel.add(tickDurationLabel, c);
        c.gridx = 1;
        c.weightx = 1.0;
        tickDurationField = new JTextField("1", 10); // Default to "1"
        tickDurationField.setToolTipText("Enter the duration in game ticks (1 tick = 0.6 seconds).");
        playerAnimInputPanel.add(tickDurationField, c);

        // Add Button
        c.gridx = 0;
        c.gridy = 4;
        c.gridwidth = 2;
        c.weightx = 1.0;
        JButton addButton = new JButton("Add Configuration");
        addButton.setToolTipText("Add a new animation ID configuration.");
        addButton.addActionListener(e -> addPlayerAnimationConfiguration());
        playerAnimInputPanel.add(addButton, c);

        add(playerAnimInputPanel);
        add(Box.createVerticalStrut(10)); // Spacer

        // --- Player Animation Config List Panel ---
        configListPanel = new JPanel();
        configListPanel.setLayout(new BoxLayout(configListPanel, BoxLayout.Y_AXIS));
        add(configListPanel);
        add(Box.createVerticalStrut(10)); // Spacer

        rebuildConfigListPanel(); // Player animation configs
        updateRecentAnimations();
    }

    private void addPlayerAnimationConfiguration()
    {
        try
        {
            String name = nameField.getText().trim();
            if (name.isEmpty())
            {
                name = "New Config " + (plugin.getAnimationColorConfigs().size() + 1); // Generate a default name
            }
            int animationId = Integer.parseInt(animationIdField.getText());
            Color color = colorButton.getBackground();
            int tickDuration = Integer.parseInt(tickDurationField.getText());
            int opacity = color.getAlpha(); // Use the alpha from the chosen color

            List<AnimationColorConfigEntry> configs = plugin.getAnimationColorConfigs(); // Get live list
            configs.add(new AnimationColorConfigEntry(name, animationId, color, tickDuration, opacity));
            plugin.saveCurrentAnimationColorConfigs(); // Save the live list

            nameField.setText("");
            animationIdField.setText("");
            tickDurationField.setText("1"); // Reset to default "1"
            colorButton.setBackground(Color.RED); // Reset to default for next entry

            rebuildConfigListPanel();
            plugin.recheckCurrentPlayerAnimation(); // Recheck current animation after adding new config
        }
        catch (NumberFormatException ex)
        {
            JOptionPane.showMessageDialog(this, "Please enter valid numbers for Animation ID and Tick Duration.", "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void removePlayerAnimationConfiguration(AnimationColorConfigEntry entryToRemove)
    {
        List<AnimationColorConfigEntry> configs = plugin.getAnimationColorConfigs(); // Get live list
        configs.remove(entryToRemove);
        plugin.saveCurrentAnimationColorConfigs(); // Save the live list
        rebuildConfigListPanel();
        plugin.recheckCurrentPlayerAnimation(); // Recheck current animation after removing config
    }

    private void rebuildConfigListPanel()
    {
        configListPanel.removeAll();
        List<AnimationColorConfigEntry> configs = plugin.getAnimationColorConfigs(); // Get live list

        if (configs.isEmpty())
        {
            configListPanel.add(new JLabel("No player animation configurations added yet."));
        }
        else
        {
            for (AnimationColorConfigEntry entry : configs)
            {
                // Pass the live entry object to the ConfigEntryPanel
                configListPanel.add(new ConfigEntryPanel(plugin, entry, this::removePlayerAnimationConfiguration));
                configListPanel.add(Box.createVerticalStrut(5));
            }
        }
        configListPanel.revalidate();
        configListPanel.repaint();
        revalidate();
        repaint();
    }

    public void updateRecentAnimations()
    {
        List<ActionAvailabilityMain.AnimationHistoryEntry> recentHistory = plugin.getRecentAnimationHistory();
        if (recentHistory.isEmpty())
        {
            recentAnimationsLabel.setText("No recent animations.");
        }
        else
        {
            StringBuilder sb = new StringBuilder("<html>");
            for (int i = 0; i < recentHistory.size(); i++)
            {
                ActionAvailabilityMain.AnimationHistoryEntry entry = recentHistory.get(i);
                sb.append(entry.getAnimationId()).append(": ").append(entry.getDurationTicks()).append(" ticks");
                if (i < recentHistory.size() - 1)
                {
                    sb.append("<br>");
                }
            }
            sb.append("</html>");
            recentAnimationsLabel.setText(sb.toString());
        }
        recentAnimationsPanel.revalidate();
        recentAnimationsPanel.repaint();
    }

    private class ConfigEntryPanel extends JPanel
    {
        private final ActionAvailabilityMain plugin;
        private final AnimationColorConfigEntry entry; // This is now a direct reference to the live object
        private final JTextField nameEditField; // New field for editing name
        private final JTextField animationIdEditField;
        private final ColorJButton colorEditButton;
        private final JTextField tickDurationEditField;
        private final JLabel hexColorLabel; // New label for hex color
        private final java.util.function.Consumer<AnimationColorConfigEntry> removeCallback;

        public ConfigEntryPanel(ActionAvailabilityMain plugin, AnimationColorConfigEntry entry, java.util.function.Consumer<AnimationColorConfigEntry> removeCallback)
        {
            this.plugin = plugin;
            this.entry = entry; // Direct reference to the object in the main list
            this.removeCallback = removeCallback;

            setLayout(new GridBagLayout());
            setBorder(BorderFactory.createLineBorder(Color.GRAY));
            setPreferredSize(new Dimension(250, 150)); // Adjusted size for new name field
            setMinimumSize(new Dimension(250, 150));

            GridBagConstraints c = new GridBagConstraints();
            c.fill = GridBagConstraints.HORIZONTAL;
            c.insets = new Insets(5, 5, 5, 5);

            // Name Label
            c.gridx = 0;
            c.gridy = 0;
            c.weightx = 0;
            c.anchor = GridBagConstraints.WEST;
            JLabel nameLabel = new JLabel("Name:");
            nameLabel.setToolTipText("Name of this configuration.");
            add(nameLabel, c);

            // Name Field
            c.gridx = 1;
            c.gridy = 0;
            c.weightx = 1.0;
            nameEditField = new JTextField(8);
            nameEditField.setText(entry.getName() != null ? entry.getName() : "");
            nameEditField.setToolTipText("Edit the configuration name.");
            nameEditField.addFocusListener(new FocusAdapter()
            {
                @Override
                public void focusLost(FocusEvent e)
                {
                    String newName = nameEditField.getText().trim();
                    if (!newName.equals(entry.getName()))
                    {
                        entry.setName(newName);
                        saveChanges();
                    }
                }
            });
            add(nameEditField, c);

            // Remove Button (X)
            c.gridx = 2;
            c.gridy = 0;
            c.weightx = 0;
            c.fill = GridBagConstraints.NONE;
            c.anchor = GridBagConstraints.NORTHEAST;
            JButton removeButton = new JButton("X");
            removeButton.setMargin(new Insets(1, 4, 1, 4));
            removeButton.setToolTipText("Remove this configuration.");
            removeButton.addActionListener(e -> removeCallback.accept(entry));
            add(removeButton, c);

            // Animation ID Label
            c.gridx = 0;
            c.gridy = 1;
            c.weightx = 0;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.anchor = GridBagConstraints.WEST;
            JLabel idLabel = new JLabel("ID:");
            idLabel.setToolTipText("The animation ID.");
            add(idLabel, c);

            // Animation ID Field
            c.gridx = 1;
            c.gridy = 1;
            c.gridwidth = 2; // Span across two columns
            c.weightx = 1.0;
            animationIdEditField = new JTextField(8);
            animationIdEditField.setText(String.valueOf(entry.getAnimationId()));
            animationIdEditField.setToolTipText("Edit the animation ID.");
            animationIdEditField.addFocusListener(new FocusAdapter()
            {
                @Override
                public void focusLost(FocusEvent e)
                {
                    try
                    {
                        int newId = Integer.parseInt(animationIdEditField.getText());
                        // Only update if the value actually changed
                        if (newId != entry.getAnimationId())
                        {
                            entry.setAnimationId(newId); // Modify the live object
                            saveChanges();
                        }
                    }
                    catch (NumberFormatException ex)
                    {
                        JOptionPane.showMessageDialog(ConfigEntryPanel.this, "Invalid Animation ID.", "Input Error", JOptionPane.ERROR_MESSAGE);
                        animationIdEditField.setText(String.valueOf(entry.getAnimationId())); // Revert UI on error
                    }
                }
            });
            add(animationIdEditField, c);

            // Tick Duration Label
            c.gridx = 0;
            c.gridy = 2;
            c.gridwidth = 1;
            c.weightx = 0;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.anchor = GridBagConstraints.WEST;
            JLabel ticksLabel = new JLabel("Ticks:");
            ticksLabel.setToolTipText("The duration in game ticks.");
            add(ticksLabel, c);

            // Tick Duration Field
            c.gridx = 1;
            c.gridy = 2;
            c.gridwidth = 2;
            c.weightx = 1.0;
            tickDurationEditField = new JTextField(8);
            tickDurationEditField.setText(String.valueOf(entry.getTickDuration()));
            tickDurationEditField.setToolTipText("Edit the duration in game ticks.");
            tickDurationEditField.addFocusListener(new FocusAdapter()
            {
                @Override
                public void focusLost(FocusEvent e)
                {
                    try
                    {
                        int newDuration = Integer.parseInt(tickDurationEditField.getText());
                        // Only update if the value actually changed
                        if (newDuration != entry.getTickDuration())
                        {
                            entry.setTickDuration(newDuration); // Modify the live object
                            saveChanges();
                        }
                    }
                    catch (NumberFormatException ex)
                    {
                        JOptionPane.showMessageDialog(ConfigEntryPanel.this, "Invalid Tick Duration.", "Input Error", JOptionPane.ERROR_MESSAGE);
                        tickDurationEditField.setText(String.valueOf(entry.getTickDuration())); // Revert UI on error
                    }
                }
            });
            add(tickDurationEditField, c);

            // Color Label
            c.gridx = 0;
            c.gridy = 3;
            c.gridwidth = 1;
            c.weightx = 0;
            c.anchor = GridBagConstraints.WEST;
            JLabel colorEditLabel = new JLabel("Color:");
            colorEditLabel.setToolTipText("The color of the dot for this animation.");
            add(colorEditLabel, c);

            // Color Button
            c.gridx = 1;
            c.gridy = 3;
            c.weightx = 0; // Don't let button expand
            colorEditButton = new ColorJButton("", Color.black); // Empty text, color will be background
            colorEditButton.setBackground(entry.getColor());
            colorEditButton.setPreferredSize(new Dimension(25, 25));
            colorEditButton.setMinimumSize(new Dimension(25, 25));
            colorEditButton.setToolTipText("Click to change the color.");
            colorEditButton.addActionListener(e ->
            {
                Color newColor = JColorChooser.showDialog(ConfigEntryPanel.this, "Choose Color", colorEditButton.getBackground());
                if (newColor != null && !newColor.equals(entry.getColor()))
                {
                    entry.setColor(newColor);
                    entry.setOpacity(newColor.getAlpha()); // Update opacity when color changes
                    colorEditButton.setBackground(newColor);
                    updateHexColorLabel(newColor); // Update hex label
                    saveChanges();
                }
            });
            add(colorEditButton, c);

            // Hex Color Label
            c.gridx = 2;
            c.gridy = 3;
            c.weightx = 1.0; // Let hex label take remaining space
            hexColorLabel = new JLabel();
            updateHexColorLabel(entry.getColor()); // Initialize hex label
            add(hexColorLabel, c);
        }

        private void updateHexColorLabel(Color color)
        {
            hexColorLabel.setText(String.format("#%06X", (color.getRGB() & 0xFFFFFF)));
        }

        private void saveChanges()
        {
            // Save the plugin's internal list of configurations, which already contains the updated 'entry'
            plugin.saveCurrentAnimationColorConfigs();
            // Recheck the current animation to apply the new config immediately
            plugin.recheckCurrentPlayerAnimation();
            // No need to rebuild the entire panel here, as changes are applied directly to the entry
            // and only affect this specific ConfigEntryPanel's display.
        }
    }
}

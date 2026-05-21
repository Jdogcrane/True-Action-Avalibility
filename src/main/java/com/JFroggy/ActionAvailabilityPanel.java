package com.JFroggy;

import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.components.ColorJButton;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.List;
import java.util.function.Consumer;

public class ActionAvailabilityPanel extends PluginPanel
{
    private final ActionAvailabilityMain plugin;

    // Animation Config Fields
    private final JTextField animNameField;
    private final JTextField animationIdField;
    private final ColorJButton animColorButton;
    private final JTextField animTickDurationField;
    private final JPanel animConfigListPanel;

    // Interaction Config Fields
    private final JTextField interactionNameField;
    private final JTextField targetIdField;
    private final ColorJButton interactionColorButton; // Removed menuOptionField
    private final JTextField interactionTickDurationField;
    private final JPanel interactionConfigListPanel;

    // Recent Animations Panel
    private final JPanel recentAnimationsPanel;
    private final JLabel recentAnimationsLabel;

    // Recent Interactions Panel
    private final JPanel recentInteractionsPanel;
    private final JLabel recentInteractionsLabel;

    public ActionAvailabilityPanel(ActionAvailabilityMain plugin)
    {
        this.plugin = plugin;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // --- Recent Player Animations Section ---
        JPanel recentAnimationsWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        recentAnimationsPanel = new JPanel();
        recentAnimationsPanel.setLayout(new BoxLayout(recentAnimationsPanel, BoxLayout.Y_AXIS));
        recentAnimationsPanel.setBorder(BorderFactory.createTitledBorder("Recent Player Animations (ID: Ticks)"));
        recentAnimationsPanel.setToolTipText("Shows the last 3 unique animation IDs and how many ticks they lasted.");
        recentAnimationsPanel.setPreferredSize(new Dimension(200, 100));
        recentAnimationsPanel.setMinimumSize(new Dimension(200, 100));
        recentAnimationsPanel.setMaximumSize(new Dimension(200, 100));

        recentAnimationsLabel = new JLabel("No recent animations.");
        recentAnimationsPanel.add(recentAnimationsLabel);
        recentAnimationsWrapper.add(recentAnimationsPanel);
        add(recentAnimationsWrapper);
        add(Box.createVerticalStrut(10));

        // --- Recent Interactions Section ---
        JPanel recentInteractionsWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        recentInteractionsPanel = new JPanel();
        recentInteractionsPanel.setLayout(new BoxLayout(recentInteractionsPanel, BoxLayout.Y_AXIS));
        recentInteractionsPanel.setBorder(BorderFactory.createTitledBorder("Recent Interactions"));
        recentInteractionsPanel.setToolTipText("Shows the last 3 unique interactions and how many ticks they lasted.");
        recentInteractionsPanel.setPreferredSize(new Dimension(200, 100));
        recentInteractionsPanel.setMinimumSize(new Dimension(200, 100));
        recentInteractionsPanel.setMaximumSize(new Dimension(200, 100));

        recentInteractionsLabel = new JLabel("No recent interactions.");
        recentInteractionsPanel.add(recentInteractionsLabel);
        recentInteractionsWrapper.add(recentInteractionsPanel);
        add(recentInteractionsWrapper);
        add(Box.createVerticalStrut(10));


        // --- Input Panel for adding new Player Animation configurations ---
        JPanel playerAnimInputPanel = new JPanel(new GridBagLayout());
        playerAnimInputPanel.setBorder(BorderFactory.createTitledBorder("Player Animation Config"));
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(5, 5, 5, 5);

        // Name
        c.gridx = 0; c.gridy = 0; c.weightx = 0;
        playerAnimInputPanel.add(new JLabel("Name:"), c);
        c.gridx = 1; c.weightx = 1.0;
        animNameField = new JTextField(10);
        animNameField.setToolTipText("e.g., 'Attack', 'Idle', 'Woodcutting'");
        playerAnimInputPanel.add(animNameField, c);

        // Animation ID
        c.gridx = 0; c.gridy = 1; c.weightx = 0;
        playerAnimInputPanel.add(new JLabel("Animation ID:"), c);
        c.gridx = 1; c.weightx = 1.0;
        animationIdField = new JTextField(10);
        animationIdField.setToolTipText("Enter the animation ID (e.g., -1 for idle).");
        playerAnimInputPanel.add(animationIdField, c);

        // Color
        c.gridx = 0; c.gridy = 2; c.weightx = 0;
        playerAnimInputPanel.add(new JLabel("Color:"), c);
        c.gridx = 1; c.weightx = 1.0;
        animColorButton = new ColorJButton("", Color.RED);
        animColorButton.setBackground(Color.RED);
        animColorButton.setToolTipText("Click to change color for new configuration.");
        animColorButton.addActionListener(e ->
        {
            Color newColor = JColorChooser.showDialog(this, "Choose Color", animColorButton.getBackground());
            if (newColor != null) { animColorButton.setBackground(newColor); }
        });
        playerAnimInputPanel.add(animColorButton, c);

        // Tick Duration
        c.gridx = 0; c.gridy = 3; c.weightx = 0;
        playerAnimInputPanel.add(new JLabel("Tick Duration:"), c);
        c.gridx = 1; c.weightx = 1.0;
        animTickDurationField = new JTextField("1", 10);
        animTickDurationField.setToolTipText("Enter the duration in game ticks (1 tick = 0.6 seconds).");
        playerAnimInputPanel.add(animTickDurationField, c);

        // Add Button
        c.gridx = 0; c.gridy = 4; c.gridwidth = 2; c.weightx = 1.0;
        JButton addAnimButton = new JButton("Add Animation Configuration");
        addAnimButton.setToolTipText("Add a new animation ID configuration.");
        addAnimButton.addActionListener(e -> addPlayerAnimationConfiguration());
        playerAnimInputPanel.add(addAnimButton, c);

        add(playerAnimInputPanel);
        add(Box.createVerticalStrut(10));

        // --- Player Animation Config List Panel ---
        animConfigListPanel = new JPanel();
        animConfigListPanel.setLayout(new BoxLayout(animConfigListPanel, BoxLayout.Y_AXIS));
        add(animConfigListPanel);
        add(Box.createVerticalStrut(10));

        // --- Input Panel for adding new Interaction Trigger configurations ---
        JPanel interactionInputPanel = new JPanel(new GridBagLayout());
        interactionInputPanel.setBorder(BorderFactory.createTitledBorder("Interaction Trigger Config"));
        GridBagConstraints ic = new GridBagConstraints();
        ic.fill = GridBagConstraints.HORIZONTAL;
        ic.insets = new Insets(5, 5, 5, 5);

        // Name
        ic.gridx = 0; ic.gridy = 0; ic.weightx = 0;
        interactionInputPanel.add(new JLabel("Name:"), ic);
        ic.gridx = 1; ic.weightx = 1.0;
        interactionNameField = new JTextField(10);
        interactionNameField.setToolTipText("e.g., 'Attack Goblin', 'Use Pot', 'Talk to NPC'");
        interactionInputPanel.add(interactionNameField, ic);

        // Target ID
        ic.gridx = 0; ic.gridy = 1; ic.weightx = 0;
        interactionInputPanel.add(new JLabel("Target ID:"), ic);
        ic.gridx = 1; ic.weightx = 1.0;
        targetIdField = new JTextField(10);
        targetIdField.setToolTipText("Enter the target ID (NPC, Object, Item). Use -1 for any ID.");
        interactionInputPanel.add(targetIdField, ic);

        // Color
        ic.gridx = 0; ic.gridy = 2; ic.weightx = 0; // Adjusted gridy
        interactionInputPanel.add(new JLabel("Color:"), ic);
        ic.gridx = 1; ic.weightx = 1.0;
        interactionColorButton = new ColorJButton("", Color.BLUE); // Default color for new config
        interactionColorButton.setBackground(Color.BLUE);
        interactionColorButton.setToolTipText("Click to change color for new configuration.");
        interactionColorButton.addActionListener(e ->
        {
            Color newColor = JColorChooser.showDialog(this, "Choose Color", interactionColorButton.getBackground());
            if (newColor != null) { interactionColorButton.setBackground(newColor); }
        });
        interactionInputPanel.add(interactionColorButton, ic);

        // Tick Duration
        ic.gridx = 0; ic.gridy = 3; ic.weightx = 0; // Adjusted gridy
        interactionInputPanel.add(new JLabel("Tick Duration:"), ic);
        ic.gridx = 1; ic.weightx = 1.0;
        interactionTickDurationField = new JTextField("1", 10);
        interactionTickDurationField.setToolTipText("Enter the duration in game ticks (1 tick = 0.6 seconds).");
        interactionInputPanel.add(interactionTickDurationField, ic);

        // Add Button
        ic.gridx = 0; ic.gridy = 4; ic.gridwidth = 2; ic.weightx = 1.0; // Adjusted gridy
        JButton addInteractionButton = new JButton("Add Interaction Configuration");
        addInteractionButton.setToolTipText("Add a new interaction trigger configuration.");
        addInteractionButton.addActionListener(e -> addInteractionConfiguration());
        interactionInputPanel.add(addInteractionButton, ic);

        add(interactionInputPanel);
        add(Box.createVerticalStrut(10));

        // --- Interaction Config List Panel ---
        interactionConfigListPanel = new JPanel();
        interactionConfigListPanel.setLayout(new BoxLayout(interactionConfigListPanel, BoxLayout.Y_AXIS));
        add(interactionConfigListPanel);
        add(Box.createVerticalStrut(10));

        rebuildAnimConfigListPanel();
        rebuildInteractionConfigListPanel();
        updateRecentAnimations();
        updateRecentInteractions(); // Call new update method
    }

    private void addPlayerAnimationConfiguration()
    {
        try
        {
            String name = animNameField.getText().trim();
            if (name.isEmpty())
            {
                name = "New Anim Config " + (plugin.getAnimationColorConfigs().size() + 1);
            }
            int animationId = Integer.parseInt(animationIdField.getText());
            Color color = animColorButton.getBackground();
            int tickDuration = Integer.parseInt(animTickDurationField.getText());
            int opacity = color.getAlpha();

            List<AnimationColorConfigEntry> configs = plugin.getAnimationColorConfigs();
            configs.add(new AnimationColorConfigEntry(name, animationId, color, tickDuration, opacity));
            plugin.saveCurrentAnimationColorConfigs();

            animNameField.setText("");
            animationIdField.setText("");
            animTickDurationField.setText("1");
            animColorButton.setBackground(Color.RED);

            rebuildAnimConfigListPanel();
            plugin.recheckCurrentPlayerAnimation();
        }
        catch (NumberFormatException ex)
        {
            JOptionPane.showMessageDialog(this, "Please enter valid numbers for Animation ID and Tick Duration.", "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void removePlayerAnimationConfiguration(AnimationColorConfigEntry entryToRemove)
    {
        List<AnimationColorConfigEntry> configs = plugin.getAnimationColorConfigs();
        configs.remove(entryToRemove);
        plugin.saveCurrentAnimationColorConfigs();
        rebuildAnimConfigListPanel();
        plugin.recheckCurrentPlayerAnimation();
    }

    private void rebuildAnimConfigListPanel()
    {
        animConfigListPanel.removeAll();
        List<AnimationColorConfigEntry> configs = plugin.getAnimationColorConfigs();

        if (configs.isEmpty())
        {
            animConfigListPanel.add(new JLabel("No player animation configurations added yet."));
        }
        else
        {
            for (AnimationColorConfigEntry entry : configs)
            {
                animConfigListPanel.add(new AnimConfigEntryPanel(plugin, entry, this::removePlayerAnimationConfiguration));
                animConfigListPanel.add(Box.createVerticalStrut(5));
            }
        }
        animConfigListPanel.revalidate();
        animConfigListPanel.repaint();
        revalidate();
        repaint();
    }

    private void addInteractionConfiguration()
    {
        try
        {
            String name = interactionNameField.getText().trim();
            if (name.isEmpty())
            {
                name = "New Interaction Config " + (plugin.getInteractionTriggerConfigs().size() + 1);
            }
            int targetId = Integer.parseInt(targetIdField.getText());
            Color color = interactionColorButton.getBackground();
            int tickDuration = Integer.parseInt(interactionTickDurationField.getText());
            int opacity = color.getAlpha();

            List<InteractionConfigEntry> configs = plugin.getInteractionTriggerConfigs();
            configs.add(new InteractionConfigEntry(name, targetId, color, tickDuration, opacity)); // Removed menuOption
            plugin.saveCurrentInteractionTriggerConfigs();

            interactionNameField.setText("");
            targetIdField.setText("");
            interactionTickDurationField.setText("1");
            interactionColorButton.setBackground(Color.BLUE);

            rebuildInteractionConfigListPanel();
            // No direct recheck for interactions needed here, as they are event-driven
        }
        catch (NumberFormatException ex)
        {
            JOptionPane.showMessageDialog(this, "Please enter valid numbers for Target ID and Tick Duration.", "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void removeInteractionConfiguration(InteractionConfigEntry entryToRemove)
    {
        List<InteractionConfigEntry> configs = plugin.getInteractionTriggerConfigs();
        configs.remove(entryToRemove);
        plugin.saveCurrentInteractionTriggerConfigs();
        rebuildInteractionConfigListPanel();
    }

    private void rebuildInteractionConfigListPanel()
    {
        interactionConfigListPanel.removeAll();
        List<InteractionConfigEntry> configs = plugin.getInteractionTriggerConfigs();

        if (configs.isEmpty())
        {
            interactionConfigListPanel.add(new JLabel("No interaction trigger configurations added yet."));
        }
        else
        {
            for (InteractionConfigEntry entry : configs)
            {
                interactionConfigListPanel.add(new InteractionConfigEntryPanel(plugin, entry, this::removeInteractionConfiguration));
                interactionConfigListPanel.add(Box.createVerticalStrut(5));
            }
        }
        interactionConfigListPanel.revalidate();
        interactionConfigListPanel.repaint();
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

    public void updateRecentInteractions()
    {
        List<ActionAvailabilityMain.InteractionHistoryEntry> recentHistory = plugin.getRecentInteractionHistory();
        if (recentHistory.isEmpty())
        {
            recentInteractionsLabel.setText("No recent interactions.");
        }
        else
        {
            StringBuilder sb = new StringBuilder("<html>");
            for (int i = 0; i < recentHistory.size(); i++)
            {
                ActionAvailabilityMain.InteractionHistoryEntry entry = recentHistory.get(i);
                sb.append(entry.getMenuOption());
                if (entry.getMenuTarget() != null && !entry.getMenuTarget().isEmpty())
                {
                    sb.append(" ").append(entry.getMenuTarget());
                }
                sb.append(" (ID: ").append(entry.getTargetId()).append("): ").append(entry.getDurationTicks()).append(" ticks");
                if (i < recentHistory.size() - 1)
                {
                    sb.append("<br>");
                }
            }
            sb.append("</html>");
            recentInteractionsLabel.setText(sb.toString());
        }
        recentInteractionsPanel.revalidate();
        recentInteractionsPanel.repaint();
    }

    // Renamed from ConfigEntryPanel to AnimConfigEntryPanel
    private class AnimConfigEntryPanel extends JPanel
    {
        private final ActionAvailabilityMain plugin;
        private final AnimationColorConfigEntry entry;
        private final JTextField nameEditField;
        private final JTextField animationIdEditField;
        private final ColorJButton colorEditButton;
        private final JTextField tickDurationEditField;
        private final JLabel hexColorLabel;
        private final Consumer<AnimationColorConfigEntry> removeCallback;

        public AnimConfigEntryPanel(ActionAvailabilityMain plugin, AnimationColorConfigEntry entry, Consumer<AnimationColorConfigEntry> removeCallback)
        {
            this.plugin = plugin;
            this.entry = entry;
            this.removeCallback = removeCallback;

            setLayout(new GridBagLayout());
            setBorder(BorderFactory.createLineBorder(Color.GRAY));
            setPreferredSize(new Dimension(250, 150));
            setMinimumSize(new Dimension(250, 150));

            GridBagConstraints c = new GridBagConstraints();
            c.fill = GridBagConstraints.HORIZONTAL;
            c.insets = new Insets(5, 5, 5, 5);

            // Name Label
            c.gridx = 0; c.gridy = 0; c.weightx = 0; c.anchor = GridBagConstraints.WEST;
            add(new JLabel("Name:"), c);

            // Name Field
            c.gridx = 1; c.gridy = 0; c.weightx = 1.0;
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
            c.gridx = 2; c.gridy = 0; c.weightx = 0; c.fill = GridBagConstraints.NONE; c.anchor = GridBagConstraints.NORTHEAST;
            JButton removeButton = new JButton("X");
            removeButton.setMargin(new Insets(1, 4, 1, 4));
            removeButton.setToolTipText("Remove this configuration.");
            removeButton.addActionListener(e -> removeCallback.accept(entry));
            add(removeButton, c);

            // Animation ID Label
            c.gridx = 0; c.gridy = 1; c.weightx = 0; c.fill = GridBagConstraints.HORIZONTAL; c.anchor = GridBagConstraints.WEST;
            add(new JLabel("ID:"), c);

            // Animation ID Field
            c.gridx = 1; c.gridy = 1; c.gridwidth = 2; c.weightx = 1.0;
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
                        if (newId != entry.getAnimationId())
                        {
                            entry.setAnimationId(newId);
                            saveChanges();
                        }
                    }
                    catch (NumberFormatException ex)
                    {
                        JOptionPane.showMessageDialog(AnimConfigEntryPanel.this, "Invalid Animation ID.", "Input Error", JOptionPane.ERROR_MESSAGE);
                        animationIdEditField.setText(String.valueOf(entry.getAnimationId()));
                    }
                }
            });
            add(animationIdEditField, c);

            // Tick Duration Label
            c.gridx = 0; c.gridy = 2; c.gridwidth = 1; c.weightx = 0; c.fill = GridBagConstraints.HORIZONTAL; c.anchor = GridBagConstraints.WEST;
            add(new JLabel("Ticks:"), c);

            // Tick Duration Field
            c.gridx = 1; c.gridy = 2; c.gridwidth = 2; c.weightx = 1.0;
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
                        if (newDuration != entry.getTickDuration())
                        {
                            entry.setTickDuration(newDuration);
                            saveChanges();
                        }
                    }
                    catch (NumberFormatException ex)
                    {
                        JOptionPane.showMessageDialog(AnimConfigEntryPanel.this, "Invalid Tick Duration.", "Input Error", JOptionPane.ERROR_MESSAGE);
                        tickDurationEditField.setText(String.valueOf(entry.getTickDuration()));
                    }
                }
            });
            add(tickDurationEditField, c);

            // Color Label
            c.gridx = 0; c.gridy = 3; c.gridwidth = 1; c.weightx = 0; c.anchor = GridBagConstraints.WEST;
            add(new JLabel("Color:"), c);

            // Color Button
            c.gridx = 1; c.gridy = 3; c.weightx = 0;
            colorEditButton = new ColorJButton("", Color.black);
            colorEditButton.setBackground(entry.getColor());
            colorEditButton.setPreferredSize(new Dimension(25, 25));
            colorEditButton.setMinimumSize(new Dimension(25, 25));
            colorEditButton.setToolTipText("Click to change the color.");
            colorEditButton.addActionListener(e ->
            {
                Color newColor = JColorChooser.showDialog(AnimConfigEntryPanel.this, "Choose Color", colorEditButton.getBackground());
                if (newColor != null && !newColor.equals(entry.getColor()))
                {
                    entry.setColor(newColor);
                    entry.setOpacity(newColor.getAlpha());
                    colorEditButton.setBackground(newColor);
                    updateHexColorLabel(newColor);
                    saveChanges();
                }
            });
            add(colorEditButton, c);

            // Hex Color Label
            c.gridx = 2; c.gridy = 3; c.weightx = 1.0;
            hexColorLabel = new JLabel();
            updateHexColorLabel(entry.getColor());
            add(hexColorLabel, c);
        }

        private void updateHexColorLabel(Color color)
        {
            hexColorLabel.setText(String.format("#%06X", (color.getRGB() & 0xFFFFFF)));
        }

        private void saveChanges()
        {
            plugin.saveCurrentAnimationColorConfigs();
            plugin.recheckCurrentPlayerAnimation();
        }
    }

    // New class for InteractionConfigEntryPanel
    private class InteractionConfigEntryPanel extends JPanel
    {
        private final ActionAvailabilityMain plugin;
        private final InteractionConfigEntry entry;
        private final JTextField nameEditField;
        private final JTextField targetIdEditField;
        private final ColorJButton colorEditButton;
        private final JTextField tickDurationEditField;
        private final JLabel hexColorLabel;
        private final Consumer<InteractionConfigEntry> removeCallback;

        public InteractionConfigEntryPanel(ActionAvailabilityMain plugin, InteractionConfigEntry entry, Consumer<InteractionConfigEntry> removeCallback)
        {
            this.plugin = plugin;
            this.entry = entry;
            this.removeCallback = removeCallback;

            setLayout(new GridBagLayout());
            setBorder(BorderFactory.createLineBorder(Color.GRAY));
            setPreferredSize(new Dimension(250, 150)); // Adjusted size for removed menu option
            setMinimumSize(new Dimension(250, 150));

            GridBagConstraints c = new GridBagConstraints();
            c.fill = GridBagConstraints.HORIZONTAL;
            c.insets = new Insets(5, 5, 5, 5);

            // Name Label
            c.gridx = 0; c.gridy = 0; c.weightx = 0; c.anchor = GridBagConstraints.WEST;
            add(new JLabel("Name:"), c);

            // Name Field
            c.gridx = 1; c.gridy = 0; c.weightx = 1.0;
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
            c.gridx = 2; c.gridy = 0; c.weightx = 0; c.fill = GridBagConstraints.NONE; c.anchor = GridBagConstraints.NORTHEAST;
            JButton removeButton = new JButton("X");
            removeButton.setMargin(new Insets(1, 4, 1, 4));
            removeButton.setToolTipText("Remove this configuration.");
            removeButton.addActionListener(e -> removeCallback.accept(entry));
            add(removeButton, c);

            // Target ID Label
            c.gridx = 0; c.gridy = 1; c.weightx = 0; c.fill = GridBagConstraints.HORIZONTAL; c.anchor = GridBagConstraints.WEST;
            add(new JLabel("Target ID:"), c);

            // Target ID Field
            c.gridx = 1; c.gridy = 1; c.gridwidth = 2; c.weightx = 1.0;
            targetIdEditField = new JTextField(8);
            targetIdEditField.setText(String.valueOf(entry.getTargetId()));
            targetIdEditField.setToolTipText("Edit the target ID. Use -1 for any ID.");
            targetIdEditField.addFocusListener(new FocusAdapter()
            {
                @Override
                public void focusLost(FocusEvent e)
                {
                    try
                    {
                        int newId = Integer.parseInt(targetIdEditField.getText());
                        if (newId != entry.getTargetId())
                        {
                            entry.setTargetId(newId);
                            saveChanges();
                        }
                    }
                    catch (NumberFormatException ex)
                    {
                        JOptionPane.showMessageDialog(InteractionConfigEntryPanel.this, "Invalid Target ID.", "Input Error", JOptionPane.ERROR_MESSAGE);
                        targetIdEditField.setText(String.valueOf(entry.getTargetId()));
                    }
                }
            });
            add(targetIdEditField, c);

            // Tick Duration Label
            c.gridx = 0; c.gridy = 2; c.gridwidth = 1; c.weightx = 0; c.fill = GridBagConstraints.HORIZONTAL; c.anchor = GridBagConstraints.WEST;
            add(new JLabel("Ticks:"), c);

            // Tick Duration Field
            c.gridx = 1; c.gridy = 2; c.gridwidth = 2; c.weightx = 1.0;
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
                        if (newDuration != entry.getTickDuration())
                        {
                            entry.setTickDuration(newDuration);
                            saveChanges();
                        }
                    }
                    catch (NumberFormatException ex)
                    {
                        JOptionPane.showMessageDialog(InteractionConfigEntryPanel.this, "Invalid Tick Duration.", "Input Error", JOptionPane.ERROR_MESSAGE);
                        tickDurationEditField.setText(String.valueOf(entry.getTickDuration()));
                    }
                }
            });
            add(tickDurationEditField, c);

            // Color Label
            c.gridx = 0; c.gridy = 3; c.gridwidth = 1; c.weightx = 0; c.anchor = GridBagConstraints.WEST;
            add(new JLabel("Color:"), c);

            // Color Button
            c.gridx = 1; c.gridy = 3; c.weightx = 0;
            colorEditButton = new ColorJButton("", Color.black);
            colorEditButton.setBackground(entry.getColor());
            colorEditButton.setPreferredSize(new Dimension(25, 25));
            colorEditButton.setMinimumSize(new Dimension(25, 25));
            colorEditButton.setToolTipText("Click to change the color.");
            colorEditButton.addActionListener(e ->
            {
                Color newColor = JColorChooser.showDialog(InteractionConfigEntryPanel.this, "Choose Color", colorEditButton.getBackground());
                if (newColor != null && !newColor.equals(entry.getColor()))
                {
                    entry.setColor(newColor);
                    entry.setOpacity(newColor.getAlpha());
                    colorEditButton.setBackground(newColor);
                    updateHexColorLabel(newColor);
                    saveChanges();
                }
            });
            add(colorEditButton, c);

            // Hex Color Label
            c.gridx = 2; c.gridy = 3; c.weightx = 1.0;
            hexColorLabel = new JLabel();
            updateHexColorLabel(entry.getColor());
            add(hexColorLabel, c);
        }

        private void updateHexColorLabel(Color color)
        {
            hexColorLabel.setText(String.format("#%06X", (color.getRGB() & 0xFFFFFF)));
        }

        private void saveChanges()
        {
            plugin.saveCurrentInteractionTriggerConfigs();
            // No direct recheck for interactions needed here, as they are event-driven
        }
    }
}

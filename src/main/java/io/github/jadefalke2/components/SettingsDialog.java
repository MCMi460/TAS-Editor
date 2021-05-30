package io.github.jadefalke2.components;

import io.github.jadefalke2.util.Settings;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.util.function.Consumer;
import java.util.function.Function;

public class SettingsDialog extends JDialog {

	public SettingsDialog(Window owner, Settings prefs){
		super(owner, "Settings", ModalityType.APPLICATION_MODAL);

		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		JPanel mainPanel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weighty = 1;
		c.weightx = 1;
		c.gridx = 0;
		c.gridy = 0;

		addCheckboxSetting("Dark Theme", prefs.isDarkTheme(), prefs::setDarkTheme, mainPanel, c);
		c.gridy += 1;

		addSpinnerSetting("Show last stick positions", prefs.getLastStickPositionCount(), prefs::setLastStickPositionCount, mainPanel, c);
		c.gridy += 1;

		addRadioButtonSetting("IntTest", 2, e -> {}, new Integer[]{1,2,3}, new String[]{"One", "Two", "Three"}, Integer::parseInt, mainPanel, c);

		add(mainPanel);
		setLocationRelativeTo(null);
		pack();
	}

	private void addCheckboxSetting(String name, boolean defaultState, Consumer<Boolean> setter, JPanel mainPanel, GridBagConstraints c){
		mainPanel.add(new JLabel(name), c);
		c.gridx = 1;
		JCheckBox box = new JCheckBox();
		box.setSelected(defaultState);
		box.addItemListener((event) -> setter.accept(event.getStateChange() == ItemEvent.SELECTED));
		mainPanel.add(box, c);
		c.gridx = 0;
	}

	private void addSpinnerSetting(String name, int defaultState, Consumer<Integer> setter, JPanel mainPanel, GridBagConstraints c){
		mainPanel.add(new JLabel(name), c);
		c.gridx = 1;
		JSpinner spinner = new JSpinner();
		SpinnerNumberModel model = new SpinnerNumberModel();
		model.setMinimum(0);
		spinner.setModel(model);
		spinner.setValue(defaultState);
		spinner.addChangeListener((event) -> setter.accept((Integer)spinner.getValue()));
		mainPanel.add(spinner, c);
		c.gridx = 0;
	}

	private <T> void addRadioButtonSetting(String name, T defaultState, Consumer<T> setter, T[] values, String[] descriptions, Function<String, T> creator, JPanel mainPanel, GridBagConstraints c){
		mainPanel.add(new JLabel(name), c);
		c.gridx = 1;
		JPanel buttonPanel = new JPanel();
		ButtonGroup group = new ButtonGroup();
		for(int i=0;i<values.length;i++){
			JRadioButton button = new JRadioButton(descriptions[i]);
			button.setActionCommand(values[i].toString());
			button.addActionListener(e -> setter.accept(creator.apply(((JRadioButton)e.getSource()).getActionCommand())));

			group.add(button);
			buttonPanel.add(button);
			if(values[i] == defaultState)
				button.setSelected(true);
		}
		mainPanel.add(buttonPanel, c);
		c.gridx = 0;
	}

}

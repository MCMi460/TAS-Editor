package io.github.jadefalke2.stickRelatedClasses;

import io.github.jadefalke2.util.Settings;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.util.stream.IntStream;

public class SmoothTransitionDialog extends JDialog {

	public static final String[] dropdownOptions = new String[]{
		"Angular (Closest)",
		"Linear",
		"Angular (Clockwise)",
		"Angular (Counter-Clockwise)"
	};

	private final int frames;
	private final JComboBox<String> dropdownMenu;
	private final JoystickPanel startJoystick,  endJoystick;

	public SmoothTransitionDialog(Settings settings, StickPosition startPos, StickPosition endPos, int frames){
		super();

		this.frames = frames;
		//option
		dropdownMenu = new JComboBox<>();
		if(dropdownOptions.length != 4) throw new UnsupportedOperationException("Too many items in options list...");
		for(String option : dropdownOptions)
			dropdownMenu.addItem(option);

		dropdownMenu.setSelectedIndex(settings.getSmoothTransitionType().ordinal());

		JButton okButton = new JButton("OK");
		okButton.addActionListener(e -> dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING)));

		startJoystick = new JoystickPanel(settings, "Start");
		startJoystick.setStickPosition(startPos);
		endJoystick = new JoystickPanel(settings, "End");
		endJoystick.setStickPosition(endPos);

		CustomChangeListener<StickPosition> joystickPanelListener = e -> {
			if(frames > 1){
				StickPosition[] stickPositionsPreview = getSmoothTransitionData();
				startJoystick.setStickPositions(reverse(stickPositionsPreview));
				endJoystick.setStickPositions(stickPositionsPreview);
			}
		};
		startJoystick.setOnChangeListener(joystickPanelListener);
		endJoystick.setOnChangeListener(joystickPanelListener);

		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		panel.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
		GridBagConstraints c = new GridBagConstraints();


		c.weightx = 1;
		c.weighty = 0;

		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridwidth = 2;

		panel.add(dropdownMenu, c);

		c.fill = GridBagConstraints.BOTH;
		c.gridwidth = 1;
		c.weighty = 1;

		c.gridx = 0;
		c.gridy = 1;
		panel.add(startJoystick, c);

		c.gridx = 1;
		panel.add(endJoystick, c);

		c.gridwidth = 2;
		c.weighty = 0;
		c.gridx = 0;
		c.gridy = 2;
		panel.add(okButton, c);


		add(panel);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setLocationRelativeTo(null);
		setModal(true);
		setSize(500,500); //TODO don't do this
	}

	private StickPosition[] reverse (StickPosition[] old) {
		StickPosition[] newArray = new StickPosition[old.length];
		IntStream.range(0, old.length).forEach(i -> newArray[i] = old[old.length - i - 1]);
		return newArray;
	}

	public StickPosition[] getSmoothTransitionData () {
		StickPosition firstPos = startJoystick.getStickPosition();
		StickPosition endPos = endJoystick.getStickPosition();

		int index = dropdownMenu.getSelectedIndex();
		if(index == 0)
			return transitionAngularClosest(firstPos, endPos, frames);
		else if(index == 1)
			return transitionLinearClosest(firstPos, endPos, frames);
		else if(index == 2)
			return transitionAngularClockwise(firstPos, endPos, frames);
		else if(index == 3)
			return transitionAngularCounterClockwise(firstPos, endPos, frames);
		else
			throw new UnsupportedOperationException("Selected unknown item in interpolation type dropdown: "+dropdownMenu.getSelectedItem()+" ("+dropdownMenu.getSelectedIndex()+")");
	}

	public static StickPosition[] transitionAngularClosest(StickPosition firstPos, StickPosition endPos, int frames){
		double diffThetaCounterClockwise = endPos.getTheta() - firstPos.getTheta();
		double diffThetaClockwise = diffThetaCounterClockwise;
		if(diffThetaCounterClockwise < 0) diffThetaCounterClockwise += 2*Math.PI;
		if(diffThetaClockwise > 0) diffThetaClockwise -= 2*Math.PI;

		if(Math.abs(diffThetaClockwise) < Math.abs(diffThetaCounterClockwise))
			return transitionAngularClockwise(firstPos, endPos, frames);
		else
			return transitionAngularCounterClockwise(firstPos, endPos, frames);
	}

	public static StickPosition[] transitionAngularCounterClockwise(StickPosition firstPos, StickPosition endPos, int frames){
		StickPosition[] result = new StickPosition[frames];
		double firstTheta = firstPos.getTheta();
		double firstRadius = firstPos.getRadius();
		double diffTheta = endPos.getTheta() - firstTheta;
		double diffRadius = endPos.getRadius() - firstRadius;

		if(diffTheta < 0) diffTheta += 2*Math.PI;

		return calculatePositions(endPos, frames, result, firstTheta, firstRadius, diffTheta, diffRadius);
	}

	public static StickPosition[] transitionAngularClockwise(StickPosition firstPos, StickPosition endPos, int frames){
		StickPosition[] result = new StickPosition[frames];
		double firstTheta = firstPos.getTheta();
		double firstRadius = firstPos.getRadius();
		double diffTheta = endPos.getTheta() - firstTheta;
		double diffRadius = endPos.getRadius() - firstRadius;

		if(diffTheta > 0) diffTheta -= 2*Math.PI;

		return calculatePositions(endPos, frames, result, firstTheta, firstRadius, diffTheta, diffRadius);
	}

	private static StickPosition[] calculatePositions(StickPosition endPos, int frames, StickPosition[] result, double firstTheta, double firstRadius, double diffTheta, double diffRadius) {
		for(int i=0;i<frames-1;i++){
			result[i] = new StickPosition(firstTheta+((i/((double)frames-1))*diffTheta), firstRadius+((i/((double)frames-1))*diffRadius));
		}
		result[frames-1] = endPos;

		return result;
	}

	public static StickPosition[] transitionLinearClosest(StickPosition firstPos, StickPosition endPos, int frames){
		StickPosition[] result = new StickPosition[frames];
		int firstX = firstPos.getX();
		int firstY = firstPos.getY();
		int diffX = endPos.getX() - firstX;
		int diffY = endPos.getY() - firstY;

		for(int i=0;i<frames-1;i++){
			result[i] = new StickPosition((int)(firstX+((i/((double)frames-1))*diffX)), (int)(firstY+((i/((double)frames-1))*diffY)));
		}
		result[frames-1] = endPos;

		return result;
	}
}

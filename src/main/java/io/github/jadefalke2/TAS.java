package io.github.jadefalke2;

import com.bulenkov.darcula.DarculaLaf;
import com.sun.java.swing.plaf.windows.WindowsLookAndFeel;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;

public class TAS {

	private static TAS instance;

	private Window window;
	private JPanel startUpPanel;
	private JPanel editor;

	private Script script;
	private File currentFile;

	private boolean functionWindowIsOpen;


	private final LookAndFeel original = UIManager.getLookAndFeel();

	private Stack<Action> undoStack;
	private Stack<Action> redoStack;

	private TAS() {
		instance = this;
		startProgram();
	}

	public static TAS getInstance() {
		return instance;
	}

	/**
	 * starts the program by opening a new window with the two options of either creating a new script or loading in a preexisting one. After this it will start the editor.
	 */

	public void startProgram() {
		startUpPanel = new JPanel();

		window = new Window();
		window.setBackground(new Color(52, 52, 52));
		window.setSize(300, 200);
		window.add(startUpPanel);

		JButton createNewScriptButton = new JButton("create new script");
		JButton loadScriptButton = new JButton("load script");


		createNewScriptButton.addActionListener(e -> {
			onNewScriptButtonPress();
		});

		loadScriptButton.addActionListener(e -> {
			onLoadButtonPress();
		});


		startUpPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

		startUpPanel.add(createNewScriptButton);
		startUpPanel.add(loadScriptButton);

	}

	public void onLoadButtonPress() {
		openLoadFileChooser();
	}

	public void onNewScriptButtonPress() {
		openNewFileCreator();
	}

	public void openLoadFileChooser() {

		setWindowsLookAndFeel();

		JFileChooser fileChooser = new JFileChooser(FileSystemView.getFileSystemView());

		fileChooser.setDialogTitle("Choose existing TAS file");
		fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
		FileNameExtensionFilter filter = new FileNameExtensionFilter(".txt files", "txt", "text");
		fileChooser.setFileFilter(filter);

		int option = fileChooser.showOpenDialog(null);

		if (option == JFileChooser.APPROVE_OPTION) {

			File fileToOpen = fileChooser.getSelectedFile();
			prepareEditor(fileToOpen);
		}

		try {
			UIManager.setLookAndFeel(original);
		} catch (UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
	}

	private void prepareEditor(File fileToOpen) {
		currentFile = fileToOpen;

		StringBuilder stringBuilder = new StringBuilder();
		try (BufferedReader br = new BufferedReader(new FileReader(fileToOpen))) {

			String sCurrentLine;
			while ((sCurrentLine = br.readLine()) != null) {
				stringBuilder.append(sCurrentLine).append("\n");
			}
		} catch (IOException exception) {
			exception.printStackTrace();
		}

		String string = stringBuilder.toString();
		script = new Script(string);

		try {
			UIManager.setLookAndFeel(original);
		} catch (UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}

		startEditor();
	}

	public void openNewFileCreator() {

		setWindowsLookAndFeel();

		JFileChooser fileChooser = new JFileChooser(FileSystemView.getFileSystemView());

		fileChooser.setDialogTitle("Choose where you want your TAS file to go");
		fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
		FileNameExtensionFilter filter = new FileNameExtensionFilter(".txt files", "txt", "text");
		fileChooser.setFileFilter(filter);
		fileChooser.setSelectedFile(new File("script1.txt"));

		int option = fileChooser.showSaveDialog(null);

		if (option == JFileChooser.APPROVE_OPTION) {

			File fileToOpen = fileChooser.getSelectedFile();
			String fileName = fileChooser.getSelectedFile().getPath();
			File file = new File(fileName);
			try {
				file.createNewFile();
				FileWriter fileWriter = new FileWriter(fileName);
				// optimize the below later
				fileWriter.write("1 NONE 0;0 0;0\n");
				fileWriter.write("2 NONE 0;0 0;0\n");
				fileWriter.write("3 NONE 0;0 0;0\n");
				fileWriter.write("4 NONE 0;0 0;0\n");
				fileWriter.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}

			prepareEditor(fileToOpen);
		}

	}

	public void setWindowsLookAndFeel() {
		System.out.println("Windows Look and Feel!");
		try {
			UIManager.setLookAndFeel(new WindowsLookAndFeel());
			SwingUtilities.updateComponentTreeUI(window);
		} catch (UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
	}

	public void setDarculaLookAndFeel() {
		System.out.println("Darcula Look and Feel!");
		try {
			UIManager.setLookAndFeel(new DarculaLaf());
			SwingUtilities.updateComponentTreeUI(window);
		} catch (UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}


	}

	public void startEditor() {

		window.remove(startUpPanel);

		editor = new JPanel();
		window.add(editor);

		PianoRoll pianoRoll = new PianoRoll(script);
		JScrollPane scrollPane = new JScrollPane(pianoRoll);

		MainMenuBar menuBar = new MainMenuBar(this);
		window.setMenuBar(menuBar);


		undoStack = new CircularStack<>(1024);
		redoStack = new CircularStack<>(1024);

		editor.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));


		window.setSize(700, 1100);
		editor.setSize(550, 550);


		editor.add(scrollPane);

		JPanel buttonsPanel = new JPanel();
		buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.Y_AXIS));

		JButton functionEditorButton = new JButton("Function editor");
		functionEditorButton.addActionListener(e -> openFunctionEditor());
		buttonsPanel.add(functionEditorButton);

		editor.add(buttonsPanel);
	}

	private void openFunctionEditor() {
		JFrame functionEditor;

		if (!functionWindowIsOpen) {

			functionWindowIsOpen = true;

			functionEditor = new JFrame();
			functionEditor.setVisible(true);
			functionEditor.setSize(500,400);
			functionEditor.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

			functionEditor.addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosing(WindowEvent e) {
					functionWindowIsOpen = false;
					e.getWindow().dispose();
				}
			});

			JPanel functionWindowTypeChooser = new JPanel();
			functionEditor.add(functionWindowTypeChooser);

			JButton createNewFunctionButton = new JButton("Create new function");
			createNewFunctionButton.addActionListener(e -> {
				createNewFunction();
			});

			JButton editFunctionButton = new JButton("Edit existing function");
			editFunctionButton.addActionListener(e -> {
				editFunction();
			});

			functionWindowTypeChooser.add(editFunctionButton);
			functionWindowTypeChooser.add(createNewFunctionButton);
		}

	}

	private void createNewFunction() {
		openLoadFileChooser();
	}

	private void editFunction() {
		//edits a function
	}

	public void saveFile() {


		BufferedWriter writer = null;
		try {

			StringBuilder wholeScript = new StringBuilder();

			for (InputLine currentLine : script.getInputLines()) {
				wholeScript.append(currentLine.getFull() + "\n");
			}

			FileWriter fw;

			fw = new FileWriter(currentFile);


			writer = new BufferedWriter(fw);


			writer.write(wholeScript.toString());

		} catch (IOException ioe) {
			ioe.printStackTrace();
		} finally {
			try {
				if (writer != null)
					writer.close();
			} catch (Exception ex) {
				System.out.println("Error in closing the BufferedWriter" + ex);
			}
		}

	}

	public void executeAction(Action action) {
		action.execute();
		undoStack.push(action);
		redoStack.clear();
	}

	public void undo() {
		if (undoStack.isEmpty())
			return;
		Action action = undoStack.pop();
		action.revert();
		redoStack.push(action);
	}

	public void redo() {
		if (redoStack.isEmpty())
			return;
		Action action = redoStack.pop();
		action.execute();
		undoStack.push(action);
	}

	public static void main(String[] args) {
		new TAS();
	}
}

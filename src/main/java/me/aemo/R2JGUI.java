package me.aemo;

import me.aemo.interfaces.ConvertListener;
import me.aemo.interfaces.ReadFileListener;
import me.aemo.interfaces.WriteFileListener;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.*;
import java.io.File;

public class R2JGUI {

    private JFrame frame;
    private JTextField inputFileField;
    private JTextField packageNameField;
    private JButton inputFileButton;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new R2JGUI().createAndShowGUI());
    }

    public void createAndShowGUI() {
        // Setup JFrame
        frame = new JFrame("R2J Converter");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 200);
        frame.setLayout(new GridBagLayout());

        // Setup constraints
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Setup views
        JLabel inputFileLabel = new JLabel("Input File:");
        inputFileField = new JTextField();
        inputFileButton = new JButton("Browse");
        JLabel packageNameLabel = new JLabel("Package Name:");
        packageNameField = new JTextField();
        JButton convertButton = new JButton("Convert");

        // Set font size for the labels
        inputFileLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        packageNameLabel.setFont(new Font("Arial", Font.PLAIN, 16));

        // Set preferred size and minimum size for the convertButton
        convertButton.setPreferredSize(new Dimension(80, 30));
        convertButton.setMinimumSize(new Dimension(80, 30));
        convertButton.setFont(new Font("Arial", Font.PLAIN, 12));

        // Setting constraints for the input file label
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 0.0;
        frame.add(inputFileLabel, gbc);

        // Setting constraints for the input file text field
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        frame.add(inputFileField, gbc);

        // Setting constraints for the browse button
        gbc.gridx = 3;
        gbc.gridwidth = 1;
        gbc.weightx = 0.0;
        frame.add(inputFileButton, gbc);

        // Setting constraints for the package name label
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.weightx = 0.0;
        frame.add(packageNameLabel, gbc);

        // Setting constraints for the package name text field
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        frame.add(packageNameField, gbc);

        // Setting constraints for the convert button
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        frame.add(convertButton, gbc);

        // Action listeners
        inputFileButton.addActionListener(e -> selectFile(inputFileField));
        convertButton.addActionListener(e -> performConversion());

        // Set up drag and drop
        setupDragAndDrop(inputFileField);
        setupDragAndDropForButton(inputFileButton);

        // Explicitly set preferred size and location
        frame.setPreferredSize(new Dimension(500, 200));
        frame.pack();
        frame.setLocationRelativeTo(null); // Center the frame on the screen
        frame.setVisible(true);
    }

    private void selectFile(JTextField textField) {
        JFileChooser fileChooser = new JFileChooser();
        int returnValue = fileChooser.showOpenDialog(frame);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            textField.setText(fileChooser.getSelectedFile().getPath());
        }
    }

    private void setupDragAndDrop(JTextField textField) {
        textField.setDropTarget(new DropTarget() {
            @Override
            public synchronized void drop(DropTargetDropEvent evt) {
                try {
                    evt.acceptDrop(DnDConstants.ACTION_COPY);
                    java.util.List<File> droppedFiles = (java.util.List<File>) evt.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                    if (!droppedFiles.isEmpty()) {
                        textField.setText(droppedFiles.get(0).getAbsolutePath());
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    private void setupDragAndDropForButton(JButton button) {
        button.setDropTarget(new DropTarget() {
            @Override
            public synchronized void drop(DropTargetDropEvent evt) {
                try {
                    evt.acceptDrop(DnDConstants.ACTION_COPY);
                    java.util.List<File> droppedFiles = (java.util.List<File>) evt.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                    if (!droppedFiles.isEmpty()) {
                        inputFileField.setText(droppedFiles.get(0).getAbsolutePath());
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    private void performConversion() {
        String inputFile = inputFileField.getText();
        String packageName = packageNameField.getText().trim(); // Get package name and trim any extra whitespace

        if (inputFile.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Please select an input file.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        File input = new File(inputFile);
        String outputFile = input.getParent() + File.separator + "R.java";

        R2J.readTextFile(inputFile, new ReadFileListener() {
            @Override
            public void onSuccess(String content) {
                R2J.convert(content, packageName, new ConvertListener() {
                    @Override
                    public void onSuccess(String content) {
                        R2J.writeJavaFile(outputFile, content, new WriteFileListener() {
                            @Override
                            public void onSuccess() {
                                JOptionPane.showMessageDialog(frame, "Conversion completed successfully.\n" +
                                        "R.java has been created at: " + outputFile);
                            }

                            @Override
                            public void onError(String error) {
                                errorMessageDialog("Write File Error", error);
                            }
                        });
                    }

                    @Override
                    public void onError(String error) {
                        errorMessageDialog("Convert To Java Error", error);
                    }
                });
            }

            @Override
            public void onError(String error, String errorFrom) {
                errorMessageDialog("Read File Error ~ " + errorFrom, error);
            }
        });
    }

    private void errorMessageDialog(String title, String message){
        JOptionPane.showMessageDialog(frame, message, title, JOptionPane.ERROR_MESSAGE);
    }
}

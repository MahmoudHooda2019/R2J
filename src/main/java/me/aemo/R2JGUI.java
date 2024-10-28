package me.aemo;

import com.formdev.flatlaf.FlatLightLaf;
import jnafilechooser.api.JnaFileChooser;
import me.aemo.interfaces.ConvertListener;
import me.aemo.interfaces.ReadFileListener;
import me.aemo.interfaces.WriteFileListener;
import me.aemo.ui.MyButton;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.*;
import java.io.File;
import java.net.URL;
import java.util.concurrent.CompletableFuture;

public class R2JGUI {

    private JFrame frame;
    private JTextField inputFileField;
    private JTextField packageNameField;
    private MyButton inputFileButton;
    private MyButton convertButton;

    public static void main(String[] args) throws UnsupportedLookAndFeelException {
        UIManager.setLookAndFeel(new FlatLightLaf());
        SwingUtilities.invokeLater(R2JGUI::new);
    }

    public R2JGUI() {
        createAndShowGUI();
    }

    public void createAndShowGUI() {
        frame = new JFrame("R2J Converter");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 200);
        frame.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        JLabel fileLabel = new JLabel("Select a R.txt file:");
        inputFileField = new JTextField();
        inputFileField.setPreferredSize(new Dimension(250, 25));
        inputFileButton = new MyButton("Browse");
        inputFileButton.setRadius(25);
        inputFileButton.setBorderColor(new Color(0xB0BEC5));
        inputFileButton.setColorClick(new Color(0x0056B3));
        inputFileButton.setColorOver(new Color(0x76C7FF));
        inputFileButton.addActionListener(e -> openFileChooser());

        JLabel packageLabel = new JLabel("Package Name:");
        packageNameField = new JTextField();
        packageNameField.setPreferredSize(new Dimension(250, 25));

        convertButton = new MyButton("Convert");
        convertButton.setRadius(25);
        convertButton.setBorderColor(new Color(0xB0BEC5));
        convertButton.setColorClick(new Color(0x0056B3));
        convertButton.setColorOver(new Color(0x76C7FF));
        convertButton.addActionListener(e -> convert());

        gbc.gridx = 0;
        gbc.gridy = 0;
        frame.add(fileLabel, gbc);

        gbc.gridx = 1;
        frame.add(inputFileField, gbc);

        gbc.gridx = 2;
        frame.add(inputFileButton, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        frame.add(packageLabel, gbc);

        gbc.gridx = 1;
        frame.add(packageNameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 3;
        gbc.anchor = GridBagConstraints.CENTER;
        frame.add(convertButton, gbc);

        loadIcon();
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        // Enable drag-and-drop for the entire frame
        enableDragAndDrop();
        // Enable drag-and-drop for the input file field
        enableDragAndDropForInputField();
    }

    private void enableDragAndDrop() {
        new DropTarget(frame, new DropTargetListener() {
            @Override
            public void dragEnter(DropTargetDragEvent dtde) {}

            @Override
            public void dragOver(DropTargetDragEvent dtde) {}

            @Override
            public void dropActionChanged(DropTargetDragEvent dtde) {}

            @Override
            public void dragExit(DropTargetEvent dte) {}

            @Override
            public void drop(DropTargetDropEvent dtde) {
                handleDrop(dtde);
            }
        });
    }

    private void enableDragAndDropForInputField() {
        new DropTarget(inputFileField, new DropTargetListener() {
            @Override
            public void dragEnter(DropTargetDragEvent dtde) {}

            @Override
            public void dragOver(DropTargetDragEvent dtde) {}

            @Override
            public void dropActionChanged(DropTargetDragEvent dtde) {}

            @Override
            public void dragExit(DropTargetEvent dte) {}

            @Override
            public void drop(DropTargetDropEvent dtde) {
                handleDrop(dtde);
            }
        });
    }

    private void handleDrop(DropTargetDropEvent dtde) {
        try {
            dtde.acceptDrop(DnDConstants.ACTION_COPY);
            java.util.List<File> droppedFiles = (java.util.List<File>) dtde.getTransferable()
                    .getTransferData(DataFlavor.javaFileListFlavor);

            for (File file : droppedFiles) {
                if (file.getName().endsWith(".txt")) {
                    inputFileField.setText(file.getAbsolutePath());
                } else {
                    showError("Please drop a valid .txt file.");
                }
            }
            dtde.dropComplete(true);
        } catch (Exception e) {
            e.printStackTrace();
            dtde.dropComplete(false);
        }
    }

    private void convert() {
        String inputFile = inputFileField.getText();
        String packageName = packageNameField.getText().trim();

        if (inputFile.isEmpty()) {
            showError("Please select an input .txt file.");
            return;
        }

        File input = new File(inputFile);
        String outputFile = input.getParent() + File.separator + "R.java";

        // Create progress dialog
        JDialog progressDialog = new JDialog(frame, "Converting...", true);
        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setString("Processing...");
        progressBar.setStringPainted(true);
        progressDialog.getContentPane().add(progressBar);
        progressDialog.setSize(300, 100);
        progressDialog.setLocationRelativeTo(frame);
        progressDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

        // Create and execute SwingWorker
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            private String error = null;
            private String errorTitle = null;

            @Override
            protected Void doInBackground() {
                try {
                    // Step 1: Read file
                    CompletableFuture<String> readFuture = new CompletableFuture<>();
                    R2J.readTextFile(inputFile, new ReadFileListener() {
                        @Override
                        public void onSuccess(String content) {
                            readFuture.complete(content);
                        }

                        @Override
                        public void onError(String err, String errorFrom) {
                            readFuture.completeExceptionally(
                                    new Exception(errorFrom + ": " + err));
                        }
                    });

                    String content = readFuture.get();

                    // Step 2: Convert
                    CompletableFuture<String> convertFuture = new CompletableFuture<>();
                    R2J.convert(content, packageName, new ConvertListener() {
                        @Override
                        public void onSuccess(String converted) {
                            convertFuture.complete(converted);
                        }

                        @Override
                        public void onError(String err) {
                            convertFuture.completeExceptionally(
                                    new Exception("Convert Error: " + err));
                        }
                    });

                    String convertedContent = convertFuture.get();

                    // Step 3: Write file
                    CompletableFuture<Void> writeFuture = new CompletableFuture<>();
                    R2J.writeJavaFile(outputFile, convertedContent, new WriteFileListener() {
                        @Override
                        public void onSuccess() {
                            writeFuture.complete(null);
                        }

                        @Override
                        public void onError(String err) {
                            writeFuture.completeExceptionally(
                                    new Exception("Write Error: " + err));
                        }
                    });

                    writeFuture.get();

                } catch (Exception e) {
                    error = e.getMessage();
                    errorTitle = "Conversion Error";
                }
                return null;
            }

            @Override
            protected void done() {
                progressDialog.dispose();
                if (error != null) {
                    showError(error, errorTitle);
                } else {
                    JOptionPane.showMessageDialog(frame,
                            "Conversion completed successfully.\n" +
                                    "R.java has been created at: " + outputFile);
                }
            }
        };

        // Start the worker
        worker.execute();

        // Show dialog after starting the worker
        progressDialog.setVisible(true);
    }
    private void openFileChooser() {
        JnaFileChooser fileChooser = new JnaFileChooser();
        fileChooser.addFilter("","*.txt");

        if (fileChooser.showOpenDialog(frame)) {
            File selectedFile = fileChooser.getSelectedFile();
            if (selectedFile != null) {
                inputFileField.setText(selectedFile.getAbsolutePath());
            } else {
                showError("File selection was canceled.");
            }
        } else {
            showSampleDialog("not file chooser.");
        }
    }

    private void loadIcon() {
        URL iconUrl = R2JGUI.class.getClassLoader().getResource("icon.png");
        if (iconUrl != null) {
            ImageIcon icon = new ImageIcon(iconUrl);
            frame.setIconImage(icon.getImage());
        }
    }

    private void showSampleDialog(String message){
        JOptionPane.showMessageDialog(frame, message);
    }
    private void showError(String message) {
        JOptionPane.showMessageDialog(frame, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
    private void showError(String message, String title) {
        JOptionPane.showMessageDialog(frame, message, title, JOptionPane.ERROR_MESSAGE);
    }
}
import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class NeuralNetGUI extends JFrame {
    // Multithreading
    private final int NUM_CORES; // May be set to number of CPU Threads in case hyperthreading is enabled on the CPU
    // Neural net object
    NeuralesNetz net;
    // Training data
    ArrayList<Dataset> trainingData;
    private JPanel rootPanel;
    private JTextField textFilename;
    private JLabel labelFilename;
    private JLabel labelstructHiddenLayers;
    private JTextField textStructHiddenLayers;
    private JLabel labelBias;
    private JTextField textBias;
    private JTextField textLearningRate;
    private JLabel labelLearningRate;
    private JTextField textNumEpochs;
    private JCheckBox checkBoxNumEpochsInf;
    private JPanel panelSettings;
    private JLabel labelNumEpochs;
    private JCheckBox checkBoxStructHiddenLayers;
    private JButton buttonStart;
    private JButton buttonPause;
    private JLabel labelWeightInitMethod;
    private JComboBox comboBoxWeightInitMethod;
    private JButton buttonOpen;
    private JPanel panelErrorPlot;
    private JTextField textChartUpdateInterval;
    private JCheckBox checkChartUpdateInterval;
    private JCheckBox checkAllowReiterationOfSample;
    private JPanel panelDataPlot;
    private JPanel panelButton;
    private JButton buttonReset;
    private JButton buttonExit;
    private JPanel panelSpeed;
    private JSlider sliderThrottle;
    private JCheckBox checkThrottle;
    private JLabel labelNumThreads;
    private JSpinner spinnerNumThreads;
    private JPanel panelClassify;
    private JPanel panelOutput;
    private JLabel labelClassifyUserInput;
    private JLabel labelClassifyW1;
    private JLabel labelClassifyW2;
    private JTextField textClassifyW1;
    private JTextField textClassifyW2;
    private JButton buttonClassify;
    private JTextArea textAreaOutput;
    private JScrollPane scrollPaneOutput;
    private JPanel panel;
    // Net settings
    private File dataFile;
    private int[] structHiddenLayers;
    private double bias;
    private double learningRate;
    private long maxEpochs; // -1 = infinite // TODO biginteger
    private WeigthInitializationMethod weigthInitializationMethod;
    private boolean allowReiteration;
    // Charts
    private XYChart dataDistributionChart;
    private XYChart errorRateChart;
    // Chart/Application settings
    private boolean throttled;
    private boolean isPaused;
    private int numThreadsToUse;
    private ArrayList<Thread> threadList = new ArrayList<>();

    public NeuralNetGUI() {
        // Init some default values
        throttled = true;
        allowReiteration = true;
        NUM_CORES = Runtime.getRuntime().availableProcessors();

        JFrame frame = new JFrame("NeuralNetGUI");
        frame.setContentPane(rootPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Make Text Area automatically scroll down
        DefaultCaret caret = (DefaultCaret) textAreaOutput.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        // Make Text Area show stdout
        PrintStream printStream = new PrintStream(System.out) {
            @Override
            public void print(String text) {
                textAreaOutput.append(text + "\n");
            }
        };
        System.setOut(printStream);

        // Init Thread spinner
        SpinnerModel spinnerNumThreadsModel = new SpinnerNumberModel(1, 1, NUM_CORES, 1);
        spinnerNumThreads.setModel(spinnerNumThreadsModel);

        // Init Plots
        final Dimension CHART_SIZE = new Dimension(600, 400);

        // Init data distribution panel
        panelDataPlot.setLayout(new java.awt.BorderLayout()); // Enforce correct layout
        dataDistributionChart = new XYChart("Data Distribution", "w1", "w2", false, true);
        panelDataPlot.add(dataDistributionChart.getPanel());
        panelDataPlot.setPreferredSize(CHART_SIZE);
        panelDataPlot.validate();

        // Init error rate panel
        panelErrorPlot.setLayout(new java.awt.BorderLayout()); // Enforce correct layout
        errorRateChart = new XYChart("Mean Squared Error", "Epoch", "Error value", true, false);
        panelErrorPlot.add(errorRateChart.getPanel());
        panelErrorPlot.setPreferredSize(CHART_SIZE);
        panelErrorPlot.validate();

        // Apply preferred size
        frame.pack();
        frame.setVisible(true);

        // Populate combo box with enum
        comboBoxWeightInitMethod.setModel(new DefaultComboBoxModel<>(WeigthInitializationMethod.values()));

        checkBoxStructHiddenLayers.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent itemEvent) {
                if (itemEvent.getStateChange() == ItemEvent.SELECTED) {
                    textStructHiddenLayers.setEnabled(false);
                } else {
                    textStructHiddenLayers.setEnabled(true);
                }
            }
        });
        checkBoxNumEpochsInf.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent itemEvent) {
                if (itemEvent.getStateChange() == ItemEvent.SELECTED) {
                    textNumEpochs.setEnabled(false);
                } else {
                    textNumEpochs.setEnabled(true);
                }
            }
        });
        checkAllowReiterationOfSample.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent itemEvent) {
                if (itemEvent.getStateChange() == ItemEvent.SELECTED) {
                    allowReiteration = true;
                } else {
                    allowReiteration = false;
                }
            }
        });
        buttonStart.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                { // Data file
                    if (dataFile == null || (!dataFile.exists())) {
                        printSyntaxError();
                        return;
                    }
                }
                { // Struct hidden layers
                    if (checkBoxStructHiddenLayers.isSelected()) {
                        structHiddenLayers = null;
                    } else {
                        String userInput = textStructHiddenLayers.getText();
                        if (!userInput.matches("^[1-9](,[1-9])*$")) {
                            printSyntaxError();
                            return;
                        }
                        structHiddenLayers = Arrays.stream(userInput.split(",")).mapToInt(Integer::parseInt).toArray();
                    }
                }
                { // Bias
                    String userInput = textBias.getText();
                    try {
                        bias = Double.parseDouble(userInput);
                    } catch (NumberFormatException e) {
                        printSyntaxError();
                        return;
                    }
                }
                { // Learning Rate
                    String userInput = textLearningRate.getText();
                    try {
                        learningRate = Double.parseDouble(userInput);
                    } catch (NumberFormatException e) {
                        printSyntaxError();
                        return;
                    }
                    if (learningRate <= 0 || learningRate >= 1) {
                        printSyntaxError();
                        return;
                    }
                }
                { // Num Epochs
                    if (checkBoxNumEpochsInf.isSelected()) {
                        maxEpochs = -1;
                    } else {
                        String userInput = textNumEpochs.getText();
                        try {
                            maxEpochs = Integer.parseInt(userInput);
                        } catch (NumberFormatException e) {
                            printSyntaxError();
                            return;
                        }
                        if (maxEpochs <= 0) {
                            printSyntaxError();
                            return;
                        }
                    }
                }
                { // Weight init method
                    if (comboBoxWeightInitMethod.getSelectedItem() == WeigthInitializationMethod.Random) {
                        weigthInitializationMethod = WeigthInitializationMethod.Random;
                    } else if (comboBoxWeightInitMethod.getSelectedItem() == WeigthInitializationMethod.Xavier) {
                        weigthInitializationMethod = WeigthInitializationMethod.Xavier;
                    }
                }
                { // Num Threads
                    numThreadsToUse = (int) spinnerNumThreads.getValue();
                }

                startCalculations();
            }
        });
        buttonOpen.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                JFileChooser fileChooser = new JFileChooser();
                int returnCode = fileChooser.showOpenDialog(rootPanel);

                if (returnCode == JFileChooser.APPROVE_OPTION) {
                    dataFile = fileChooser.getSelectedFile();
                    textFilename.setText(dataFile.getAbsolutePath());
                }

            }
        });
        buttonPause.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (isPaused) {
                    modeResumed();
                } else {
                    modePaused();
                }
            }
        });
        buttonReset.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                for (Thread t : threadList) {
                    t.stop();
                }
                frame.dispose();
                Main.runGUI();
            }
        });
        buttonExit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                for (Thread t : threadList) {
                    t.stop();
                }
                frame.dispose();
            }
        });
        checkThrottle.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (checkThrottle.isSelected()) {
                    sliderThrottle.setEnabled(true);
                    throttled = true;
                } else {
                    sliderThrottle.setEnabled(false);
                    throttled = false;
                }
            }
        });
        buttonClassify.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                String userInputW1 = textClassifyW1.getText();
                String userInputW2 = textClassifyW2.getText();
                double w1, w2;

                try {
                    w1 = Double.parseDouble(userInputW1);
                    w2 = Double.parseDouble(userInputW2);
                } catch (NumberFormatException e) {
                    printSyntaxError();
                    return;
                }

                double[] result = net.classify(w1, w2);
                System.out.println("Target Class: " + result[0] + " | Sum: " + result[1]);
            }
        });
    }

    private void printSyntaxError() {
        JOptionPane.showMessageDialog(rootPanel, "Syntax error while parsing given parameters.", "Syntax Error", JOptionPane.ERROR_MESSAGE);
    }

    private void startCalculations() {
        // Change button mode
        modeStart();

        // Read training data and configure hidden layers
        trainingData = Dataset.read(dataFile);
        if (structHiddenLayers == null) {
            structHiddenLayers = new int[]{Dataset.numFeatures};
        }

        // Plot training data to chart
        insertDataPoints(); // Add training data to function plot

        // Initialize dataset for mse values
        errorRateChart.addEmptySeries("mseValues");

        net = new NeuralesNetz(trainingData, structHiddenLayers, weigthInitializationMethod, bias, learningRate, maxEpochs, allowReiteration);

        // Initialize and start threads that do the calculations
        for (int i = 0; i < numThreadsToUse; i++) {
            threadList.add(new Thread(() -> {
                while (true) {
                    while (isPaused) {
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    HashMap<Long, Double> mseValues = net.train(1);
                    if (mseValues.isEmpty()) break; // This is important, break execution if training is done
                    errorRateChart.addValues(mseValues, "mseValues");

                    if (throttled) {
                        int throttleMilliSec = sliderThrottle.getValue() * 2;
                        try {
                            Thread.sleep(throttleMilliSec);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
                modeCompleted();
            }));
        }

        for (Thread t : threadList) {
            t.start();
        }
    }

    private void modeStart() {
        Component[] settingsPanelComponents = panelSettings.getComponents();
        for (Component component : settingsPanelComponents) {
            component.setEnabled(false);
        }
        buttonStart.setEnabled(false);

        buttonPause.setEnabled(true);
        isPaused = false;
    }

    private void modePaused() {
        buttonPause.setText("Resume");
        isPaused = true;
        buttonClassify.setEnabled(true);
    }

    private void modeResumed() {
        buttonPause.setText("Pause");
        isPaused = false;
        buttonClassify.setEnabled(false);
    }

    private void modeCompleted() {
        buttonPause.setEnabled(false);
        buttonClassify.setEnabled(true);
    }

    private void insertDataPoints() {
        // Shape/Line colors are bound to a XYSeries, so it is easier to create two series based on targetClass rather
        // than overriding methods from the lib
        ArrayList<Double[]> valuesTargetClass0 = new ArrayList<>();
        ArrayList<Double[]> valuesTargetClass1 = new ArrayList<>();
        for (int datasetNum = 0; datasetNum < trainingData.size(); datasetNum++) {
            Dataset curDataset = trainingData.get(datasetNum);
            if (curDataset.getTargetClass() == 0) {
                valuesTargetClass0.add(new Double[]{curDataset.getFeatures()[0], curDataset.getFeatures()[1]});
            } else if (curDataset.getTargetClass() == 1) {
                valuesTargetClass1.add(new Double[]{curDataset.getFeatures()[0], curDataset.getFeatures()[1]});
            } else {
                throw new IllegalArgumentException("Invalid target class."); // TODO
            }
        }
        dataDistributionChart.addSeries(valuesTargetClass0, Color.BLUE, "Class 0");
        dataDistributionChart.addSeries(valuesTargetClass1, Color.RED, "Class 1");
    }
}
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class NeuralNet implements Cloneable {
    // Training data
    private final ArrayList<Dataset> TRAINING_DATA;

    // How many input-/ and output neurons to use (Number of features = Number of input neurons)
    private final int NUM_FEATURES = Dataset.numFeatures;
    private final int NUM_OUTPUT_NEURONS = 1;
    // Various parameters regarding the training process
    private final double LEARNING_RATE;
    private final long MAX_EPOCHS; // How many epochs should be completed in total (-1 = infinite)
    private final double BIAS;
    private final boolean ALLOW_REITERATION;
    // How weights should be initialized on net creation
    private final WeigthInitializationMethod WEIGHT_INIT_METHOD;
    // Describes the topology of the neural net, including ALL layers
    private ArrayList<Layer> layers = new ArrayList<>();
    private long curNetEpoch = 1; // Number of the current epoch, starting at 1
    private int dataRecordNumPrevRun = 0; // At which data record of the sample data the train method has stopped last time
    private boolean netTrainCompleted = false; // Indicates whether further epochs should be done

    public NeuralNet(ArrayList<Dataset> trainingData, int[] structHiddenLayers, WeigthInitializationMethod weightInitializationMethod, double bias, double learningRate, long maxEpochs, boolean allowReiteration) {
        if (structHiddenLayers.length < 1)
            throw new IllegalArgumentException("At least one hidden layer must be specified.");
        if (learningRate <= 0 || learningRate >= 1)
            throw new IllegalArgumentException("Learning rate has to be in range (0; 1).");

        // Initialize some parameters
        this.TRAINING_DATA = trainingData;
        this.LEARNING_RATE = learningRate;
        this.MAX_EPOCHS = maxEpochs;
        this.ALLOW_REITERATION = allowReiteration;

        // Creating the net structure
        layers.add(new Layer(NUM_FEATURES, 0)); // Input layer first
        for (int i = 0; i < structHiddenLayers.length; i++) { // Hidden layers second
            layers.add(new Layer(structHiddenLayers[i], layers.get(i).getNumNeurons()));
        }
        layers.add(new Layer(NUM_OUTPUT_NEURONS, layers.get(layers.size() - 1).getNumNeurons())); // Output layer finally

        // Initialize the default weights and the bias
        this.WEIGHT_INIT_METHOD = weightInitializationMethod;
        this.BIAS = bias;
        initializeWeightsAndBias();
    }

    private void initializeWeightsAndBias() {
        if (WEIGHT_INIT_METHOD == WeigthInitializationMethod.Random) {
            for (int i = 0; i < layers.size(); i++) {
                Neuron[] neuronList = layers.get(i).getNeuronList();
                for (int j = 0; j < neuronList.length; j++) {
                    double[] weigths = neuronList[j].getWeigths();
                    for (int k = 0; k < weigths.length; k++) {
                        weigths[k] = -1.0 + 2. * Math.random();
                    }
                    neuronList[j].setBias(BIAS);
                }
            }
        } else if (WEIGHT_INIT_METHOD == WeigthInitializationMethod.Xavier) {
            // TODO
        }
    }

    public synchronized HashMap<Long, Double> train(long epochs) {
        HashMap<Long, Double> mseValues = new HashMap<>();

        if (netTrainCompleted) return mseValues; // Return empty map if train completed

        for (int dataRecordNum = dataRecordNumPrevRun, numEpochs = 0; dataRecordNum < TRAINING_DATA.size(); dataRecordNum++, curNetEpoch++, numEpochs++) {
            // Check if number of epochs given in method parameter have been reached
            if (numEpochs > epochs) {
                dataRecordNumPrevRun = dataRecordNum;
                break;
            }
            // Check if MAX_EPOCHS have been reached
            if (MAX_EPOCHS != -1 && curNetEpoch > MAX_EPOCHS) {
                netTrainCompleted = true;
                break;
            }

            // Step 1: Forward Pass
            // Activation of input neurons matches values from provided test data
            Neuron[] inputLayer = layers.get(0).getNeuronList();
            for (int featureNum = 0; featureNum < NUM_FEATURES; featureNum++) {
                inputLayer[featureNum].setActivation(TRAINING_DATA.get(dataRecordNum).getFeatures()[featureNum]);
            }

            Neuron[] previousNeuronList = inputLayer;
            // Now calculate and update activations beginning from first hidden layer to output layer
            for (int layerNum = 1; layerNum < layers.size(); layerNum++) { // For every layer beginning from first hidden layer...
                Neuron[] currentNeuronList = layers.get(layerNum).getNeuronList();
                for (int neuronNum = 0; neuronNum < currentNeuronList.length; neuronNum++) { // For every neuron in this layer...
                    double sum = 0;
                    for (int previousNeuronsNum = 0; previousNeuronsNum < previousNeuronList.length; previousNeuronsNum++) {
                        sum += currentNeuronList[neuronNum].getWeigths()[previousNeuronsNum] * previousNeuronList[previousNeuronsNum].getActivation();
                    }
                    sum += BIAS;
                    currentNeuronList[neuronNum].setActivation(activationFunction(sum, ActivationFunction.Sigmoid));
                }
                previousNeuronList = currentNeuronList;
            }

            // Step 2: Calculate MSE
            // TODO Alternative Kostenfunktion testen
            double targetOutput = TRAINING_DATA.get(dataRecordNum).getTargetClass();
            double mse = calculateMeanSquaredError(targetOutput);
            mseValues.put(curNetEpoch, mse);
            System.out.println("Epoch: " + curNetEpoch + " | Error: " + mse);

            // Step 3: Backpropagate
            for (int layerNum = layers.size() - 1; layerNum >= 1; layerNum--) {
                Neuron[] currentNeuronList = layers.get(layerNum).getNeuronList();
                for (int neuronNum = 0; neuronNum < currentNeuronList.length; neuronNum++) {
                    Neuron curNeuron = currentNeuronList[neuronNum];
                    for (int previousNeuronNum = 0; previousNeuronNum < curNeuron.getWeigths().length; previousNeuronNum++) {
                        double activationCurNeuron = curNeuron.getActivation();
                        double activationPrevNeuron = layers.get(layerNum - 1).getNeuronList()[previousNeuronNum].getActivation();
                        double deltaWeight = LEARNING_RATE * mse * activationPrevNeuron * activationCurNeuron * (1 - activationCurNeuron);
                        double curWeight = curNeuron.getWeigths()[previousNeuronNum];
                        curNeuron.setWeight(previousNeuronNum, curWeight - deltaWeight);
                    }
                }
            }
            if (dataRecordNum == TRAINING_DATA.size() - 1) {
                if (ALLOW_REITERATION) {
                    dataRecordNum = 0;
                } else {
                    netTrainCompleted = true;
                }
            }
        }
        return mseValues;
    }

    public double[] classify(double input1, double input2) { // TODO fix
        NeuralNet dummy;
        try {
            dummy = (NeuralNet) this.clone(); // Deep copy since activations should not actually change
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
        double[] result = new double[2];

        Neuron[] inputLayer = dummy.layers.get(0).getNeuronList();
        inputLayer[0].setActivation(input1);
        inputLayer[1].setActivation(input2);

        Neuron[] previousNeuronList = inputLayer;
        for (int layerNum = 1; layerNum < dummy.layers.size(); layerNum++) { // For every layer beginning from first hidden layer...
            Neuron[] currentNeuronList = dummy.layers.get(layerNum).getNeuronList();
            for (int neuronNum = 0; neuronNum < currentNeuronList.length; neuronNum++) { // For every neuron in this layer...
                double sum = 0;
                for (int previousNeuronsNum = 0; previousNeuronsNum < previousNeuronList.length; previousNeuronsNum++) {
                    sum += currentNeuronList[neuronNum].getWeigths()[previousNeuronsNum] * previousNeuronList[previousNeuronsNum].getActivation();
                }
                sum += dummy.BIAS;
                result[1] = sum;
                currentNeuronList[neuronNum].setActivation(activationFunction(sum, ActivationFunction.Sigmoid));
            }
            previousNeuronList = currentNeuronList;
        }

        double outputNeuronActivation = dummy.layers.get(layers.size() - 1).getNeuronList()[0].getActivation();
        if (outputNeuronActivation >= 0.5) {
            result[0] = 1;
        } else {
            result[0] = 0;
        }

        return result;
    }

    private double activationFunction(double value, ActivationFunction activationFunction) {
        if (activationFunction == ActivationFunction.Sigmoid) {
            return 1.0 / (1.0 + Math.exp(-value));
        } else if (activationFunction == ActivationFunction.RoundingUpOrDown) {
            return Math.round(value); // TODO weg
        }
        return 0;
    }

    private double calculateMeanSquaredError(double targetValue) {
        Neuron[] outputLayer = layers.get(layers.size() - 1).getNeuronList();
        int numPredictionValues = outputLayer.length;

        double errorRate = 0.0;
        for (int neuronNum = 0; neuronNum < numPredictionValues; neuronNum++) { // TODO weg, gibt eh nur ein output neuron
            errorRate += (Math.pow(targetValue - outputLayer[neuronNum].getActivation(), 2));
        }

        return (1.0 / 2.0) * errorRate;
    }

    public void dumpNetState() {
        // TODO
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        NeuralNet clone = (NeuralNet) super.clone(); // Shallow copy

        // Deep copy layers
        clone.layers = new ArrayList<>(layers.size());
        for (int i = 0; i < layers.size(); i++) {
            clone.layers.add(i, (Layer) layers.get(i).clone());
        }

        return clone;
    }
}

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class NeuralNet {
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
    private final WeightInitializationMethod WEIGHT_INIT_METHOD;
    // Describes the topology of the neural net, including ALL layers
    private final ArrayList<Layer> LAYERS = new ArrayList<>();
    private long curNetEpoch = 1; // Number of the current epoch, starting at 1
    private int dataRecordNumPrevRun = 0; // At which data record of the sample data the train method has stopped last time
    private boolean netTrainCompleted = false; // Indicates whether further epochs should be done

    public NeuralNet(ArrayList<Dataset> trainingData, int[] structHiddenLayers, WeightInitializationMethod weightInitializationMethod, double bias, double learningRate, long maxEpochs, boolean allowReiteration) {
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
        LAYERS.add(new Layer(NUM_FEATURES, 0)); // Input layer first
        for (int i = 0; i < structHiddenLayers.length; i++) { // Hidden layers second
            LAYERS.add(new Layer(structHiddenLayers[i], LAYERS.get(i).getNumNeurons()));
        }
        LAYERS.add(new Layer(NUM_OUTPUT_NEURONS, LAYERS.get(LAYERS.size() - 1).getNumNeurons())); // Output layer finally

        // Initialize the default weights and the bias
        this.WEIGHT_INIT_METHOD = weightInitializationMethod;
        this.BIAS = bias;
        initializeWeightsAndBias();
    }

    private void initializeWeightsAndBias() {
        if (WEIGHT_INIT_METHOD == WeightInitializationMethod.Random) {
            for (int i = 1; i < LAYERS.size(); i++) {
                Neuron[] neuronList = LAYERS.get(i).getNeuronList();
                for (int j = 0; j < neuronList.length; j++) {
                    double[] weights = neuronList[j].getWeights();
                    for (int k = 0; k < weights.length; k++) {
                        weights[k] = -1.0 + 2. * Math.random();
                    }
                    neuronList[j].setBias(BIAS);
                }
            }
        } else if (WEIGHT_INIT_METHOD == WeightInitializationMethod.XavierUniform) {
            for (int i = 1; i < LAYERS.size(); i++) {
                Neuron[] neuronList = LAYERS.get(i).getNeuronList();
                int fan_in = neuronList[0].getWeights().length;
                int fan_out;
                if (i == LAYERS.size() - 1) {
                    fan_out = 0;
                } else {
                    fan_out = LAYERS.get(i + 1).getNumNeurons();
                }
                double xavier_max = Math.sqrt(6.0 / (fan_in + fan_out)); // Upper bound
                double xavier_min = -xavier_max; // Lower bound
                Random random = new Random(); // Values calculated by Random are (approximately) uniformly distributed
                for (int j = 0; j < neuronList.length; j++) {
                    double[] weights = neuronList[j].getWeights();
                    for (int k = 0; k < weights.length; k++) {
                        weights[k] = random.nextDouble(xavier_max) + xavier_min;
                    }
                    neuronList[j].setBias(BIAS);
                }
            }
        }
    }

    /**
     * Method for training a neural net using the provided training data.
     *
     * @param epochs How many epochs should be calculated on this single method call. Does not interfere with other
     *               settings like "Num_Epochs" or "Allow reiteration of sample data"
     * @return HashMap containing x (Epoch num) and y (MSE) values suitable for displaying on a chart. Returns
     * empty map if the method has been called AND a termination condition ("Num_epochs" reached or whole dataset
     * has been parsed and reiteration is disallowed) has been reached.
     */
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
            Neuron[] inputLayer = LAYERS.get(0).getNeuronList();
            for (int featureNum = 0; featureNum < NUM_FEATURES; featureNum++) {
                inputLayer[featureNum].setActivation(TRAINING_DATA.get(dataRecordNum).getFeatures()[featureNum]);
            }

            Neuron[] previousNeuronList = inputLayer;
            // Now calculate and update activations beginning from first hidden layer to output layer
            for (int layerNum = 1; layerNum < LAYERS.size(); layerNum++) { // For every layer beginning from first hidden layer...
                Neuron[] currentNeuronList = LAYERS.get(layerNum).getNeuronList();
                for (int neuronNum = 0; neuronNum < currentNeuronList.length; neuronNum++) { // For every neuron in this layer...
                    double sum = 0;
                    for (int previousNeuronsNum = 0; previousNeuronsNum < previousNeuronList.length; previousNeuronsNum++) {
                        sum += currentNeuronList[neuronNum].getWeights()[previousNeuronsNum] * previousNeuronList[previousNeuronsNum].getActivation();
                    }
                    sum += currentNeuronList[neuronNum].getBias();
                    currentNeuronList[neuronNum].setActivation(activationFunction(sum, ActivationFunction.Sigmoid));
                }
                previousNeuronList = currentNeuronList;
            }

            // Step 2: Calculate MSE
            double targetOutput = TRAINING_DATA.get(dataRecordNum).getTargetClass();
            double mse = calculateMeanSquaredError(targetOutput);
            mseValues.put(curNetEpoch, mse);
            System.out.println("Epoch: " + curNetEpoch + " | Error: " + mse);

            // Step 3: Backpropagate
            for (int layerNum = LAYERS.size() - 1; layerNum >= 1; layerNum--) {
                Neuron[] currentNeuronList = LAYERS.get(layerNum).getNeuronList();
                for (int neuronNum = 0; neuronNum < currentNeuronList.length; neuronNum++) {
                    Neuron curNeuron = currentNeuronList[neuronNum];
                    for (int previousNeuronNum = 0; previousNeuronNum < curNeuron.getWeights().length; previousNeuronNum++) {
                        double activationCurNeuron = curNeuron.getActivation();
                        double activationPrevNeuron = LAYERS.get(layerNum - 1).getNeuronList()[previousNeuronNum].getActivation();
                        double deltaWeight = LEARNING_RATE * mse * activationPrevNeuron * activationCurNeuron * (1 - activationCurNeuron);
                        double curWeight = curNeuron.getWeights()[previousNeuronNum];
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

    /**
     * Classifies two given sample inputs.
     *
     * @param input1 Activation of input neuron1
     * @param input2 Activation of input neuron2
     * @return Double array containing [a,b,c]:
     * a: Target class in {0,1}
     * b: Output neuron activation with activation function applied
     * c: Output neuron activation without activation function applied
     */
    public double[] classify(double input1, double input2) {
        double[] result = new double[3];

        Neuron[] inputLayer = LAYERS.get(0).getNeuronList();
        inputLayer[0].setActivation(input1);
        inputLayer[1].setActivation(input2);

        Neuron[] previousNeuronList = inputLayer;
        for (int layerNum = 1; layerNum < LAYERS.size(); layerNum++) { // For every layer beginning from first hidden layer...
            Neuron[] currentNeuronList = LAYERS.get(layerNum).getNeuronList();
            for (int neuronNum = 0; neuronNum < currentNeuronList.length; neuronNum++) { // For every neuron in this layer...
                double sum = 0;
                for (int previousNeuronsNum = 0; previousNeuronsNum < previousNeuronList.length; previousNeuronsNum++) {
                    sum += currentNeuronList[neuronNum].getWeights()[previousNeuronsNum] * previousNeuronList[previousNeuronsNum].getActivation();
                }
                sum += BIAS;
                result[2] = sum;
                currentNeuronList[neuronNum].setActivation(activationFunction(sum, ActivationFunction.Sigmoid));
            }
            previousNeuronList = currentNeuronList;
        }

        double outputNeuronActivation = LAYERS.get(LAYERS.size() - 1).getNeuronList()[0].getActivation();
        result[1] = outputNeuronActivation;
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
        }
        return 0;
    }

    private double calculateMeanSquaredError(double targetValue) {
        Neuron[] outputLayer = LAYERS.get(LAYERS.size() - 1).getNeuronList();
        int numPredictionValues = outputLayer.length;

        double errorRate = 0.0;
        for (int neuronNum = 0; neuronNum < numPredictionValues; neuronNum++) {
            errorRate += (Math.pow(targetValue - outputLayer[neuronNum].getActivation(), 2));
        }

        return (1.0 / 2.0) * errorRate;
    }
}

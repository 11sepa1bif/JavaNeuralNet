/**
 * Class for creating a layer which is part of a neural net. A layer contains an array containing a list of Neurons.
 */
public class Layer {
    private final Neuron[] NEURON_LIST;
    private final int NUM_NEURONS;

    public Layer(int numNeurons, int numInputWeightsPerNeuron) {
        if (numNeurons < 1) throw new IllegalArgumentException("A layer must contain at least one neuron.");

        NEURON_LIST = new Neuron[numNeurons];
        for (int i = 0; i < numNeurons; i++) {
            NEURON_LIST[i] = new Neuron(numInputWeightsPerNeuron);
        }

        this.NUM_NEURONS = NEURON_LIST.length;
    }

    public Neuron[] getNeuronList() {
        return NEURON_LIST;
    }

    public int getNumNeurons() {
        return NUM_NEURONS;
    }
}
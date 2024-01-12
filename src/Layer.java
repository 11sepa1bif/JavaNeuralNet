public class Layer {
    private Neuron[] neuronList;
    private int numNeurons;

    public Layer(int numNeurons, int numInputWeightsPerNeuron) {
        if (numNeurons < 1) throw new IllegalArgumentException("A layer must contain at least one neuron.");

        neuronList = new Neuron[numNeurons];
        for (int i = 0; i < numNeurons; i++) {
            neuronList[i] = new Neuron(numInputWeightsPerNeuron);
        }

        this.numNeurons = neuronList.length;
    }

    public Neuron[] getNeuronList() {
        return neuronList;
    }

    public int getNumNeurons() {
        return numNeurons;
    }
}
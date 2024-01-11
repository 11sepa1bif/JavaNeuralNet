public class Layer implements Cloneable {
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

    @Override
    protected Object clone() throws CloneNotSupportedException {
        Layer clone = (Layer) super.clone();
        
        clone.neuronList = new Neuron[neuronList.length];
        for (int i = 0; i < neuronList.length; i++) {
            clone.neuronList[i] = (Neuron) neuronList[i].clone();
        }
        
        return clone;
    }
}
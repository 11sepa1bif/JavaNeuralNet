public class Neuron {
    private final double[] WEIGHTS; // Incoming weights
    private double bias;
    private double activation;

    public Neuron(int numInputWeights) {
        WEIGHTS = new double[numInputWeights];
    }

    public double[] getWeights() {
        return WEIGHTS;
    }

    public void setWeight(int index, double newWeight) {
        WEIGHTS[index] = newWeight;
    }

    public double getBias() {
        return bias;
    }

    public void setBias(double bias) {
        this.bias = bias;
    }

    public double getActivation() {
        return activation;
    }

    public void setActivation(double activation) {
        this.activation = activation;
    }
}
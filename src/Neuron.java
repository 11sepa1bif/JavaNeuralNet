public class Neuron {
    private double[] weights; // Incoming weights
    private double bias;
    private double activation;

    public Neuron(int numInputWeights) {
        weights = new double[numInputWeights];
    }

    public double[] getWeigths() {
        return weights;
    }

    public void setWeight(int index, double newWeight) {
        weights[index] = newWeight;
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
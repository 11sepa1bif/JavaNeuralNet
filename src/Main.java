import java.io.IOException;


public class Main {
    static NeuralNetGUI neuralNetGUI;

    public static void main(String[] args) {
        // Example 1: Crate test dataset in current working directory with 1000 sample values
        //TestDataGenerator.generateXOR(1_000, false, "XOR_1000.txt", false);

        // Example 2: Crate test dataset in current working directory with 10000 sample values, each target class having same amount of values (5000 each)
        //TestDataGenerator.generateXOR(10_000, true, "XOR_10000_balanced.txt", false);

        runGUI();
    }

    public static void runGUI() {
        neuralNetGUI = new NeuralNetGUI();
    }
}

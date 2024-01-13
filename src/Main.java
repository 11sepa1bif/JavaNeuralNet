public class Main {
    static NeuralNetGUI neuralNetGUI;

    public static void main(String[] args) {
        // Example 1: Crate "XOR" test dataset in current working directory with 1000 sample values
        //TestDataGenerator.generateXOR(1_000, 0, 1.0, false, "XOR_1000.txt", false);

        // Example 2: Crate "XOR" test dataset in current working directory with 1000 sample values in range (+0.0; +100.0)
        //TestDataGenerator.generateXOR(1_000, 0, 100, false, "XOR_1000_scaled.txt", false);

        // Example 3: Crate "Circle" test dataset in current working directory with 10000 sample values
        //TestDataGenerator.generateCircle(10_000, 0.5, false, "Circle_10_000.txt", false);

        runGUI();
    }

    public static void runGUI() {
        neuralNetGUI = new NeuralNetGUI();
    }
}

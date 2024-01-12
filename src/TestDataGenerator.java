import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class TestDataGenerator {
    private static double[][] testData;

    /**
     * Generates test data similar to XOR function, but varies values in between (+0.0; +1.0) and classifies them
     * by defining 4 quadrants, separated by borders at x = 0.5 and y = 0.5
     *
     * @param numTestdata              Number of total test data to generate
     * @param shift                    Shift the values up or down, e.g. shift=20 would produce values in range (+20.0; +21.0)
     * @param scale                    Scale the values up or down, e.g. scale=100 would produce values in range (+0.0; +100.0)
     * @param balanceTargetClassValues Whether the number of values of each target Class should be the same
     * @param outputFileName           Filename of the file the data will be written to. Can be null which disables writing data to a file.
     * @param allowOverride            Whether overriding of an existing file named "outputFileName" is allowed
     */
    public static void generateXOR(int numTestdata, double shift, double scale, boolean balanceTargetClassValues, String outputFileName, boolean allowOverride) {
        testData = new double[numTestdata][3];

        int num0 = 0;
        int num1 = 0;

        for (int testDataIndex = 0; testDataIndex < numTestdata; testDataIndex++) {
            int targetClass;
            double x = Math.random();
            double y = Math.random();

            if (x < 0.5 && y < 0.5) { // Lower left quadrant
                targetClass = 0;
            } else if (x > 0.5 && y < 0.5) { // Lower right quadrant
                targetClass = 1;
            } else if (x < 0.5 && y > 0.5) { // Upper left quadrant
                targetClass = 1;
            } else if (x > 0.5 && y > 0.5) { // Upper right quadrant
                targetClass = 0;
            } else { // Imaginary border at x = 0.5 and y = 0.5
                testDataIndex--;
                continue;
            }

            if (balanceTargetClassValues) {
                if (targetClass == 0 && num0 >= numTestdata / 2.0) {
                    testDataIndex--;
                    continue;
                } else if (targetClass == 1 && num1 >= numTestdata / 2.0) {
                    testDataIndex--;
                    continue;
                }
            }
            testData[testDataIndex][0] = (x * scale) + shift;
            testData[testDataIndex][1] = (y * scale) + shift;
            testData[testDataIndex][2] = targetClass;
            if (targetClass == 0) {
                num0++;
            } else {
                num1++;
            }
        }

        printToFile(outputFileName, allowOverride);
    }

    /**
     * Saves generated test data to file.
     *
     * @param filename      Filename to write data to
     * @param allowOverride Whether an existing file with the same filename, if existing, should be overridden
     */
    private static void printToFile(String filename, boolean allowOverride) {
        if (filename == null) return;

        if (!allowOverride) {
            File file = new File(System.getProperty("user.dir") + File.separator + filename);
            if (file.exists()) {
                throw new RuntimeException("File does already exist.");
            }
        }

        try {
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(filename, false));
            for (int line = 0; line < testData.length; line++) {
                bufferedWriter.write(Double.toString(testData[line][0]) + " ");
                bufferedWriter.write(Double.toString(testData[line][1]) + " ");
                bufferedWriter.write(Integer.toString((int) testData[line][2]));
                bufferedWriter.newLine();
            }
            bufferedWriter.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

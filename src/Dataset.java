import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class Dataset {
    static int numDataSets;
    static int numFeatures;

    private final double[] FEATURES; // Holds input features
    private final int TARGET_CLASS;

    public Dataset(double[] features, int targetClass) {
        this.FEATURES = features;
        this.TARGET_CLASS = targetClass;
    }

    public static ArrayList<Dataset> read(File file) {
        ArrayList<Dataset> data = new ArrayList<>();
        try {
            Scanner scanner = new Scanner(file);
            while (scanner.hasNext()) {
                double feature1 = Double.valueOf(scanner.next());
                double feature2 = Double.valueOf(scanner.next());
                int targetClass = Integer.valueOf(scanner.next());
                double[] features = {feature1, feature2};
                Dataset m = new Dataset(features, targetClass);
                data.add(m);
            }
            Dataset.numDataSets = data.size();
            Dataset.numFeatures = 2;
            scanner.close();
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
        }
        return data;
    }

    public double[] getFeatures() {
        return FEATURES;
    }

    public int getTargetClass() {
        return TARGET_CLASS;
    }
}

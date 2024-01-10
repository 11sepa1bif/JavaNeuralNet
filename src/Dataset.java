import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class Dataset {
    static int numDataSets;
    static int numFeatures;

    private double[] features; // Holds input features
    private int targetClass;

    public Dataset(double[] features, int targetClass) {
        this.features = features;
        this.targetClass = targetClass;
    }

    public static ArrayList<Dataset> read(File file) {
        ArrayList<Dataset> data = new ArrayList<Dataset>();
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
        return features;
    }

    public int getTargetClass() {
        return targetClass;
    }
}

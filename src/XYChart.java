import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Wrapper class for creating XYCharts provided by jfreechart library. Can be embedded in existing Java Swing Applications.
 * Implements useful methods for easy adding of series and values.
 */
public class XYChart extends ApplicationFrame {
    private ChartPanel xyChartPanel;
    private JFreeChart chart;
    private XYSeriesCollection datasetCollection; // One collection can hold multiple series (names datasets in this class)
    private XYLineAndShapeRenderer renderer; // Changes look of a series

    public XYChart(String title, String xAxisLabel, String yAxisLabel, boolean showLines, boolean showLegend) {
        super(null);
        datasetCollection = new XYSeriesCollection();
        chart = ChartFactory.createXYLineChart(title,
                xAxisLabel,
                yAxisLabel,
                datasetCollection,
                PlotOrientation.VERTICAL,
                showLegend,
                true,
                false);

        if (showLines) { // Whether the individual values should be connected with each other
            renderer = new XYLineAndShapeRenderer(true, true);
        } else {
            renderer = new XYLineAndShapeRenderer(false, true);
        }
        chart.getXYPlot().setRenderer(renderer);

        xyChartPanel = new ChartPanel(chart);
    }

    public JPanel getPanel() {
        return xyChartPanel;
    }

    /**
     * Add values to an existing series.
     *
     * @param values     Values to add using syntax <x,y>
     * @param seriesName Name of series to add values to
     */
    public void addValues(HashMap<Long, Double> values, String seriesName) {
        XYSeries dataset = datasetCollection.getSeries(seriesName); // Datasets are accessed via unique identifiers
        for (Map.Entry<Long, Double> entry : values.entrySet()) {
            long epoch = entry.getKey();
            double mse = entry.getValue();
            dataset.add(epoch, mse);
        }
    }

    /**
     * Add new empty series.
     *
     * @param name Unique name of new series
     */
    public void addEmptySeries(String name) {
        XYSeries dataset = new XYSeries(name);
        datasetCollection.addSeries(dataset);
    }

    /**
     * Add new empty series, add values to that series using a specified color.
     *
     * @param xy         Values to add
     * @param color      Color to paint the values with
     * @param seriesName Name of existing series
     */
    public void addSeries(ArrayList<Double[]> xy, Color color, String seriesName) {
        XYSeries dataset = new XYSeries(seriesName);
        for (Double[] value : xy) {
            dataset.add(value[0], value[1]);
        }
        datasetCollection.addSeries(dataset);

        renderer.setSeriesPaint(datasetCollection.getSeriesIndex(seriesName), color);
        chart.getXYPlot().setRenderer(renderer);
    }
}
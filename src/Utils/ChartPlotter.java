package Utils;

import org.apache.commons.math3.analysis.function.Log;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class ChartPlotter extends JFrame {
    public ChartPlotter(String title, ArrayList<Double> groundTruth, ArrayList<Double> approximated){
        super(title);

        XYDataset dataset = createDataset(groundTruth, approximated);
        JFreeChart chart = ChartFactory.createXYLineChart(
          title,
          "Time",
          "Time",
          dataset,
         PlotOrientation.VERTICAL,
            true,
            true,
            false
        );

        XYPlot plot = chart.getXYPlot();
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();

        // Make lines bolder
        renderer.setSeriesStroke(0, new BasicStroke(3.0f));
        renderer.setSeriesStroke(1, new BasicStroke(3.0f));

        // Add dots at each data point
        renderer.setSeriesShapesVisible(0, true);
        renderer.setSeriesShapesVisible(1, true);

        plot.setRenderer(renderer);

        ChartPanel panel = new ChartPanel(chart);
        setContentPane(panel);
    }

    private XYDataset createDataset(ArrayList<Double> groundTruth, ArrayList<Double> approximated) {
        XYSeriesCollection dataset = new XYSeriesCollection();

        XYSeries series1 = new XYSeries("Ground Truth");
        Logger.debug(groundTruth.toString());
        Logger.debug("Ground truth size: " + groundTruth.size());
        Logger.debug(approximated.toString());
        Logger.debug("Approximated size: " + approximated.size());
        series1.add(groundTruth.getLast().doubleValue(), 0.0);
        for (int i = 0; i < groundTruth.size(); i++){
            series1.add(groundTruth.getLast() - groundTruth.get(i), groundTruth.get(i));
        }
        dataset.addSeries(series1);

        XYSeries series2 = new XYSeries("Approximated");
        for (int i = 0; i < approximated.size(); i++){
            series2.add(groundTruth.get(i), approximated.get(i));
        }
        dataset.addSeries(series2);

        return dataset;
    }
}

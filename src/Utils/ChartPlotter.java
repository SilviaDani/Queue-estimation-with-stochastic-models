package Utils;

import org.apache.commons.math3.analysis.function.Log;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.DeviationRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.*;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class ChartPlotter extends JFrame {
    public ChartPlotter(String title, ArrayList<Double> groundTruth, ArrayList<Double> approximated, ArrayList<Double> stds){
        super(title);

        YIntervalSeriesCollection dataset = createDataset(groundTruth, approximated, stds);
        //XYDataset dataset = createDataset(groundTruth, approximated);
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
        DeviationRenderer renderer = new DeviationRenderer(true, false);

        // Make lines bolder
        renderer.setSeriesStroke(0, new BasicStroke(3.0f));
        renderer.setSeriesStroke(1, new BasicStroke(3.0f));

        // Add dots at each data point
        renderer.setSeriesShapesVisible(0, true);
        renderer.setSeriesShapesVisible(1, true);

        renderer.setSeriesFillPaint(0, new Color(255, 200, 200));
        renderer.setSeriesFillPaint(1, new Color(200, 200, 255));

        plot.setRenderer(renderer);

        ChartPanel panel = new ChartPanel(chart);
        setContentPane(panel);
    }

    private YIntervalSeriesCollection createDataset(ArrayList<Double> groundTruth, ArrayList<Double> approximated, ArrayList<Double> stds) {
        YIntervalSeriesCollection dataset = new YIntervalSeriesCollection();
        YIntervalSeries series1 = new YIntervalSeries("Ground Truth");
        series1.add(groundTruth.getLast(), 0.0, 0.0, 0.0);
        for (int i = 0; i < groundTruth.size(); i++){
            series1.add(groundTruth.getLast() - groundTruth.get(i), groundTruth.get(i), groundTruth.get(i), groundTruth.get(i));
        }
        dataset.addSeries(series1);

        YIntervalSeries series2 = new YIntervalSeries("Approximated");
        for (int i = 0; i < approximated.size(); i++){
            series2.add(groundTruth.get(i), approximated.get(i), approximated.get(i) - stds.get(i), approximated.get(i) + stds.get(i));
        }
        dataset.addSeries(series2);

        return dataset;



        /*XYSeries series1 = new XYSeries("Ground Truth");
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

        return dataset;*/
    }
}

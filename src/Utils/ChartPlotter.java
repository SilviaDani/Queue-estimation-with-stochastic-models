package Utils;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.util.ArrayList;

public class ChartPlotter extends JFrame {
    public ChartPlotter(String title, ArrayList<Double> groundTruth, ArrayList<Double> approximated){
        super(title);

        XYDataset dataset = createDataset(groundTruth, approximated);
        JFreeChart chart = ChartFactory.createXYLineChart(
          title,
          "Time",
          "Time",
          dataset
        );

        ChartPanel panel = new ChartPanel(chart);
        setContentPane(panel);
    }

    private XYDataset createDataset(ArrayList<Double> groundTruth, ArrayList<Double> approximated) {
        XYSeriesCollection dataset = new XYSeriesCollection();

        XYSeries series1 = new XYSeries("Ground Truth");
        for (int i = 0; i < groundTruth.size(); i++){
            series1.add(groundTruth.get(i), groundTruth.get(i));
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

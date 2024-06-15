package Utils;

import org.apache.commons.math3.analysis.function.Log;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.DeviationRenderer;
import org.jfree.chart.renderer.xy.StackedXYAreaRenderer;
import org.jfree.chart.renderer.xy.XYAreaRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.ui.RectangleAnchor;
import org.jfree.chart.ui.TextAnchor;
import org.jfree.data.xy.*;


import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;


public class ReggeSoltiPlotter extends JFrame {
    public ReggeSoltiPlotter(String title, HashMap<Double, Double> before, HashMap<Double, Double> after, int eventTime) {
        super(title);

        // Create dataset
        XYSeriesCollection lineDataset = createLineDataset(before, after, eventTime);
        XYSeriesCollection areaDataset = createAreaDataset(before, after, eventTime);

        // Create chart
        JFreeChart chart = ChartFactory.createXYLineChart(
                title,
                "Time",
                "CDF",
                lineDataset,
                PlotOrientation.VERTICAL,
                false,
                true,
                false
        );

        // Customize the plot
        XYPlot plot = chart.getXYPlot();

        // Line renderer for the full curves
        XYLineAndShapeRenderer lineRenderer = new XYLineAndShapeRenderer();
        lineRenderer.setSeriesPaint(0, new Color(139,0,0)); // f(x)
        lineRenderer.setSeriesPaint(1, new Color(139,0,0)); // g(x)
        lineRenderer.setSeriesPaint(2, new Color(0,0,139)); // h(x)
        plot.setRenderer(0, lineRenderer);

        // Disable shapes (dots) for all series
        lineRenderer.setSeriesShapesVisible(0, false);
        lineRenderer.setSeriesShapesVisible(1, false);
        lineRenderer.setSeriesShapesVisible(2, false);

        // Set line thickness for all series
        float lineWidth = 3.0f; // Set the desired line width
        lineRenderer.setSeriesStroke(0, new BasicStroke(lineWidth));
        lineRenderer.setSeriesStroke(1, new BasicStroke(lineWidth));
        lineRenderer.setSeriesStroke(2, new BasicStroke(lineWidth));

        // Area renderer for the partial colored area
        XYAreaRenderer areaRenderer = new XYAreaRenderer();
        areaRenderer.setSeriesPaint(0, new Color(139, 0, 0, 100)); // Partial area for f(x)
        plot.setDataset(1, areaDataset);
        plot.setRenderer(1, areaRenderer);

        ValueMarker marker = new ValueMarker(eventTime);
        marker.setPaint(Color.BLACK);
        marker.setLabel("Event");
        marker.setLabelAnchor(RectangleAnchor.TOP_LEFT);
        marker.setLabelTextAnchor(TextAnchor.TOP_LEFT);
        float[] dash = {10.0f, 10.0f};
        marker.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f));
        plot.addDomainMarker(marker);

        // Set the background color to white
        plot.setBackgroundPaint(Color.WHITE);

        // Set the gridline color to black
        plot.setDomainGridlinePaint(Color.BLACK);
        plot.setRangeGridlinePaint(Color.BLACK);
        plot.setDomainGridlinesVisible(true);
        plot.setRangeGridlinesVisible(true);

        // Show the chart in a panel
        ChartPanel panel = new ChartPanel(chart);
        panel.setPreferredSize(new Dimension(800, 600));
        setContentPane(panel);
    }

    private XYSeriesCollection createLineDataset(HashMap<Double, Double> before, HashMap<Double, Double> after, double eventTime) {
        XYSeriesCollection dataset = new XYSeriesCollection();
        XYSeries series1 = new XYSeries("Before with event");

        // Define the first function and add data points
        int eventIndex = -1;
        Set<Double> keys = before.keySet();
        Double[] timesBefore = keys.toArray(new Double[0]);
        Arrays.sort(timesBefore);
        for (int i = 0; i < before.size(); i++){
            if (timesBefore[i]>eventTime){
                eventIndex = i;
                break;
            }
            series1.add(timesBefore[i], before.get(timesBefore[i]));
        }
        dataset.addSeries(series1);

        XYSeries series2 = new XYSeries("After without event");
        for (int i = eventIndex - 1; i < before.size(); i++){
            series2.add(timesBefore[i], before.get(timesBefore[i]));
        }
        dataset.addSeries(series2);

        // Define the third function and add data points
        XYSeries series3 = new XYSeries("After with event");
        Set<Double> keys_after = after.keySet();
        Double[] timesAfter = keys_after.toArray(new Double[0]);
        Arrays.sort(timesAfter);
        for (int i = eventIndex - 1; i < after.size(); i++) {
            series3.add(timesAfter[i], after.get(timesAfter[i]));
        }
        dataset.addSeries(series3);

        Logger.debug("Length of series1: " + series1.getItemCount() + " Length of series2: " + series2.getItemCount() + " Length of series3: " + series3.getItemCount());
        return dataset;
    }

    private XYSeriesCollection createAreaDataset(HashMap<Double, Double> before, HashMap<Double, Double> after, double eventTime) {
        XYSeriesCollection dataset = new XYSeriesCollection();
        XYSeries series1 = new XYSeries("Before with event");

        // Define the partial area for the first function
        int eventIndex = -1;
        Set<Double> keys = before.keySet();
        Double[] timesBefore = keys.toArray(new Double[0]);
        Arrays.sort(timesBefore);
        for (int i = 0; i < before.size(); i++){
            if (timesBefore[i]>eventTime){
                eventIndex = i;
                break;
            }
            series1.add(timesBefore[i], before.get(timesBefore[i]));
        }
        dataset.addSeries(series1);

        return dataset;
    }
}


/*private YIntervalSeriesCollection createDataset(HashMap<Double, Double> before, HashMap<Double, Double> after, double eventTime) {
    YIntervalSeriesCollection dataset = new YIntervalSeriesCollection();
    YIntervalSeries series1 = new YIntervalSeries("Before with event");

    int eventIndex = -1;
    Set<Double> keys = before.keySet();
    Double[] timesBefore = keys.toArray(new Double[0]);
    Arrays.sort(timesBefore);
    for (int i = 0; i < before.size(); i++){
        if (timesBefore[i]>eventTime){
            eventIndex = i;
            break;
        }
        series1.add(new YIntervalDataItem(timesBefore[i], before.get(timesBefore[i]), 0, 0), true);
    }
    dataset.addSeries(series1);

    YIntervalSeries series2 = new YIntervalSeries("After without event");
    for (int i = eventIndex - 1; i < before.size(); i++){
        series2.add(new YIntervalDataItem(timesBefore[i], before.get(timesBefore[i]), 0, 0), true);
    }
    dataset.addSeries(series2);

    YIntervalSeries series3 = new YIntervalSeries("After with event");
    Set<Double> keys_after = after.keySet();
    Double[] timesAfter = keys_after.toArray(new Double[0]);
    Arrays.sort(timesBefore);
    for (int i = 0; i < after.size(); i++){
        series3.add(new YIntervalDataItem(timesAfter[i], after.get(timesAfter[i]), 0, 0), true);
    }
    dataset.addSeries(series3);

    return dataset;*/
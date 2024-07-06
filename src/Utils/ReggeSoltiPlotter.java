package Utils;

import org.apache.commons.math3.analysis.function.Log;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
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
    double timeStep;
    boolean isCDF = false;
    public ReggeSoltiPlotter(String title, HashMap<Integer, Double> before, HashMap<Integer, Double> after, HashMap<Integer, Double> groundTruth, double eventTime, double timeStep, String xAxisLabel, String yAxisLabel, double timeLimit) {
        super(title);
        this.timeStep = timeStep;
        // Create dataset
        XYSeriesCollection lineDataset = createLineDataset(before, after, groundTruth, eventTime);
        XYSeriesCollection areaDataset = createAreaDataset(before, eventTime);
        if (yAxisLabel.equals("CDF"))
            isCDF = true;

        // Create chart
        JFreeChart chart = ChartFactory.createXYLineChart(
                title,
                xAxisLabel,
                yAxisLabel,
                lineDataset,
                PlotOrientation.VERTICAL,
                false,
                true,
                false
        );

        NumberAxis xAxis = new NumberAxis();
        xAxis.setRange(0.0, timeLimit + 10.0);
        // Customize the plot
        XYPlot plot = chart.getXYPlot();
        plot.setDomainAxis(xAxis);

        // Line renderer for the full curves
        XYLineAndShapeRenderer lineRenderer = new XYLineAndShapeRenderer();
        lineRenderer.setSeriesPaint(0, new Color(139,0,0)); // f(x)
        lineRenderer.setSeriesPaint(1, new Color(139,0,0)); // g(x)
        lineRenderer.setSeriesPaint(2, new Color(0,0,139)); // h(x)
        lineRenderer.setSeriesPaint(3, new Color(0,0,0)); //
        plot.setRenderer(0, lineRenderer);

        // Disable shapes (dots) for all series
        lineRenderer.setSeriesShapesVisible(0, false);
        lineRenderer.setSeriesShapesVisible(1, false);
        lineRenderer.setSeriesShapesVisible(2, false);
        lineRenderer.setSeriesShapesVisible(3, false);

        // Set line thickness for all series
        float lineWidth = 3.0f; // Set the desired line width
        lineRenderer.setSeriesStroke(0, new BasicStroke(lineWidth));
        if (isCDF)
            lineRenderer.setSeriesStroke(1, new BasicStroke(lineWidth));
        else{
            float[] dash = {10.0f, 10.0f};
            lineRenderer.setSeriesStroke(1, new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f));
        }
        lineRenderer.setSeriesStroke(2, new BasicStroke(lineWidth));
        lineRenderer.setSeriesStroke(3, new BasicStroke(lineWidth));

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


    private XYSeriesCollection createLineDataset(HashMap<Integer, Double> before, HashMap<Integer, Double> after, HashMap<Integer, Double> GT, double eventTime) {
        XYSeriesCollection dataset = new XYSeriesCollection();
        XYSeries series1 = new XYSeries("Before without event");

        // Define the first function and add data points
        int eventIndex = 0;
        Set<Integer> keys = before.keySet();
        Integer[] timesBefore = keys.toArray(new Integer[0]);
        Arrays.sort(timesBefore);
        for (int i = 0; i < before.size(); i++){
            Logger.debug("Time: " + timesBefore[i] + " Value: " + before.get(timesBefore[i]) + " Event Time: " + eventTime);
            if (timesBefore[i]>((int) (eventTime / timeStep))){
                break;
            }
            else {
                series1.add(timesBefore[i] * timeStep, before.get(timesBefore[i]));
                eventIndex = i;
            }
        }
        dataset.addSeries(series1);

        XYSeries series2 = new XYSeries("After without event");
        for (int i = eventIndex; i < before.size(); i++){
            series2.add(timesBefore[i] * timeStep, before.get(timesBefore[i]));
        }
        dataset.addSeries(series2);

        // Define the third function and add data points
        XYSeries series3 = new XYSeries("After with event");
        Set<Integer> keys_after = after.keySet();
        Integer[] timesAfter = keys_after.toArray(new Integer[0]);
        Arrays.sort(timesAfter);
        for (int i = 0; i < after.size(); i++) {
            series3.add(timesAfter[i] * timeStep, after.get(timesAfter[i]));
        }
        dataset.addSeries(series3);

        // Define function to plot the ground truth
        XYSeries seriesGT = new XYSeries("Ground Truth");

        Set<Integer> keysGT = GT.keySet();
        Integer[] timesGT = keysGT.toArray(new Integer[0]);
        Arrays.sort(timesGT);
        for (int i = 0; i < GT.size(); i++){
            seriesGT.add(timesGT[i] * timeStep, GT.get(timesGT[i]));
           // ! seriesGT.add(new YIntervalDataItem(timesGT[i], GT.get(timesGT[i]), 0, 0), true);
        }
        dataset.addSeries(seriesGT);

        Logger.debug("Length of series1: " + series1.getItemCount() + " Length of series2: " + series2.getItemCount() + " Length of series3: " + series3.getItemCount() + " Length of seriesGT: " + seriesGT.getItemCount() );
        return dataset;
    }
    private XYSeriesCollection createAreaDataset(HashMap<Integer, Double> before, double eventTime) {
        XYSeriesCollection dataset = new XYSeriesCollection();
        XYSeries series1 = new XYSeries("Before with event");

        // Define the partial area for the first function
        Set<Integer> keys = before.keySet();
        Integer[] timesBefore = keys.toArray(new Integer[0]);
        Arrays.sort(timesBefore);
        for (int i = 0; i < before.size(); i++){
            if (timesBefore[i]>((int) (eventTime / timeStep))){
                break;
            }
            else
                series1.add(timesBefore[i] * timeStep, before.get(timesBefore[i]));
        }
        dataset.addSeries(series1);

        return dataset;
    }
}
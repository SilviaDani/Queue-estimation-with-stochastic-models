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
import java.util.HashMap;
import java.util.Set;


public class ReggeSoltiPlotter extends JFrame{
    public ReggeSoltiPlotter(String title, HashMap<Double, Double> before, HashMap<Double, Double> after, int eventTime){
        super(title);

        YIntervalSeriesCollection dataset = createDataset(before, after, eventTime);
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
        StackedXYAreaRenderer renderer = new StackedXYAreaRenderer(XYAreaRenderer.AREA_AND_SHAPES);

        // Make lines bolder
        renderer.setSeriesStroke(0, new BasicStroke(3.0f));
        renderer.setSeriesStroke(1, new BasicStroke(3.0f));
        renderer.setSeriesStroke(2, new BasicStroke(3.0f));

        //highlights area under curve "before" before the event we are considering
        renderer.setOutline(0, true);

        plot.setRenderer(renderer);

        ChartPanel panel = new ChartPanel(chart);
        setContentPane(panel);
    }

    private YIntervalSeriesCollection createDataset(HashMap<Double, Double> before, HashMap<Double, Double> after, double eventTime) {
        YIntervalSeriesCollection dataset = new YIntervalSeriesCollection();
        YIntervalSeries series1 = new YIntervalSeries("Before with event");
        int eventIndex = -1;
        Double[] timesBefore = (Double[]) before.keySet().toArray();
        for (int i = 0; i < before.size(); i++){
            if (timesBefore[i]>eventTime){
                eventIndex = i;
                break;
            }
            series1.add(timesBefore[i], before.get(timesBefore[i]));
        }
        dataset.addSeries(series1);

        YIntervalSeries series2 = new YIntervalSeries("After without event");
        for (int i = eventIndex; i < before.size(); i++){
            series2.add(timesBefore[i], before.get(timesBefore[i]));
        }
        dataset.addSeries(series2);

        YIntervalSeries series3 = new YIntervalSeries("After with event");
        Object[] timesAfter = after.keySet().toArray();
        for (int i = 0; i < after.size(); i++){
            series3.add(timesAfter[i], after.get(timesAfter[i]));
        }
        dataset.addSeries(series3);

        return dataset;
    }
}

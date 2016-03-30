package application.node.implementations;

import application.data.SavableAttribute;
import application.data.chart.Chart;
import application.data.chart.ChartPoint;
import application.data.chart.ChartSeries;
import application.gui.Controller;
import application.gui.UI;
import application.node.design.DrawableNode;
import application.utils.NodeRunParams;
import javafx.embed.swing.SwingNode;
import javafx.scene.control.Tab;
import javafx.scene.layout.AnchorPane;
import org.apache.log4j.Logger;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.util.ArrayList;
import java.util.List;

/**
 * This purpose of this class is to draw graphs and charts of data that is passed into it.
 *
 * @author Alex Brown
 */

public class ChartNode extends DrawableNode {
    private JFreeChart jFreeChart = null;
    private SwingNode chartSwingNode = new SwingNode();

    private static Logger log = Logger.getLogger(ChartNode.class);

    /**
     * This method is used to copy a {@link application.node.implementations.ChartNode} and give back a new object.
     *
     * @param chartNode The {@link application.node.implementations.ChartNode} we want to create a copy of.
     */
    public ChartNode(ChartNode chartNode) {
        this.setX(chartNode.getX());
        this.setY(chartNode.getY());
        this.setWidth(chartNode.getWidth());
        this.setHeight(chartNode.getHeight());
        this.setColor(chartNode.getColor());
        this.setScale(chartNode.getScale());
        this.setContainedText(chartNode.getContainedText());
        //this.setProgramUuid(chartNode.getProgramUuid());
        this.setNextNodeToRun(chartNode.getNextNodeToRun());
    }

    public ChartNode() {
        super();
    }

    public Tab createInterface() {
        Controller controller = Controller.getInstance();

        Tab tab = controller.createDefaultNodeTab(this);
        AnchorPane anchorPane = controller.getContentAnchorPaneOfTab(tab);

        chartSwingNode = new SwingNode();
        ChartPanel cpanel = new ChartPanel(jFreeChart);
        chartSwingNode.setContent(cpanel);

        UI.setAnchorMargins(chartSwingNode, 50.0, 0.0, 11.0, 0.0);

        anchorPane.getChildren().add(chartSwingNode);

        return tab;
    }

    /**
     * @return
     */

    public List<SavableAttribute> getDataToSave() {
        List<SavableAttribute> savableAttributes = new ArrayList<>();

        savableAttributes.addAll(super.getDataToSave());

        return savableAttributes;
    }

    /**
     * @param whileWaiting
     * @param nodeRunParams
     */
    public void run(Boolean whileWaiting, NodeRunParams nodeRunParams) {
        if (nodeRunParams.getOneTimeVariable() != null && nodeRunParams.getOneTimeVariable() instanceof Chart) {
            Chart chartData = (Chart) nodeRunParams.getOneTimeVariable();

            // Create the chart data from what we passed in
            XYSeriesCollection data = new XYSeriesCollection();
            for (ChartSeries chartSeries : chartData.getSeriesList()) {
                XYSeries xySeries = new XYSeries(chartSeries.getSeriesTitle());

                for (ChartPoint chartPoint : chartSeries.getPoints()) {
                    xySeries.add(chartPoint.getX(), chartPoint.getY());
                }

                data.addSeries(xySeries);
            }

            JFreeChart newChart = ChartFactory.createXYLineChart(chartData.getTitle(), chartData.getXAxisTitle(), chartData.getYAxisTitle(),
                    data, PlotOrientation.VERTICAL, chartData.getShowLegend(), false, false);
            XYPlot xyp = newChart.getXYPlot();

            xyp.getDomainAxis().setVerticalTickLabels(true);
            xyp.getDomainAxis().setStandardTickUnits(NumberAxis.createIntegerTickUnits());
            xyp.getRangeAxis().setStandardTickUnits(NumberAxis.createIntegerTickUnits());

            jFreeChart = newChart;
            ChartPanel cpanel = new ChartPanel(jFreeChart);
            chartSwingNode.setContent(cpanel);
        } else {
            log.info("Chart data was empty, nothing was passed in");
        }
    }
}

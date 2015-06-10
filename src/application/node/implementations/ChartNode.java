package application.node.implementations;

import application.data.SavableAttribute;
import application.data.chart.Chart;
import application.data.chart.ChartPoint;
import application.data.chart.ChartSeries;
import application.gui.Controller;
import application.node.design.DrawableNode;
import application.utils.NodeRunParams;
import javafx.embed.swing.SwingNode;
import javafx.scene.control.Tab;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
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

public class ChartNode extends DrawableNode {
    private JFreeChart jFreeChart = null;
    private SwingNode chartSwingNode = new SwingNode();

    // This will make a copy of the node passed to it
    public ChartNode(ChartNode chartNode) {
        this.setId(-1);
        this.setX(chartNode.getX());
        this.setY(chartNode.getY());
        this.setWidth(chartNode.getWidth());
        this.setHeight(chartNode.getHeight());
        this.setColor(chartNode.getColor());
        this.setScale(chartNode.getScale());
        this.setContainedText(chartNode.getContainedText());
        this.setProgramId(chartNode.getProgramId());
        this.setNextNodeToRun(chartNode.getNextNodeToRun());
    }

    public ChartNode(Integer id, Integer programId) {
        super(id, programId);
    }

    public ChartNode(Double x, Double y, String containedText) {
        super(x, y, 50.0, 40.0, Color.BLACK, containedText, -1, -1);
    }

    public ChartNode(Double x, Double y, Double width, Double height, Color color, String containedText, Integer programId, Integer id) {
        super(x, y, width, height, color, containedText, programId, id);
    }

    public Tab createInterface() {
        Controller controller = Controller.getInstance();

        Tab tab = controller.createDefaultNodeTab(this);
        AnchorPane anchorPane = (AnchorPane) tab.getContent();

        chartSwingNode = new SwingNode();
        ChartPanel cpanel = new ChartPanel(jFreeChart);
        chartSwingNode.setContent(cpanel);

        AnchorPane.setBottomAnchor(chartSwingNode, 0.0);
        AnchorPane.setLeftAnchor(chartSwingNode, 11.0);
        AnchorPane.setRightAnchor(chartSwingNode, 0.0);
        AnchorPane.setTopAnchor(chartSwingNode, 50.0);

        anchorPane.getChildren().add(chartSwingNode);

        return tab;
    }

    public List<SavableAttribute> getDataToSave() {
        List<SavableAttribute> savableAttributes = new ArrayList<>();

        savableAttributes.addAll(super.getDataToSave());

        return savableAttributes;
    }

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
            System.out.println("Chart data was empty, nothing was passed in");
        }
    }
}

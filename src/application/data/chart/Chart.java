package application.data.chart;

import java.util.ArrayList;
import java.util.List;

public class Chart {
    private String title = "";
    private String xAxisTitle = "";
    private String yAxisTitle = "";
    private Boolean showLegend = false;
    private List<ChartSeries> seriesList = new ArrayList<>();

    public Chart() {
    }

    public void addSeries(ChartSeries chartSeries) {
        seriesList.add(chartSeries);
    }

    public String getXAxisTitle() {
        return xAxisTitle;
    }

    public void setXAxisTitle(String xAxisTitle) {
        this.xAxisTitle = xAxisTitle;
    }

    public String getYAxisTitle() {
        return yAxisTitle;
    }

    public void setYAxisTitle(String yAxisTitle) {
        this.yAxisTitle = yAxisTitle;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<ChartSeries> getSeriesList() {
        return seriesList;
    }

    public Boolean getShowLegend() {
        return showLegend;
    }

    public void setShowLegend(Boolean showLegend) {
        this.showLegend = showLegend;
    }
}

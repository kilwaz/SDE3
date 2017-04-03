package application.data.chart;

import java.util.ArrayList;
import java.util.List;

public class ChartSeries {
    private String seriesTitle = "";
    private List<ChartPoint> points = new ArrayList<>();

    public ChartSeries() {
    }

    public void addPoint(ChartPoint chartPoint) {
        points.add(chartPoint);
    }

    public List<ChartPoint> getPoints() {
        return points;
    }

    public void setSeriesTitle(String seriesTitle) {
        this.seriesTitle = seriesTitle;
    }

    public String getSeriesTitle() {
        return seriesTitle;
    }
}

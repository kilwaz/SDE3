package application.net.proxy;

import application.error.Error;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GroupedRequests extends ArrayList<MetaRecordedRequest> {
    private static final int SIZE = 1;
    private static final int DURATION = 2;
    private static Logger log = Logger.getLogger(GroupedRequests.class);

    public GroupedRequests() {
    }

    public Integer getAverageResponseDuration() {
        Integer total = 0;
        for (MetaRecordedRequest request : this) {
            total += request.getDuration();
        }
        return total / size();
    }

    public Integer getAverageResponseSize() {
        Integer total = 0;
        for (MetaRecordedRequest request : this) {
            total += request.getResponseSize();
        }
        return total / size();
    }

    public Integer getSizesPercentile(Double percent) {
        return getPercentile(percent, SIZE);
    }

    public Integer getDurationPercentile(Double percent) {
        return getPercentile(percent, DURATION);
    }

    // Value from 0 to 1 expected as percent
    private Integer getPercentile(Double percent, int type) {
        if (percent < 0 || percent > 1) {
            Error.PERCENTILE_OUT_OF_RANGE.record().create();
            return -1;
        }

        // Create the list
        List<Integer> valueList = new ArrayList<>();
        for (MetaRecordedRequest request : this) {
            if (type == SIZE) {
                valueList.add(request.getResponseSize());
            } else if (type == DURATION) {
                valueList.add(request.getDuration());
            }
        }

        if (valueList.size() > 0) {
            Collections.sort(valueList);
            double index = percent * valueList.size();
            if (index == 0) { // In case of looking for 0th percentile
                index = 1;
            }
            if ((index % 1) == 0) { // If a whole number
                if (index < valueList.size()) { // If we are at the end anyway
                    Integer value1 = valueList.get((int) index - 1);
                    Integer value2 = valueList.get((int) index);
                    return (value1 + value2) / 2;
                }
            }
            index = Math.ceil(index);
            return valueList.get((int) index - 1);
        } else {
            return 0;
        }
    }

    public GroupedRequests filter(String filterURL) {
        GroupedRequests recordedRequests = new GroupedRequests();

        for (MetaRecordedRequest request : this) {
            String url = request.getUrl();
            String cleanedURL = url;
            if (url.contains("?")) {
                cleanedURL = url.substring(0, url.indexOf("?"));
            }

            if (filterURL != null && filterURL.equals(cleanedURL)) {
                recordedRequests.add(request);
            }
        }

        return recordedRequests;
    }
}

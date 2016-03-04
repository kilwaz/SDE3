package application.net.proxy;

import java.util.ArrayList;

public class GroupedRequests extends ArrayList<RecordedRequest> {
    public GroupedRequests() {
    }

    public Integer getAverageResponseDuration() {
        Integer total = 0;
        for (RecordedRequest request : this) {
            total += request.getDuration();
        }
        return total / size();
    }

    public Integer getAverageResponseSize() {
        Integer total = 0;
        for (RecordedRequest request : this) {
            total += request.getResponseSize();
        }
        return total / size();
    }

    public GroupedRequests filter(String filterURL) {
        GroupedRequests recordedRequests = new GroupedRequests();

        for (RecordedRequest request : this) {
            String url = request.getURL();
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

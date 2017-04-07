package sde.application.net.proxy;

import sde.application.error.Error;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;

public class HttpClientRetryHandler implements HttpRequestRetryHandler {
    private final Integer maxRetryCount;

    public HttpClientRetryHandler(Integer maxRetryCount) {
        this.maxRetryCount = maxRetryCount;
    }

    @Override
    public boolean retryRequest(IOException exception, int executionCount, HttpContext httpContext) {
        if (executionCount >= maxRetryCount) {
            Error.HTTP_CLIENT_RETRY_MAX_ATTEMPTS_REACHED.record().create();
            return false;
        }
        if (exception instanceof org.apache.http.NoHttpResponseException) {
            Error.HTTP_CLIENT_RETRY_CONNECTION_ATTEMPT.record().additionalInformation("NoHttpResponse - Attempt number " + executionCount).create();
            return true;
        } else {
            Error.HTTP_CLIENT_RETRY_CONNECTION_ATTEMPT.record().additionalInformation("Attempt number " + executionCount).create();
            return true;
        }
    }
}

package com.visitscotland.ccg.exception;

import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.HttpClientErrorException;

public class TraceApiException extends RuntimeException {

    public TraceApiException(String message) {
        super(message);
    }

    public TraceApiException(String message, HttpClientErrorException cause) {
        super(message, cause);
    }

    public HttpStatusCode getStatusCode() {
        if (isApiError()) {
            return ((HttpClientErrorException) getCause()).getStatusCode();
        }
        return HttpStatusCode.valueOf(500);
    }

    public String getApiMessage() {
        if (isApiError()) {
            return getCause().getMessage();
        }
        return "There was an error processing the response from Trace API";
    }

    public boolean isApiError() {
        return getCause() != null;
    }
}

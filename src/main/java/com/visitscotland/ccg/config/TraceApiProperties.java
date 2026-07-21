package com.visitscotland.ccg.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "trace-api")
public class TraceApiProperties {

    private String baseUrl;
    private String[] removeProperties;
    private boolean enabled;
    private String apiKey;

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String[] getRemoveProperties() {
        return removeProperties;
    }

    public void setRemoveProperties(String[] removeProperties) {
        this.removeProperties = removeProperties;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
}
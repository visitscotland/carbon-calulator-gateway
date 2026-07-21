package com.visitscotland.ccg.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "breg")
public class BregProperties {

    private String serviceUrl;
    private String[] removeProperties;
    private boolean enabled;

    public String getServiceUrl() {
        return serviceUrl;
    }

    public void setServiceUrl(String serviceUrl) {
        this.serviceUrl = serviceUrl;
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
}
package com.visitscotland.ccg.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "recaptcha")
public class ReCaptchaProperties {

    private String secretkey;

    private boolean enabled;

    public String getSecretkey() {
        return secretkey;
    }

    public void setSecretkey(String secretkey) {
        this.secretkey = secretkey;
    }

    public boolean isRecaptchaEnabled() {
        return enabled;
    }

    public void setRecaptchaEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}

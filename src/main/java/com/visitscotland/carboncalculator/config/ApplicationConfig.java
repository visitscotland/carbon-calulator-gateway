package com.visitscotland.carboncalculator.config;

import com.visitscotland.recaptcha.ReCaptcha;
import com.visitscotland.utils.info.NetworkUtils;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class ApplicationConfig {

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }

    @Bean
    public ReCaptcha reCaptcha(){
        return ReCaptcha.getInstance();
    }

    @Bean
    public NetworkUtils networkUtils() {
        return new NetworkUtils();
    }
}

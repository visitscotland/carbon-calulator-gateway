package com.visitscotland.ccg.notification;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Service
public class EmailComposer {

    private final ResourceLoader resourceLoader;

    public EmailComposer(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public String compose(String resourcePath, Map<String, String> variables) throws IOException {

        Resource resource = resourceLoader.getResource("classpath:" + resourcePath);

        String template = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

        for (Map.Entry<String, String> entry : variables.entrySet()) {
            template = template.replace("{{"+ entry.getKey() + "}}", entry.getValue());
        }

        return template;
    }
}

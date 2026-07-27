package com.visitscotland.ccg.notification;

import com.visitscotland.ccg.config.EmailProperties;
import com.visitscotland.ccg.exception.TraceApiException;
import com.visitscotland.ccg.exception.VsException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class EmailNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(EmailNotificationService.class);

    private final EmailProperties properties;
    private final JavaMailSender mailSender;
    private final ResourceLoader resourceLoader;

    public EmailNotificationService(EmailProperties properties, JavaMailSender mailSender, ResourceLoader resourceLoader) {
        this.properties = properties;
        this.mailSender = mailSender;
        this.resourceLoader = resourceLoader;
    }

    public void notify(TraceApiException traceApiException, VsException vsException, String submissionId) {
        if (properties.isEnabled()) {
            try {
                String message = compose(traceApiException, vsException, submissionId);
                send(message);
            } catch (IOException | MessagingException | MailException e) {
                logger.error("Unable to send notification email: {}", e.getMessage(), e);
            }
        }
    }

    private String compose(TraceApiException traceApiException,
                           VsException vsException,
                           String submissionId) throws IOException {

        Resource resource = resourceLoader.getResource("classpath:templates/notification/error-notification.html");

        String template = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

        template = template.replace("{{traceError}}",
                traceApiException == null ? "" :
                        "<li>Trace API responded with status code "
                                + traceApiException.getStatusCode()
                                + " and message: <pre>"
                                + traceApiException.getApiMessage()
                                + "</pre>"
                                + "</li>");

        template = template.replace("{{bregError}}",
                vsException == null ? "" :
                        "<li>BREG responded with the error message: "
                                + vsException.getMessage()
                                + "</li>");

        template = template.replace("{{recoveryNote}}",
                traceApiException == null || vsException == null
                        ? "<i>The submission is recoverable but needs to be manually processed.</i>"
                        : "<strong>The submission is not recoverable because neither service processed the data.</strong>");

        template = template.replace("{{submissionId}}", submissionId);

        return template;
    }

    public void send(String htmlMessage) throws MessagingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "UTF-8");

        helper.setTo(properties.getRecipients());
        helper.setSubject(properties.getSubject());
        helper.setText(htmlMessage, true); // true = HTML

        mailSender.send(mimeMessage);
    }
}

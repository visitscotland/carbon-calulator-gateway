package com.visitscotland.ccg.notification;

import com.visitscotland.ccg.config.EmailProperties;
import com.visitscotland.ccg.exception.TraceApiException;
import com.visitscotland.ccg.exception.VsException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class EmailNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(EmailNotificationService.class);

    private final EmailProperties properties;
    private final JavaMailSender mailSender;
    private final EmailComposer composer;


    public EmailNotificationService(EmailProperties properties, JavaMailSender mailSender, EmailComposer composer) {
        this.properties = properties;
        this.mailSender = mailSender;
        this.composer = composer;
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
        Map<String, String> variables = new HashMap<>();

        variables.put("traceError", traceApiException == null ? "" :
                "<li>Trace API responded with status code "
                        + traceApiException.getStatusCode()
                        + " and message: <pre>"
                        + traceApiException.getApiMessage()
                        + "</pre>"
                        + "</li>");

        variables.put("bregError", vsException == null ? "" :
                        "<li>BREG responded with the error message: "
                                + vsException.getMessage()
                                + "</li>");

        variables.put("recoveryNote",
                traceApiException == null || vsException == null
                        ? "<i>The submission is recoverable but needs to be manually processed.</i>"
                        : "<strong>The submission is not recoverable because neither service processed the data.</strong>");

        variables.put("submissionId", submissionId);

        return composer.compose("templates/notification/error-notification.html", variables);
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

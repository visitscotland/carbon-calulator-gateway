package com.visitscotland.ccg.notification;

import com.visitscotland.ccg.config.EmailProperties;
import com.visitscotland.ccg.exception.TraceApiException;
import com.visitscotland.ccg.exception.VsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class EmailNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(EmailNotificationService.class);

    private final EmailProperties properties;
    private final JavaMailSender mailSender;

    public EmailNotificationService(EmailProperties properties, JavaMailSender mailSender) {
        this.properties = properties;
        this.mailSender = mailSender;
    }

    public void notify(TraceApiException traceApiException, VsException vsException, String submissionId) {
        if (properties.isEnabled()) {
            String message = compose(traceApiException, vsException, submissionId);
            send(message);
        }
    }

    private String compose(TraceApiException traceApiException, VsException vsException, String submissionId) {
        StringBuilder message = new StringBuilder();
        message.append("<p>There has been an error while processing the Registration for the Carbon Calculator</p>");
        message.append("<ol>");

        if (traceApiException != null) {
            message.append("<li>")
                    .append("Trace API Responded with status code ")
                    .append(traceApiException.getStatusCode())
                    .append(" and message: ")
                    .append(traceApiException.getApiMessage())
                    .append("</li>");
        }

        if (vsException != null) {
            message.append("<li>")
                    .append("BREG responded with the error message: ")
                    .append(vsException.getMessage())
                    .append("</li>");
        }
        message.append("</ol>");

        message.append("<p>");
        if (traceApiException == null || vsException == null) {
            message.append("Note: <i>The submission is recoverable buy it needs to be manually processed.</i>");
        } else {
            message.append("Note: <strong> The submission is not recoverable as none of the service were able to process the data</strong>");
        }

        message.append("<p>The submission ID is ");
        message.append(submissionId);
        message.append("</p>");

        logger.info(message.toString());

        return message.toString();
    }

    public void send(String messageText) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();

            message.setTo(properties.getRecipients());
            message.setSubject(properties.getSubject());
            message.setText(messageText);

            mailSender.send(message);
        } catch (MailException e) {
            logger.error("Unable to send notification email: {}", e.getMessage(), e);
        }
    }
}

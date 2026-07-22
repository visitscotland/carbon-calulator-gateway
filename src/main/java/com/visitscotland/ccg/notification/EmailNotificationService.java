package com.visitscotland.ccg.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class EmailNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(EmailNotificationService.class);


    private final JavaMailSender mailSender;

    public EmailNotificationService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void notify(String messageText, String subject, String[] formEmailRecipient) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();

            message.setTo(formEmailRecipient);
            message.setSubject(subject);
            message.setText(messageText);

            mailSender.send(message);
        } catch (MailException e) {
            logger.error("Unable to send notification email: {}", e.getMessage(), e);
        }
    }
}

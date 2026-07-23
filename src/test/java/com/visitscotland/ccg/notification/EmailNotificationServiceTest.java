package com.visitscotland.ccg.notification;

import com.visitscotland.ccg.config.EmailProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailNotificationServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Captor
    private ArgumentCaptor<SimpleMailMessage> messageCaptor;

    private EmailNotificationService service;
    private EmailProperties properties;

    @BeforeEach
    void setUp() {
        properties = new EmailProperties();
        properties.setEnabled(true);
        properties.setSubject("This is an email notification");

        service = new EmailNotificationService(properties, mailSender);
    }

    @Test
    @DisplayName("An email is sent with the submission ID")
    void shouldSendEmail() {
        String[] recipients = new String[]{
                "mail-whatcher-1@visitscotland.com",
                "mail-whatcher-2@visitscotland.com"
        };
        String subject = "This is an email notification";
        String submissionId = UUID.randomUUID().toString();

        properties.setRecipients (new String[]{
            "mail-whatcher-1@visitscotland.com",
            "mail-whatcher-2@visitscotland.com"
        });
        properties.setSubject(subject);

        service.notify(null, null, submissionId);

        verify(mailSender).send(messageCaptor.capture());

        SimpleMailMessage message = messageCaptor.getValue();

        assertArrayEquals(recipients, message.getTo());
        assertEquals("This is an email notification", message.getSubject());
        assertNotNull(message.getText());
        assertTrue(message.getText().contains(submissionId));
    }

    @Test
    @DisplayName("It does not affect the caller when SMTP is unavailable")
    void shouldThrowRuntimeExceptionWhenMailSendingFails() {

        MailSendException exception =
                new MailSendException("SMTP unavailable");

        doThrow(exception).when(mailSender).send(any(SimpleMailMessage.class));

        service.notify(null,null, null);

        verify(mailSender).send(any(SimpleMailMessage.class));
    }
}
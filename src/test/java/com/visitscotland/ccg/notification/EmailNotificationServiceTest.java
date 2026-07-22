package com.visitscotland.ccg.notification;

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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailNotificationServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Captor
    private ArgumentCaptor<SimpleMailMessage> messageCaptor;

    private EmailNotificationService service;

    @BeforeEach
    void setUp() {
        service = new EmailNotificationService(mailSender);
    }

    @Test
    @DisplayName("Send email")
    void shouldSendEmail() {

        String[] recipients = {
                "mail-whatcher-1@visitscotland.com",
                "mail-whatcher-2@visitscotland.com"
        };

        String subject = "Test Subject";
        String body = "This is a test email.";

        service.notify(body, subject, recipients);

        verify(mailSender).send(messageCaptor.capture());

        SimpleMailMessage message = messageCaptor.getValue();

        assertArrayEquals(recipients, message.getTo());
        assertEquals(subject, message.getSubject());
        assertEquals(body, message.getText());
    }

    @Test
    @DisplayName("It does not affect the caller when SMTP is unavailable")
    void shouldThrowRuntimeExceptionWhenMailSendingFails() {

        MailSendException exception =
                new MailSendException("SMTP unavailable");

        doThrow(exception).when(mailSender).send(any(SimpleMailMessage.class));

        service.notify("Body","Subject", new String[]{"test@example.com"});

        verify(mailSender).send(any(SimpleMailMessage.class));
    }
}
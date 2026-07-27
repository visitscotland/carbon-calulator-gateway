package com.visitscotland.ccg.notification;

import com.visitscotland.ccg.config.EmailProperties;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Disabled()
class EmailNotificationServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Captor
    private ArgumentCaptor<Map<String, String>> variablesCaptor;

    private EmailNotificationService service;
    private EmailProperties properties;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private EmailComposer composer;

    @BeforeEach
    void setUp(){
        properties = new EmailProperties();
        properties.setEnabled(true);
        service = new EmailNotificationService(properties, mailSender, composer);
    }

    @Test
    @DisplayName("An email is sent with the submission ID")
    void shouldSendEmail() throws IOException, MessagingException {
        String[] recipients = new String[]{
                "mail-whatcher-1@visitscotland.com",
                "mail-whatcher-2@visitscotland.com"
        };
        String subject = "This is an email notification";
        String submissionId = UUID.randomUUID().toString();

        properties.setSubject(subject);
        properties.setRecipients(recipients);

        MimeMessage mimeMessage = new MimeMessage((Session) null);

        when(composer.compose(any(), any())).thenReturn("This is the email");
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        service.notify(null, null, submissionId);

        verify(composer).compose(any(), variablesCaptor.capture());

        Map<String, String> map = variablesCaptor.getValue();

        assertEquals(2, mimeMessage.getAllRecipients().length);
        assertEquals("This is an email notification", mimeMessage.getSubject());
        assertEquals(submissionId, map.get("submissionId"));
        assertNotNull(mimeMessage.getContent());
    }

    @Test
    @DisplayName("It does not affect the caller when SMTP is unavailable")
    void shouldThrowRuntimeExceptionWhenMailSendingFails() throws IOException {

        when(mailSender.createMimeMessage()).thenThrow(new IOException("Template not found"));

        service.notify(null,null, null);

        verify(composer).compose(anyString(), anyMap());
    }
}
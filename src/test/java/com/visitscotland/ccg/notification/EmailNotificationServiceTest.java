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

import javax.security.auth.Subject;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Disabled()
class EmailNotificationServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Captor
    private ArgumentCaptor<Map<String, String>> variablesCaptor;
    private static final String SUBJECT = "Email Notification Service";

    private EmailNotificationService service;
    private EmailProperties properties;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private EmailComposer composer;

    @BeforeEach
    void setUp(){
        properties = new EmailProperties();
        properties.setEnabled(true);
        properties.setSubject(SUBJECT);
        properties.setRecipients(new String[]{
                "mail-whatcher-1@visitscotland.com",
                "mail-whatcher-2@visitscotland.com"
        });
        service = new EmailNotificationService(properties, mailSender, composer);
    }

    @Test
    @DisplayName("An email is sent with the submission ID and information about errors")
    void shouldSendEmail() throws IOException, MessagingException {
        String submissionId = UUID.randomUUID().toString();

        MimeMessage mimeMessage = new MimeMessage((Session) null);

        when(composer.compose(any(), any())).thenReturn("This is the email");
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        service.notify(null, null, submissionId);

        verify(composer).compose(any(), variablesCaptor.capture());

        Map<String, String> map = variablesCaptor.getValue();

        assertEquals(2, mimeMessage.getAllRecipients().length);
        assertEquals(SUBJECT, mimeMessage.getSubject());
        assertEquals(submissionId, map.get("submissionId"));
        assertTrue(map.containsKey("traceError"));
        assertTrue(map.containsKey("bregError"));
        assertNotNull(mimeMessage.getContent());
    }

    @Test
    @DisplayName("If an error happens during email composition, The caller does not get affected by it")
    void shouldThrowRuntimeExceptionWhenMailSendingFails() throws IOException {

        when(composer.compose(any(), any())).thenThrow(new IOException("Template not found"));

        service.notify(null,null, null);
    }
}
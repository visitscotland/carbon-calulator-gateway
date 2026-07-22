package com.visitscotland.ccg.client;

import com.visitscotland.ccg.config.ReCaptchaProperties;
import com.visitscotland.recaptcha.ReCaptcha;
import com.visitscotland.utils.info.NetworkUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecaptchaClientTest {

    @Mock
    private ReCaptcha reCaptcha;

    @Mock
    private NetworkUtils networkUtils;

    @Mock
    private HttpServletRequest request;

    private ReCaptchaProperties properties;

    private RecaptchaClient service;

    @BeforeEach
    void setUp() {

        properties = new ReCaptchaProperties();
        properties.setRecaptchaEnabled(true);
        properties.setSecretkey("secret");

        service = new RecaptchaClient(reCaptcha, networkUtils, properties);
    }

    @Test
    @DisplayName("Should return true when reCAPTCHA validation is disabled")
    void shouldReturnTrueWhenRecaptchaIsDisabled() {
        properties.setRecaptchaEnabled(false);

        assertTrue(service.isValidRecaptcha(request, "captcha"));
        verifyNoInteractions(reCaptcha);
    }

    @Test
    @DisplayName("Should return true when the reCAPTCHA service validates the response")
    void shouldReturnTrueWhenCaptchaIsValid() throws Exception {
        when(reCaptcha.isValid(any(), any(),any())).thenReturn(true);

        assertTrue(service.isValidRecaptcha(request,"captcha-response"));
    }

    @Test
    @DisplayName("Should return false when the reCAPTCHA service rejects the response")
    void shouldReturnFalseWhenCaptchaIsInvalid() throws Exception {
        when(reCaptcha.isValid(any(), any(), any())).thenReturn(false);

        assertFalse(service.isValidRecaptcha(
                request,
                "captcha-response"));
    }

    @Test
    @DisplayName("Should return false when the captcha response is empty")
    void shouldReturnFalseWhenCaptchaResponseIsEmpty() {
        assertFalse(service.isValidRecaptcha(request, ""));

        verifyNoInteractions(reCaptcha);
    }

    @Test
    @DisplayName("Should return false when the reCAPTCHA service throws an IOException")
    void shouldReturnFalseWhenRecaptchaThrowsIOException() throws Exception {
        when(reCaptcha.isValid(any(), any(), any())).thenThrow(new IOException("Service unavailable"));

        assertFalse(service.isValidRecaptcha(request, "captcha-response"));
    }

    @Test
    @DisplayName("Should use the client IP resolved by NetworkUtils")
    void shouldUseResolvedClientIpAddress() throws Exception {

        when(request.getHeader("x-forwarded-for")).thenReturn("proxy-ip");
        when(request.getRemoteAddr()).thenReturn("remote-ip");
        when(networkUtils.getIPAddress("proxy-ip", "remote-ip")).thenReturn("client-ip");
        when(reCaptcha.isValid("client-ip","captcha-response","secret"))
                .thenReturn(true);

        assertTrue(service.isValidRecaptcha(request, "captcha-response"));
        verify(networkUtils).getIPAddress("proxy-ip", "remote-ip");
    }
}
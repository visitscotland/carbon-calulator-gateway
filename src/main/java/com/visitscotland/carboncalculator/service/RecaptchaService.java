package com.visitscotland.carboncalculator.service;

import com.visitscotland.recaptcha.ReCaptcha;
import com.visitscotland.utils.Contract;
import com.visitscotland.utils.info.NetworkUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Base64;
import java.util.Map;

@Service
public class RecaptchaService {

    private static final String HEADER_FORWARDED = "x-forwarded-for";
    public static final String RECAPTCHA_FIELD_NAME = ReCaptcha.RECAPTCHA_RESPONSE_FIELD_NAME;

    private static final Logger logger = LogManager.getLogger(RecaptchaService.class);

    @Value("${recaptcha.publickey}")
    private String publicKey;

    @Value("${recaptcha.secretkey}")
    private String secretkey;

    @Value("${recaptcha.enabled}")
    private boolean isRecaptchaEnabled;

    private final ReCaptcha reCaptcha;
    private final NetworkUtils networkUtils;

    public RecaptchaService(ReCaptcha reCaptcha, NetworkUtils networkUtils) {
        this.reCaptcha = reCaptcha;
        this.networkUtils = networkUtils;
    }

    private boolean isValidRecaptcha(Map<String, ?> formData, HttpServletRequest request, String captchaResponse) {
        String remoteAddr = networkUtils.getIPAddress(request.getHeader(HEADER_FORWARDED), request.getRemoteAddr());
        return captchaCheck(remoteAddr, captchaResponse);
    }

    public boolean captchaCheck(String remoteAddr, String captchaResponse) {
        if (!isRecaptchaEnabled) {
            logger.warn("The recaptcha validation has been disabled.");
            return true;
        } else if (!Contract.isEmpty(captchaResponse)) {
            String secretKey = this.secretkey;
            try {
                if (reCaptcha.isValid(remoteAddr, captchaResponse, secretKey)) {
                    logger.info("recaptcha success");
                    return true;
                }
            } catch (IOException e) {
                logger.info("recaptcha failure");
            }
        }
        return false;
    }

    private String generateCredentials(String publicKey, String privateKey) {
        String secret = publicKey + ":" + privateKey;
        return Base64.getEncoder().encodeToString(secret.getBytes());
    }

}
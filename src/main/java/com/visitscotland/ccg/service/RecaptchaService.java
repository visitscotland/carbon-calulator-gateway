package com.visitscotland.ccg.service;

import com.visitscotland.ccg.config.ReCaptchaProperties;
import com.visitscotland.recaptcha.ReCaptcha;
import com.visitscotland.utils.Contract;
import com.visitscotland.utils.info.NetworkUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class RecaptchaService {

    private static final String HEADER_FORWARDED = "x-forwarded-for";
    public static final String RECAPTCHA_FIELD_NAME = ReCaptcha.RECAPTCHA_RESPONSE_FIELD_NAME;

    private static final Logger logger = LogManager.getLogger(RecaptchaService.class);

    private final ReCaptcha reCaptcha;
    private final NetworkUtils networkUtils;
    private final ReCaptchaProperties properties;

    public RecaptchaService(ReCaptcha reCaptcha, NetworkUtils networkUtils, ReCaptchaProperties properties) {
        this.reCaptcha = reCaptcha;
        this.networkUtils = networkUtils;
        this.properties = properties;
    }

    public boolean isValidRecaptcha(HttpServletRequest request, String captchaResponse) {
        String remoteAddr = networkUtils.getIPAddress(request.getHeader(HEADER_FORWARDED), request.getRemoteAddr());
        return captchaCheck(remoteAddr, captchaResponse);
    }

    private boolean captchaCheck(String remoteAddr, String captchaResponse) {
        if (!properties.isRecaptchaEnabled()) {
            logger.warn("The recaptcha validation has been disabled.");
            return true;
        } else if (!Contract.isEmpty(captchaResponse)) {
            String secretKey = properties.getSecretkey();
            try {
                if (reCaptcha.isValid(remoteAddr, captchaResponse, secretKey)) {
                    logger.debug("recaptcha success");
                    return true;
                }
            } catch (IOException e) {
                logger.debug("recaptcha failure");
            }
        }
        return false;
    }
}
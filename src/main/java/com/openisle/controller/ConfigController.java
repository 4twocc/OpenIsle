package com.openisle.controller;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ConfigController {

    @Value("${app.captcha.enabled:false}")
    private boolean captchaEnabled;

    @Value("${app.captcha.register-enabled:false}")
    private boolean registerCaptchaEnabled;

    @Value("${app.captcha.login-enabled:false}")
    private boolean loginCaptchaEnabled;

    @Value("${app.captcha.post-enabled:false}")
    private boolean postCaptchaEnabled;

    @Value("${app.captcha.comment-enabled:false}")
    private boolean commentCaptchaEnabled;

    @GetMapping("/config")
    public ConfigResponse getConfig() {
        ConfigResponse resp = new ConfigResponse();
        resp.setCaptchaEnabled(captchaEnabled);
        resp.setRegisterCaptchaEnabled(registerCaptchaEnabled);
        resp.setLoginCaptchaEnabled(loginCaptchaEnabled);
        resp.setPostCaptchaEnabled(postCaptchaEnabled);
        resp.setCommentCaptchaEnabled(commentCaptchaEnabled);
        return resp;
    }

    @Data
    private static class ConfigResponse {
        private boolean captchaEnabled;
        private boolean registerCaptchaEnabled;
        private boolean loginCaptchaEnabled;
        private boolean postCaptchaEnabled;
        private boolean commentCaptchaEnabled;
    }
}

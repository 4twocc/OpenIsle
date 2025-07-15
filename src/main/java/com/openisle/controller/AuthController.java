package com.openisle.controller;

import com.openisle.model.User;
import com.openisle.service.EmailSender;
import com.openisle.service.JwtService;
import com.openisle.service.UserService;
import com.openisle.service.CaptchaService;
import com.openisle.service.GoogleAuthService;
import com.openisle.service.RegisterModeService;
import com.openisle.service.NotificationService;
import com.openisle.model.RegisterMode;
import com.openisle.repository.UserRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Value;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;
    private final JwtService jwtService;
    private final EmailSender emailService;
    private final CaptchaService captchaService;
    private final GoogleAuthService googleAuthService;
    private final RegisterModeService registerModeService;
    private final NotificationService notificationService;
    private final UserRepository userRepository;

    @Value("${app.captcha.enabled:false}")
    private boolean captchaEnabled;

    @Value("${app.captcha.register-enabled:false}")
    private boolean registerCaptchaEnabled;

    @Value("${app.captcha.login-enabled:false}")
    private boolean loginCaptchaEnabled;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
        if (captchaEnabled && registerCaptchaEnabled && !captchaService.verify(req.getCaptcha())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid captcha"));
        }
        User user = userService.register(
                req.getUsername(), req.getEmail(), req.getPassword(), req.getReason(), registerModeService.getRegisterMode());
        emailService.sendEmail(user.getEmail(), "Verification Code", "Your verification code is " + user.getVerificationCode());
        if (!user.isApproved()) {
            notificationService.createRegisterRequestNotifications(user, user.getRegisterReason());
        }
        return ResponseEntity.ok(Map.of("message", "Verification code sent"));
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verify(@RequestBody VerifyRequest req) {
        boolean ok = userService.verifyCode(req.getUsername(), req.getCode());
        if (ok) {
            return ResponseEntity.ok(Map.of("message", "Verified"));
        }
        return ResponseEntity.badRequest().body(Map.of("error", "Invalid verification code"));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        if (captchaEnabled && loginCaptchaEnabled && !captchaService.verify(req.getCaptcha())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid captcha"));
        }
        Optional<User> userOpt = userService.findByUsername(req.getUsername());
        if (userOpt.isEmpty() || !userService.matchesPassword(userOpt.get(), req.getPassword())) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Invalid credentials",
                    "reason_code", "INVALID_CREDENTIALS"));
        }
        User user = userOpt.get();
        if (!user.isVerified()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "User not verified",
                    "reason_code", "NOT_VERIFIED"));
        }
        if (!user.isApproved()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Register reason not approved",
                    "reason_code", "NOT_APPROVED"));
        }
        return ResponseEntity.ok(Map.of("token", jwtService.generateToken(user.getUsername())));
    }

    @PostMapping("/google")
    public ResponseEntity<?> loginWithGoogle(@RequestBody GoogleLoginRequest req) {
        Optional<User> user = googleAuthService.authenticate(req.getIdToken(), req.getReason(), registerModeService.getRegisterMode());
        if (user.isPresent()) {
            if (!user.get().isApproved()) {
                if (user.get().getRegisterReason() != null && !user.get().getRegisterReason().isEmpty()) {
                    // do not send empty notifition (while try login)
                    notificationService.createRegisterRequestNotifications(user.get(), req.getReason());

                    return ResponseEntity.badRequest().body(Map.of(
                            "error", "Account awaiting approval",
                            "reason_code", "IS_APPROVING"
                    ));
                }
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "Account awaiting approval",
                        "reason_code", "NOT_APPROVED"
                ));
            }
            return ResponseEntity.ok(Map.of("token", jwtService.generateToken(user.get().getUsername())));
        }
        return ResponseEntity.badRequest().body(Map.of(
                "error", "Invalid google token",
                "reason_code", "INVALID_CREDENTIALS"
        ));
    }

    @GetMapping("/check")
    public ResponseEntity<?> checkToken() {
        return ResponseEntity.ok(Map.of("valid", true));
    }

    @Data
    private static class RegisterRequest {
        private String username;
        private String email;
        private String password;
        private String captcha;
        private String reason;
    }

    @Data
    private static class LoginRequest {
        private String username;
        private String password;
        private String captcha;
    }

    @Data
    private static class GoogleLoginRequest {
        private String idToken;
        private String reason;
    }

    @Data
    private static class VerifyRequest {
        private String username;
        private String code;
    }
}

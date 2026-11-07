
package com.ecommerce.project.email;

import com.ecommerce.project.service.EmailVerificationService;
import com.ecommerce.project.service.RateLimitService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class EmailVerificationController {

    private final EmailVerificationService emailVerificationService;
    private final RateLimitService rateLimitService;

    public EmailVerificationController(
            EmailVerificationService emailVerificationService, RateLimitService rateLimitService) {
        this.emailVerificationService = emailVerificationService;
        this.rateLimitService = rateLimitService;
    }

    @PostMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(
            @RequestBody Map<String, String> request) {

        String token = request.get("token");

        try {
            emailVerificationService.verifyEmail(token);

            return ResponseEntity.ok(
                    Map.of("message", "Email je uspješno verificiran!")
            );

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    Map.of("message", e.getMessage())
            );
        }
    }



    @PostMapping("/resend-verification")
    public ResponseEntity<?> resendVerification(
            @RequestBody Map<String, String> request,
            HttpServletRequest httpRequest) {

        String ipAddress = httpRequest.getRemoteAddr();

        if (!rateLimitService.allowRequest(ipAddress)) {
            return ResponseEntity
                    .status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Map.of(
                            "message",
                            "Previše zahtjeva. Pokušajte ponovno kasnije."
                    ));
        }

        String email = request.get("email");

        emailVerificationService.resendVerificationEmail(email);

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "Ako je e-mail adresa registrirana i nije potvrđena, poslat ćemo vam novi verifikacijski link."
                )
        );
    }


}

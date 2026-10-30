package com.ecommerce.project.email;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class EmailTestController {

    private final EmailService emailService;

    public EmailTestController(EmailService emailService) {
        this.emailService = emailService;
    }

    @GetMapping("/email")
    public ResponseEntity<String> testEmail() {

        emailService.sendOrderConfirmation(
                "stjepan246" + "@gmail.com",
                123L,
                30.00
        );

        return ResponseEntity.ok("Email uspješno poslan!");
    }
}

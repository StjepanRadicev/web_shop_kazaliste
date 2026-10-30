
package com.ecommerce.project.email;

import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendOrderConfirmation(
            String customerEmail,
            Long orderId,
            Double totalAmount) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(customerEmail);
        message.setSubject("Potvrda kupnje ulaznica - Narudžba #" + orderId);

        message.setText(
                "Poštovani,\n\n" +
                        "Vaša kupnja ulaznica uspješno je završena.\n\n" +
                        "Broj narudžbe: " + orderId + "\n" +
                        "Ukupan iznos: " +
                        String.format(java.util.Locale.forLanguageTag("hr-HR"),
                                "%.2f", totalAmount) + " EUR\n\n" +
                        "Status plaćanja: Plaćeno\n\n" +
                        "Hvala na kupnji!"
        );

        mailSender.send(message);
    }
}





package com.ecommerce.project.email;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class EmailService {
    // Ostatak tvog postojećeg koda


    private final RestClient restClient;
    private final String senderEmail;

    public EmailService(
            @Value("${brevo.api.key}") String apiKey,
            @Value("${brevo.sender.email}") String senderEmail) {

        this.senderEmail = senderEmail;

        this.restClient = RestClient.builder()
                .baseUrl("https://api.brevo.com/v3")
                .defaultHeader("api-key", apiKey)
                .build();
    }

    public void sendOrderConfirmation(
            String customerEmail,
            Long orderId,
            Double totalAmount) {

        String subject = "Potvrda kupnje ulaznica - Narudžba #" + orderId;

        String content =
                "Poštovani,\n\n" +
                        "Vaša kupnja ulaznica uspješno je završena.\n\n" +
                        "Broj narudžbe: " + orderId + "\n" +
                        "Ukupan iznos: " +
                        String.format(Locale.forLanguageTag("hr-HR"),
                                "%.2f", totalAmount) + " EUR\n\n" +
                        "Status plaćanja: Plaćeno\n\n" +
                        "Hvala na kupnji!";

        Map<String, Object> request = Map.of(
                "sender", Map.of(
                        "name", "Kazalište",
                        "email", senderEmail
                ),
                "to", List.of(
                        Map.of("email", customerEmail)
                ),
                "subject", subject,
                "textContent", content
        );

        restClient.post()
                .uri("/smtp/email")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .toBodilessEntity();
    }
}
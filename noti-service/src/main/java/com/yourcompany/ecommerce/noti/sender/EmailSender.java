package com.yourcompany.ecommerce.noti.sender;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;


import java.util.Map;


@Component
public class EmailSender implements NotificationSender {


    private final WebClient client;


    public EmailSender(@Value("${app.providers.email.url}") String providerUrl) {
        this.client = WebClient.create(providerUrl);
    }


    @Override
    public void send(String to, String subject, String body, Map<String, Object> meta) throws Exception {
// In this sample we do a simple POST to a mock provider. In production use SDKs.
        client.post()
                .bodyValue(new EmailRequest(to, subject, body))
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }


    public static class EmailRequest {
        public String to;
        public String subject;
        public String body;
        public EmailRequest(String to, String subject, String body) { this.to = to; this.subject = subject; this.body = body; }
    }
}
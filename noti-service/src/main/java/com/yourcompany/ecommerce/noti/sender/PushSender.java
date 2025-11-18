package com.yourcompany.ecommerce.noti.sender;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;


import java.util.Map;


@Component
public class PushSender implements NotificationSender {


    private final WebClient client;


    public PushSender(@Value("${app.providers.push.url}") String providerUrl) {
        this.client = WebClient.create(providerUrl);
    }


    @Override
    public void send(String to, String subject, String body, Map<String, Object> meta) throws Exception {
        client.post().bodyValue(new PushRequest(to, subject, body)).retrieve().bodyToMono(String.class).block();
    }


    public static class PushRequest { public String to; public String title; public String message; public PushRequest(String to, String t, String m) { this.to = to; this.title = t; this.message = m; } }
}
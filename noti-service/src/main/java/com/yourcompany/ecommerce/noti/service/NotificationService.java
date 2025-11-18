package com.yourcompany.ecommerce.noti.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yourcompany.ecommerce.noti.sender.EmailSender;
import com.yourcompany.ecommerce.noti.sender.NotificationSender;
import com.yourcompany.ecommerce.noti.sender.PushSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.Duration;
import java.util.HashMap;
import java.util.Map;


@Service
public class NotificationService {


    private final IdempotencyService idempotency;
    private final TemplateService templateService;
    private final NotificationSender emailSender;
    private final NotificationSender pushSender;
    private final ObjectMapper mapper = new ObjectMapper();


    public NotificationService(IdempotencyService idempotency, TemplateService templateService, EmailSender emailSender, PushSender pushSender) {
        this.idempotency = idempotency;
        this.templateService = templateService;
        this.emailSender = emailSender;
        this.pushSender = pushSender;
    }


    /**
     * Process an incoming event JSON. Determine recipient, render template, send notifications.
     */
    @Transactional
    public void processEvent(String eventJson) throws Exception {
        JsonNode root = mapper.readTree(eventJson);
        String eventId = root.path("eventId").asText(null);
        String type = root.path("type").asText("unknown");
        JsonNode payload = root.path("payload");


        if (eventId == null) {
// generate synthetic id (not recommended) but keep safe
            eventId = "synthetic-" + System.currentTimeMillis();
        }


// idempotency
        if (idempotency.isProcessed(eventId)) {
            return; // already processed
        }


// choose recipients (simplified): payload.userEmail, payload.userId, payload.phone
        String userEmail = payload.path("userEmail").asText(null);
        String userId = payload.path("userId").asText(null);


        Map<String, Object> model = new HashMap<>();
        model.put("orderId", payload.path("orderId").asText(""));
        model.put("amount", payload.path("amount").asText(""));
        model.put("currency", payload.path("currency").asText(""));
        model.put("tracking", payload.path("tracking").asText(""));
        model.put("eventType", type);


        String template = templateService.getTemplateForEvent(type);
        String body = templateService.render(template, model);
        String subject = "Notification: " + type;


// send email if email exists
        if (userEmail != null && !userEmail.isEmpty()) {
            emailSender.send(userEmail, subject, body, model);
        }


// send push if userId exists
        if (userId != null && !userId.isEmpty()) {
            pushSender.send(userId, subject, body, model);
        }


// mark processed
        idempotency.markProcessed(eventId, Duration.ofDays(7));
    }
}

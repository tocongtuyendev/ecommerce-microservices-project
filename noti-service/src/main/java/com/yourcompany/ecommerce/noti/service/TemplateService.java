package com.yourcompany.ecommerce.noti.service;

import org.springframework.stereotype.Service;


import java.util.HashMap;
import java.util.Map;


@Service
public class TemplateService {


    // Very simple templating - replace {{key}} with values
    public String render(String template, Map<String, Object> model) {
        String out = template;
        for (Map.Entry<String, Object> e : model.entrySet()) {
            out = out.replace("{{" + e.getKey() + "}}", String.valueOf(e.getValue()));
        }
        return out;
    }


    public String getTemplateForEvent(String eventType) {
        switch (eventType) {
            case "payment.succeeded":
                return "Your payment for order {{orderId}} succeeded. Amount: {{amount}} {{currency}}.";
            case "payment.failed":
                return "Your payment for order {{orderId}} failed. Please try again.";
            case "order.created":
                return "Order {{orderId}} created. We'll notify when it ships.";
            case "shipment.sent":
                return "Your order {{orderId}} has been shipped. Tracking: {{tracking}}.";
            default:
                return "Notification: event {{eventType}} for {{orderId}}";
        }
    }
}
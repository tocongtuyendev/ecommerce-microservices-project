package com.yourcompany.ecommerce.noti.sender;

import java.util.Map;


public interface NotificationSender {
    void send(String to, String subject, String body, Map<String, Object> meta) throws Exception;
}
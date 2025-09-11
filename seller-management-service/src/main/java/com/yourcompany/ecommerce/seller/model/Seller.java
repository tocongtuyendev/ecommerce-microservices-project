package com.yourcompany.ecommerce.seller.model;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "sellers")
@Data
public class Seller {
    @Id
    private String id;
    private Long userId; // Liên kết với User trong identity-service
    private String username;
    private String shopName;
    private String status; // Ví dụ: PENDING_APPROVAL, ACTIVE, SUSPENDED
    private String reasonForRejection;

    @CreatedDate // Tự động điền thời gian tạo
    private Instant createdAt;
}
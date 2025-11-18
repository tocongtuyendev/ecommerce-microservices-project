package com.yourcompany.ecommerce.seller.dto;

import lombok.Data;
import java.time.Instant;

@Data
public class SellerResponse {
    private String id;
    private Long userId;
    private String username;
    private String shopName;
    private String status;
    private Instant createdAt;
}
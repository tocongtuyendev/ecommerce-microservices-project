package com.yourcompany.ecommerce.seller.dto;

import lombok.Data;

@Data
public class SellerApprovalRequest {
    private String status; // Sẽ là "ACTIVE" hoặc "REJECTED"
    private String reason; // Lý do từ chối (tùy chọn)
}
package com.yourcompany.ecommerce.common.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SellerProfileCreateEvent implements Serializable {
    private Long userId;
    private String username;
}
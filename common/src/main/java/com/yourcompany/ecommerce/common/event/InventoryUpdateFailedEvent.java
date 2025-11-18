package com.yourcompany.ecommerce.common.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InventoryUpdateFailedEvent implements Serializable {
    private String orderNumber;
    private String reason;
}
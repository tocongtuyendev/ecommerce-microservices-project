package com.yourcompany.ecommerce.payment.dto;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class CreatePaymentRequest {

    @NotBlank
    private String orderId;

    @NotNull
    @Min(1)
    private Long amount; // in cents

    @NotBlank
    private String currency;

    @NotBlank
    private String returnUrl; // where to redirect user after payment

}
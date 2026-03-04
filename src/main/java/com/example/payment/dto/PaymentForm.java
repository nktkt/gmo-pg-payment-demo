package com.example.payment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class PaymentForm {

    @NotNull
    private Long productId;

    @NotBlank
    private String token;

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
}

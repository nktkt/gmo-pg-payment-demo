package com.example.payment.dto;

public record AlterTranResponse(
        String accessId,
        String accessPass,
        String forward,
        String approve,
        String tranId,
        String tranDate
) {
}

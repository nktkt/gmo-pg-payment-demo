package com.example.payment.dto;

public record ExecTranResponse(
        String acs,
        String orderId,
        String forward,
        String method,
        String payTimes,
        String approve,
        String tranId,
        String tranDate,
        String checkString
) {
}

package com.example.payment.exception;

public class GmoPgApiException extends RuntimeException {

    private final String errCode;
    private final String errInfo;

    public GmoPgApiException(String errCode, String errInfo) {
        super("GMO-PG API Error: ErrCode=%s, ErrInfo=%s".formatted(errCode, errInfo));
        this.errCode = errCode;
        this.errInfo = errInfo;
    }

    public String getErrCode() { return errCode; }
    public String getErrInfo() { return errInfo; }
}

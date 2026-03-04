package com.example.payment.service;

import com.example.payment.config.GmoPgProperties;
import com.example.payment.dto.AlterTranResponse;
import com.example.payment.dto.EntryTranResponse;
import com.example.payment.dto.ExecTranResponse;
import com.example.payment.exception.GmoPgApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class GmoPgApiClientTest {

    private GmoPgApiClient client;
    private MockRestServiceServer mockServer;

    @BeforeEach
    void setUp() {
        RestTemplate restTemplate = new RestTemplate();
        mockServer = MockRestServiceServer.createServer(restTemplate);

        GmoPgProperties properties = new GmoPgProperties(
                "tsite00000000",
                "tsitepass0000",
                "tshop00000000",
                "tshoppass0000",
                "https://pt01.mul-pay.jp/payment/",
                "https://stg.static.mul-pay.jp/ext/js/token.js"
        );

        client = new GmoPgApiClient(restTemplate, properties);
    }

    @Test
    void entryTran_success() {
        mockServer.expect(requestTo("https://pt01.mul-pay.jp/payment/EntryTran.idPass"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(
                        "AccessID=test_access_id&AccessPass=test_access_pass",
                        MediaType.TEXT_PLAIN
                ));

        EntryTranResponse response = client.entryTran("ORDER001", "AUTH", 1000);

        assertEquals("test_access_id", response.accessId());
        assertEquals("test_access_pass", response.accessPass());
        mockServer.verify();
    }

    @Test
    void entryTran_error() {
        mockServer.expect(requestTo("https://pt01.mul-pay.jp/payment/EntryTran.idPass"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(
                        "ErrCode=E01&ErrInfo=E01010001",
                        MediaType.TEXT_PLAIN
                ));

        GmoPgApiException exception = assertThrows(GmoPgApiException.class,
                () -> client.entryTran("ORDER001", "AUTH", 1000));

        assertEquals("E01", exception.getErrCode());
        assertEquals("E01010001", exception.getErrInfo());
        mockServer.verify();
    }

    @Test
    void execTran_success() {
        mockServer.expect(requestTo("https://pt01.mul-pay.jp/payment/ExecTran.idPass"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(
                        "ACS=0&OrderID=ORDER001&Forward=2a99662&Method=1&PayTimes=&Approve=0123456&TranID=tid001&TranDate=20240101120000&CheckString=abc123",
                        MediaType.TEXT_PLAIN
                ));

        ExecTranResponse response = client.execTran("access_id", "access_pass", "ORDER001", "test_token");

        assertEquals("0", response.acs());
        assertEquals("ORDER001", response.orderId());
        assertEquals("0123456", response.approve());
        assertEquals("tid001", response.tranId());
        mockServer.verify();
    }

    @Test
    void execTran_error() {
        mockServer.expect(requestTo("https://pt01.mul-pay.jp/payment/ExecTran.idPass"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(
                        "ErrCode=G02&ErrInfo=42G020000",
                        MediaType.TEXT_PLAIN
                ));

        GmoPgApiException exception = assertThrows(GmoPgApiException.class,
                () -> client.execTran("access_id", "access_pass", "ORDER001", "bad_token"));

        assertEquals("G02", exception.getErrCode());
        mockServer.verify();
    }

    @Test
    void alterTran_success() {
        mockServer.expect(requestTo("https://pt01.mul-pay.jp/payment/AlterTran.idPass"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(
                        "AccessID=access_id&AccessPass=access_pass&Forward=2a99662&Approve=0123456&TranID=tid002&TranDate=20240101130000",
                        MediaType.TEXT_PLAIN
                ));

        AlterTranResponse response = client.alterTran("access_id", "access_pass", "SALES", 1000);

        assertEquals("access_id", response.accessId());
        assertEquals("0123456", response.approve());
        assertEquals("tid002", response.tranId());
        mockServer.verify();
    }

    @Test
    void alterTran_error() {
        mockServer.expect(requestTo("https://pt01.mul-pay.jp/payment/AlterTran.idPass"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(
                        "ErrCode=M01&ErrInfo=M01004002",
                        MediaType.TEXT_PLAIN
                ));

        GmoPgApiException exception = assertThrows(GmoPgApiException.class,
                () -> client.alterTran("access_id", "access_pass", "SALES", 1000));

        assertEquals("M01", exception.getErrCode());
        mockServer.verify();
    }
}

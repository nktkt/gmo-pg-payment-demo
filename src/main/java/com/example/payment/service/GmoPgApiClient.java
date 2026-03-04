package com.example.payment.service;

import com.example.payment.config.GmoPgProperties;
import com.example.payment.dto.AlterTranResponse;
import com.example.payment.dto.EntryTranResponse;
import com.example.payment.dto.ExecTranResponse;
import com.example.payment.exception.GmoPgApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class GmoPgApiClient {

    private static final Logger log = LoggerFactory.getLogger(GmoPgApiClient.class);

    private final RestTemplate restTemplate;
    private final GmoPgProperties properties;

    public GmoPgApiClient(RestTemplate restTemplate, GmoPgProperties properties) {
        this.restTemplate = restTemplate;
        this.properties = properties;
    }

    public EntryTranResponse entryTran(String orderId, String jobCd, int amount) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("ShopID", properties.shopId());
        params.add("ShopPass", properties.shopPass());
        params.add("OrderID", orderId);
        params.add("JobCd", jobCd);
        params.add("Amount", String.valueOf(amount));

        Map<String, String> result = postToGmoPg("EntryTran.idPass", params);

        return new EntryTranResponse(
                result.get("AccessID"),
                result.get("AccessPass")
        );
    }

    public ExecTranResponse execTran(String accessId, String accessPass, String orderId, String token) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("AccessID", accessId);
        params.add("AccessPass", accessPass);
        params.add("OrderID", orderId);
        params.add("Method", "1");
        params.add("TokenType", "1");
        params.add("Token", token);

        Map<String, String> result = postToGmoPg("ExecTran.idPass", params);

        return new ExecTranResponse(
                result.getOrDefault("ACS", ""),
                result.getOrDefault("OrderID", ""),
                result.getOrDefault("Forward", ""),
                result.getOrDefault("Method", ""),
                result.getOrDefault("PayTimes", ""),
                result.getOrDefault("Approve", ""),
                result.getOrDefault("TranID", ""),
                result.getOrDefault("TranDate", ""),
                result.getOrDefault("CheckString", "")
        );
    }

    public AlterTranResponse alterTran(String accessId, String accessPass, String jobCd, Integer amount) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("ShopID", properties.shopId());
        params.add("ShopPass", properties.shopPass());
        params.add("AccessID", accessId);
        params.add("AccessPass", accessPass);
        params.add("JobCd", jobCd);
        if (amount != null && !"VOID".equals(jobCd)) {
            params.add("Amount", String.valueOf(amount));
        }

        Map<String, String> result = postToGmoPg("AlterTran.idPass", params);

        return new AlterTranResponse(
                result.getOrDefault("AccessID", ""),
                result.getOrDefault("AccessPass", ""),
                result.getOrDefault("Forward", ""),
                result.getOrDefault("Approve", ""),
                result.getOrDefault("TranID", ""),
                result.getOrDefault("TranDate", "")
        );
    }

    private Map<String, String> postToGmoPg(String endpoint, MultiValueMap<String, String> params) {
        String url = properties.apiBaseUrl() + endpoint;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        String responseBody = restTemplate.postForObject(url, request, String.class);
        log.debug("GMO-PG Response Body: {}", responseBody);

        Map<String, String> result = parseResponse(responseBody);

        if (result.containsKey("ErrCode")) {
            throw new GmoPgApiException(
                    result.getOrDefault("ErrCode", "UNKNOWN"),
                    result.getOrDefault("ErrInfo", "UNKNOWN")
            );
        }

        return result;
    }

    private Map<String, String> parseResponse(String responseBody) {
        if (responseBody == null || responseBody.isBlank()) {
            throw new GmoPgApiException("EMPTY_RESPONSE", "Response body is empty");
        }
        return Arrays.stream(responseBody.split("&"))
                .map(pair -> pair.split("=", 2))
                .filter(parts -> parts.length == 2)
                .collect(Collectors.toMap(
                        parts -> URLDecoder.decode(parts[0], StandardCharsets.UTF_8),
                        parts -> URLDecoder.decode(parts[1], StandardCharsets.UTF_8),
                        (v1, v2) -> v1
                ));
    }
}

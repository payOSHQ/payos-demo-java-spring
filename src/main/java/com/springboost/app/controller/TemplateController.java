package com.springboost.app.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.springboost.app.type.Body;
import com.springboost.app.type.Payment;
import com.springboost.app.utils.Utils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import jakarta.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Controller
public class TemplateController {
    @Value("${PAYOS_CREATE_PAYMENT_LINK_URL}")
    private String createPaymentLinkUrl;

    @Value("${PAYOS_CLIENT_ID}")
    private String clientId;

    @Value("${PAYOS_API_KEY}")
    private String apiKey;

    @Value("${PAYOS_CHECKSUM_KEY}")
    private String checksumKey;

    @RequestMapping(value = "/")
    public String Demo() {
        return "demo";
    }
    @RequestMapping(value = "/result")
    public String Result() {
        return "result";
    }

    @RequestMapping(method = RequestMethod.POST, value = "/checkout", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public void checkout(Payment payment, HttpServletResponse httpServletResponse) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            final String productName = payment.getProductName().toString();
            final String description = payment.getDescription().toString();
            final String returnUrl = payment.getReturnUrl().toString();
            final String cancelUrl = payment.getCancelUrl().toString();
            final int price = payment.getPrice();
            // Gen order code
            String currentTimeString = String.valueOf(new Date().getTime());
            int orderCode = Integer.parseInt(currentTimeString.substring(currentTimeString.length() - 6));
            ObjectNode item = objectMapper.createObjectNode();
            item.put("name", productName);
            item.put("quantity", 1);
            item.put("price", price);

            List<ObjectNode> items = List.of(item);
            Body body = new Body(orderCode, price, description, items, cancelUrl, returnUrl);
            String bodyToSignature = Utils.createSignatureOfPaymentRequest(body, checksumKey);
            body.setSignature(bodyToSignature);
            // Tạo header
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-client-id", clientId);
            headers.set("x-api-key", apiKey);
            // Gửi yêu cầu POST
            WebClient client = WebClient.create();
            Mono<String> response = client.post()
                    .uri(createPaymentLinkUrl)
                    .headers(httpHeaders -> httpHeaders.putAll(headers))
                    .body(BodyInserters.fromValue(body))
                    .retrieve()
                    .bodyToMono(String.class);

            String responseBody = response.block();
            JsonNode res = objectMapper.readTree(responseBody);
            if (!Objects.equals(res.get("code").asText(), "00")) {
                throw new Exception("Fail");
            }
            String checkoutUrl = res.get("data").get("checkoutUrl").asText();

            // Kiểm tra dữ liệu có đúng không
            String paymentLinkResSignature = Utils.createSignatureFromObj(res.get("data"), checksumKey);
            System.out.println(paymentLinkResSignature);
            if (!paymentLinkResSignature.equals(res.get("signature").asText())) {
                throw new Exception("Signature is not compatible");
            }
            httpServletResponse.setHeader("Location", checkoutUrl);
            httpServletResponse.setStatus(302);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
package com.springboost.app.controller;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.springboost.app.model.Order;
import com.springboost.app.service.OrderService;
import com.springboost.app.type.Body;
import com.springboost.app.type.Payment;
import com.springboost.app.utils.Utils;

import java.util.Date;
import java.util.List;
import java.util.Objects;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Value;
import reactor.core.publisher.Mono;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.BodyInserters;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/order")
public class OrderController {

    @Value("${PAYOS_CREATE_PAYMENT_LINK_URL}")
    private String createPaymentLinkUrl;

    @Value("${PAYOS_CLIENT_ID}")
    private String clientId;

    @Value("${PAYOS_API_KEY}")
    private String apiKey;

    @Value("${PAYOS_CHECKSUM_KEY}")
    private String checksumKey;
    private OrderService orderService;

    public OrderController(OrderService orderService) {
        super();
        this.orderService = orderService;
    }

    @PostMapping(path = "/create")
    public ObjectNode createPaymentLink(@RequestBody Payment payment) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            final String productName = payment.getProductName();
            final String description = payment.getDescription();
            final String returnUrl = payment.getReturnUrl();
            final String cancelUrl = payment.getCancelUrl();
            final int price = payment.getPrice();
            //Gen order code
            String currentTimeString = String.valueOf(String.valueOf(new Date().getTime()));
            int orderCode =
                    Integer.parseInt(currentTimeString.substring(currentTimeString.length() - 6));
            ObjectNode item = objectMapper.createObjectNode();
            item.put("name", productName);
            item.put("quantity", 1);
            item.put("price", price);

            List<ObjectNode> items = List.of(item);
            Body body = new Body(orderCode, price, description, items,
                    cancelUrl,
                    returnUrl);
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
            System.out.println(res);
            if (!Objects.equals(res.get("code").asText(), "00")) {
                throw new Exception("Fail");
            }
            String checkoutUrl = res.get("data").get("checkoutUrl").asText();

            //Kiểm tra dữ liệu có đúng không
            String paymentLinkResSignature = Utils.createSignatureFromObj(res.get("data"), checksumKey);
            System.out.println(paymentLinkResSignature);
            if (!paymentLinkResSignature.equals(res.get("signature").asText())) {
                throw new Exception("Signature is not compatible");
            }
            //Thêm dữ liệu đơn hàng vào database
            orderService.CreateOrder(
                    new Order(orderCode, objectMapper.writeValueAsString(items), body.getAmount(),
                            description, res.get("data").get("paymentLinkId").asText()));

            ObjectNode respon = objectMapper.createObjectNode();
            respon.put("error", 0);
            respon.put("message", "success");
            respon.set("data", objectMapper.createObjectNode().put("checkoutUrl", checkoutUrl));
            return respon;

        } catch (Exception e) {
            e.printStackTrace();
            ObjectNode respon = objectMapper.createObjectNode();
            respon.put("error", -1);
            respon.put("message", "fail");
            respon.set("data", null);
            return respon;

        }
    }

    private String formatterDateTimeFromArray(JsonNode dateTimeArray) {
        int year = dateTimeArray.get(0).asInt();
        int month = dateTimeArray.get(1).asInt();
        int day = dateTimeArray.get(2).asInt();
        int hour = dateTimeArray.get(3).asInt();
        int minute = dateTimeArray.get(4).asInt();
        int second = dateTimeArray.get(5).asInt();

        return String.format("%04d-%02d-%02d %02d:%02d:%02d", year, month, day, hour, minute, second);
    }

    @GetMapping(path = "/{orderId}")
    public ObjectNode getOrderById(@PathVariable("orderId") int orderId) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        try {
            Order order = orderService.getOrder(orderId);
            System.out.println(order);
            ObjectNode respon = objectMapper.createObjectNode();
            respon.put("error", 0);
            respon.put("message", "ok");
            //Format dữ liệu từ dạng String thành Json và List int thành date Time
            ObjectNode orderJson = objectMapper.valueToTree(order);
            orderJson.set("items", objectMapper.readTree(orderJson.get("items").asText()));
            orderJson.put("created_at", formatterDateTimeFromArray(orderJson.get("created_at")));
            orderJson.put("updated_at", formatterDateTimeFromArray(orderJson.get("updated_at")));
            if (!orderJson.get("transaction_when").isNull()) {
                orderJson.put("transaction_when", formatterDateTimeFromArray(orderJson.get("transaction_when")));
            }
            orderJson.set("webhook_snapshot", objectMapper.readTree(orderJson.get("webhook_snapshot").asText()));
            respon.set("data", orderJson);

            return respon;
        } catch (Exception e) {
            e.printStackTrace();
            ObjectNode respon = objectMapper.createObjectNode();
            respon.put("error", -1);
            respon.put("message", e.getMessage());
            respon.set("data", null);
            return respon;
        }

    }
}

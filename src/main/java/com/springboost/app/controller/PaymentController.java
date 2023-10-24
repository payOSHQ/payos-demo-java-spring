package com.springboost.app.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.springboost.app.service.OrderService;
import com.springboost.app.utils.Utils;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/payment")
public class PaymentController {

  private OrderService orderService;
  @Value("${PAYOS_CHECKSUM_KEY}")
  private String checksumKey;

  public PaymentController(OrderService orderService) {
    super();
    this.orderService = orderService;
  }

  @PostMapping(path = "/payos_transfer_handler")
  public ObjectNode payosTransferHandler(@RequestBody ObjectNode body) {

    ObjectMapper objectMapper = new ObjectMapper();
    try {
      //Init Response
      ObjectNode respon = objectMapper.createObjectNode();
      respon.put("error", 0);
      respon.put("message", "Ok");

      JsonNode data = body.get("data");
      String signature = body.get("signature").asText();

      System.out.println(body);

      if (data == null) {
        throw new Exception("Không có dữ liệu");
      }
      if (signature == null) {
        throw new Exception("Không có chữ ký");
      }
      if (Objects.equals(data.get("description").asText(), "Ma giao dich thu nghiem")) {
        respon.set("data", null);
        return respon;
      }
      String signData = Utils.createSignatureFromObj(data, checksumKey);
      System.out.println(signData);
      System.out.println(signature);

      if (!signData.equals(signature)) {
        throw new Exception("Chữ ký không hợp lệ");
      }

      ObjectNode paymentData = objectMapper.createObjectNode();
      paymentData.put("refId", data.get("reference").asText());
      paymentData.put("when", data.get("transactionDateTime").asText());
      paymentData.put("code", data.get("code").asText());
      paymentData.put("webhookSnapshot", body.toString());
      respon.set("data", paymentData);
      orderService.updatePaymentForOrder(data.get("orderCode").asInt(), paymentData);
      paymentData.set("webhookSnapshot", body);
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

package com.springboot.app.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.lib.payos.PayOS;
import java.util.Objects;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/payment")
public class PaymentController {
  private final PayOS payOS;

  public PaymentController(PayOS payOS) {
    super();
    this.payOS = payOS;

  }

  @PostMapping(path = "/payos_transfer_handler")
  public ObjectNode payosTransferHandler(@RequestBody ObjectNode body) {

    ObjectMapper objectMapper = new ObjectMapper();
    ObjectNode respon = objectMapper.createObjectNode();

    try {
      //Init Response
      respon.put("error", 0);
      respon.put("message", "Ok");
      respon.set("data", null);

      JsonNode data = payOS.verifyPaymentWebhookData(body);
      System.out.println(data);
      if (Objects.equals(data.get("description").asText(), "Ma giao dich thu nghiem")){
        return respon;
      }
      return respon;
    } catch (Exception e) {
      e.printStackTrace();
      respon.put("error", -1);
      respon.put("message", e.getMessage());
      respon.set("data", null);
      return respon;
    }
  }
}

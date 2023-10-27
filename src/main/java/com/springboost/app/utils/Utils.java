package com.springboost.app.utils;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.JsonObject;
import com.springboost.app.type.Body;

import java.util.*;
import java.util.Map.Entry;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Utils {

  private static String convertObjToQueryStr(JsonNode object) {
    StringBuilder stringBuilder = new StringBuilder();
    ObjectMapper objectMapper = new ObjectMapper();

    object.fields().forEachRemaining(entry -> {
      String key = entry.getKey();
      JsonNode value = entry.getValue();
      String valueAsString = value.isTextual() ? value.asText() : value.toString();

      if (!stringBuilder.isEmpty()) {
        stringBuilder.append('&');
      }
      stringBuilder.append(key).append('=').append(valueAsString);
    });

    return stringBuilder.toString();
  }

  private static JsonNode sortObjDataByKey(JsonNode object) {
    if (!object.isObject()) {
      return object;
    }

    ObjectMapper objectMapper = new ObjectMapper();
    ObjectNode orderedObject = objectMapper.createObjectNode();

    Iterator<Entry<String, JsonNode>> fieldsIterator = object.fields();
    TreeMap<String, JsonNode> sortedMap = new TreeMap<>();

    while (fieldsIterator.hasNext()) {
      Map.Entry<String, JsonNode> field = fieldsIterator.next();
      sortedMap.put(field.getKey(), field.getValue());
    }

    sortedMap.forEach(orderedObject::set);

    return orderedObject;
  }

  private static String generateHmacSHA256(String dataStr, String key)
      throws NoSuchAlgorithmException, InvalidKeyException {
    Mac sha256Hmac = Mac.getInstance("HmacSHA256");
    SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    sha256Hmac.init(secretKey);
    byte[] hmacBytes = sha256Hmac.doFinal(dataStr.getBytes(StandardCharsets.UTF_8));

    // Chuyển byte array sang chuỗi hex
    StringBuilder hexStringBuilder = new StringBuilder();
    for (byte b : hmacBytes) {
      hexStringBuilder.append(String.format("%02x", b));
    }
    return hexStringBuilder.toString();
  }

  public static String createSignatureFromObj(JsonNode data, String key)
      throws NoSuchAlgorithmException, InvalidKeyException {
    JsonNode sortedDataByKey = sortObjDataByKey(data);
    String dataQueryStr = convertObjToQueryStr(sortedDataByKey);
    return generateHmacSHA256(dataQueryStr, key);
  }

  public static String createSignatureOfPaymentRequest(Body data, String key)
      throws NoSuchAlgorithmException, InvalidKeyException {
    int amount = data.getAmount();
    String cancelUrl = data.getCancelUrl();
    String description = data.getDescription();
    String orderCode = Integer.toString(data.getOrderCode());
    String returnUrl = data.getReturnUrl();

    String dataStr = "amount=" + amount + "&cancelUrl=" + cancelUrl + "&description=" + description
        + "&orderCode=" + orderCode + "&returnUrl=" + returnUrl;
    // Sử dụng HMAC-SHA-256 để tính toán chữ ký
    return generateHmacSHA256(dataStr, key);
  }
}

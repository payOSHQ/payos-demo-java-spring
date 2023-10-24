package com.springboost.app.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.springboost.app.model.Order;

public interface OrderService {
  void CreateOrder(Order order);
  Order getOrder(int orderId);
  void updatePaymentForOrder(int orderId, JsonNode paymentData);
}

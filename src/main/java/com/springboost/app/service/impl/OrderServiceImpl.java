package com.springboost.app.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.springboost.app.exception.ResourceNotFoundException;
import com.springboost.app.model.Order;
import com.springboost.app.repository.OrderRepository;
import com.springboost.app.service.OrderService;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Service;


@Service
public class OrderServiceImpl implements OrderService {

  private OrderRepository orderRepository;

  public OrderServiceImpl(OrderRepository orderRepository) {
    super();
    this.orderRepository = orderRepository;
  }

  @Override
  public void CreateOrder(Order order) {
    orderRepository.save(order);
  }

  @Override
  public Order getOrder(int orderId) {
    return orderRepository.findById((long) orderId)
        .orElseThrow(() -> new ResourceNotFoundException("Order", "Id", orderId));
  }

  @Override
  public void updatePaymentForOrder(int orderId, JsonNode paymentData) {
    System.out.println(paymentData);
    Order existingOrder = orderRepository.findById((long) orderId)
        .orElseThrow(() -> new ResourceNotFoundException("Employee", "Id", orderId));
    existingOrder.setWebhook_snapshot(paymentData.get("webhookSnapshot").asText());
    existingOrder.setStatus("PAID");
    existingOrder.setRef_id(paymentData.get("refId").asText());
    existingOrder.setTransaction_when(LocalDateTime.parse(paymentData.get("when").asText(),
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    existingOrder.setTransaction_code(paymentData.get("code").asText());
    orderRepository.save(existingOrder);

  }
}

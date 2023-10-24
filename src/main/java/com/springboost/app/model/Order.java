package com.springboost.app.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@Entity
@Table(name = "`order`")
public class Order {

  @Id
//  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id;
  @Column(name = "status", nullable = false, length = 8, columnDefinition = "VARCHAR(8) DEFAULT 'PENDING'")
  private String status;
  @Column(name = "items", nullable = false)
  private String items;
  @Column(name = "amount", nullable = false)
  private int amount;
  @Column(name = "ref_id", length = 30)
  private String ref_id;
  @Column(name = "description")
  private String description;
  @Column(name = "transaction_when")
  private LocalDateTime transaction_when;
  @Column(name = "payment_link_id")
  private String payment_link_id;
  @Column(name = "transaction_code", length = 6)
  private String transaction_code;
  @Column(name = "created_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
  private LocalDateTime created_at;
  @Column(name = "updated_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
  private LocalDateTime updated_at;
  @Column(name = "webhook_snapshot", length = 65535)
  private String webhook_snapshot;

  public Order(int id, String items, int amount, String description, String payment_link_id) {
    this.id = id;
    this.items = items;
    this.amount = amount;
    this.description = description;
    this.payment_link_id = payment_link_id;
    this.status = "PENDING";
    this.created_at = this.updated_at =  LocalDateTime.now();
  }

  public Order() {

  }

  ;
}

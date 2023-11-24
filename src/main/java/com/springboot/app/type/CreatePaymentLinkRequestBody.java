package com.springboot.app.type;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class CreatePaymentLinkRequestBody {
  private String productName;
  private String description;
  private String returnUrl;
  private int price;
  private String cancelUrl;

}
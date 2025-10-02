package com.springboot.app.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import vn.payos.PayOS;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkRequest;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;
import vn.payos.model.v2.paymentRequests.PaymentLinkItem;

@Controller
public class CheckoutController {
  private final PayOS payOS;

  public CheckoutController(PayOS payOS) {
    super();
    this.payOS = payOS;
  }

  @RequestMapping(value = "/")
  public String Index() {
    return "index";
  }

  @RequestMapping(value = "/success")
  public String Success() {
    return "success";
  }

  @RequestMapping(value = "/cancel")
  public String Cancel() {
    return "cancel";
  }

  @RequestMapping(
      method = RequestMethod.POST,
      value = "/create-payment-link",
      consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  public void checkout(HttpServletRequest request, HttpServletResponse httpServletResponse) {
    try {
      final String baseUrl = getBaseUrl(request);
      final String productName = "Mì tôm hảo hảo ly";
      final String description = "Thanh toan don hang";
      final String returnUrl = baseUrl + "/success";
      final String cancelUrl = baseUrl + "/cancel";
      final long price = 2000;
      final long orderCode = System.currentTimeMillis() / 1000;
      CreatePaymentLinkRequest paymentData =
          CreatePaymentLinkRequest.builder()
              .orderCode(orderCode)
              .amount(price)
              .description(description)
              .returnUrl(returnUrl)
              .cancelUrl(cancelUrl)
              .item(PaymentLinkItem.builder().name(productName).price(price).quantity(1).build())
              .build();
      CreatePaymentLinkResponse data = payOS.paymentRequests().create(paymentData);

      String checkoutUrl = data.getCheckoutUrl();

      httpServletResponse.setHeader("Location", checkoutUrl);
      httpServletResponse.setStatus(302);
    } catch (Exception e) {
      e.printStackTrace();
      httpServletResponse.setStatus(500);
    }
  }

  private String getBaseUrl(HttpServletRequest request) {
    String scheme = request.getScheme();
    String serverName = request.getServerName();
    int serverPort = request.getServerPort();
    String contextPath = request.getContextPath();

    String url = scheme + "://" + serverName;
    if ((scheme.equals("http") && serverPort != 80)
        || (scheme.equals("https") && serverPort != 443)) {
      url += ":" + serverPort;
    }
    url += contextPath;
    return url;
  }
}

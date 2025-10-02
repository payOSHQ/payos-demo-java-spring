package com.springboot.app.controller;

import com.springboot.app.type.ApiResponse;
import com.springboot.app.type.CreatePaymentLinkRequestBody;
import java.util.Map;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.payos.PayOS;
import vn.payos.core.FileDownloadResponse;
import vn.payos.exception.APIException;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkRequest;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;
import vn.payos.model.v2.paymentRequests.PaymentLink;
import vn.payos.model.v2.paymentRequests.PaymentLinkItem;
import vn.payos.model.v2.paymentRequests.invoices.InvoicesInfo;
import vn.payos.model.webhooks.ConfirmWebhookResponse;

@RestController
@RequestMapping("/order")
public class OrderController {
  private final PayOS payOS;

  public OrderController(PayOS payOS) {
    super();
    this.payOS = payOS;
  }

  @PostMapping(path = "/create")
  public ApiResponse<CreatePaymentLinkResponse> createPaymentLink(
      @RequestBody CreatePaymentLinkRequestBody RequestBody) {
    try {
      final String productName = RequestBody.getProductName();
      final String description = RequestBody.getDescription();
      final String returnUrl = RequestBody.getReturnUrl();
      final String cancelUrl = RequestBody.getCancelUrl();
      final long price = RequestBody.getPrice();
      long orderCode = System.currentTimeMillis() / 1000;
      PaymentLinkItem item =
          PaymentLinkItem.builder().name(productName).quantity(1).price(price).build();

      CreatePaymentLinkRequest paymentData =
          CreatePaymentLinkRequest.builder()
              .orderCode(orderCode)
              .description(description)
              .amount(price)
              .item(item)
              .returnUrl(returnUrl)
              .cancelUrl(cancelUrl)
              .build();

      CreatePaymentLinkResponse data = payOS.paymentRequests().create(paymentData);
      return ApiResponse.success(data);
    } catch (Exception e) {
      e.printStackTrace();
      return ApiResponse.error("fail");
    }
  }

  @GetMapping(path = "/{orderId}")
  public ApiResponse<PaymentLink> getOrderById(@PathVariable("orderId") long orderId) {
    try {
      PaymentLink order = payOS.paymentRequests().get(orderId);
      return ApiResponse.success("ok", order);
    } catch (Exception e) {
      e.printStackTrace();
      return ApiResponse.error(e.getMessage());
    }
  }

  @PutMapping(path = "/{orderId}")
  public ApiResponse<PaymentLink> cancelOrder(@PathVariable("orderId") long orderId) {
    try {
      PaymentLink order = payOS.paymentRequests().cancel(orderId, "change my mind");
      return ApiResponse.success("ok", order);
    } catch (Exception e) {
      e.printStackTrace();
      return ApiResponse.error(e.getMessage());
    }
  }

  @PostMapping(path = "/confirm-webhook")
  public ApiResponse<ConfirmWebhookResponse> confirmWebhook(
      @RequestBody Map<String, String> requestBody) {
    try {
      ConfirmWebhookResponse result = payOS.webhooks().confirm(requestBody.get("webhookUrl"));
      return ApiResponse.success("ok", result);
    } catch (Exception e) {
      e.printStackTrace();
      return ApiResponse.error(e.getMessage());
    }
  }

  @GetMapping(path = "/{orderId}/invoices")
  public ApiResponse<InvoicesInfo> retrieveInvoices(@PathVariable("orderId") long orderId) {
    try {
      InvoicesInfo invoicesInfo = payOS.paymentRequests().invoices().get(orderId);
      return ApiResponse.success("ok", invoicesInfo);
    } catch (Exception e) {
      e.printStackTrace();
      return ApiResponse.error(e.getMessage());
    }
  }

  @GetMapping(path = "/{orderId}/invoices/{invoiceId}/download")
  public ResponseEntity<?> downloadInvoice(
      @PathVariable("orderId") long orderId, @PathVariable("invoiceId") String invoiceId) {
    try {
      FileDownloadResponse invoiceFile =
          payOS.paymentRequests().invoices().download(invoiceId, orderId);

      if (invoiceFile == null || invoiceFile.getData() == null) {
        return ResponseEntity.status(404).body(ApiResponse.error("invoice not found or empty"));
      }

      ByteArrayResource resource = new ByteArrayResource(invoiceFile.getData());

      HttpHeaders headers = new HttpHeaders();
      String contentType =
          invoiceFile.getContentType() == null
              ? MediaType.APPLICATION_PDF_VALUE
              : invoiceFile.getContentType();
      headers.set(HttpHeaders.CONTENT_TYPE, contentType);
      headers.set(
          HttpHeaders.CONTENT_DISPOSITION,
          "attachment; filename=\"" + invoiceFile.getFilename() + "\"");
      if (invoiceFile.getSize() != null) {
        headers.setContentLength(invoiceFile.getSize());
      }

      return ResponseEntity.ok().headers(headers).body(resource);
    } catch (APIException e) {
      e.printStackTrace();
      return ResponseEntity.status(500)
          .body(ApiResponse.error(e.getErrorDesc().orElse(e.getMessage())));
    } catch (Exception e) {
      e.printStackTrace();
      return ResponseEntity.status(500).body(ApiResponse.error(e.getMessage()));
    }
  }
}

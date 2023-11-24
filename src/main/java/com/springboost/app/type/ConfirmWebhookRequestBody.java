package com.springboost.app.type;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class ConfirmWebhookRequestBody {
  private String webhookUrl;
}

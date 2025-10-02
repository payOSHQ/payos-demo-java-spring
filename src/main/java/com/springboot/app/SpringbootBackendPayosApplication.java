package com.springboot.app;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import vn.payos.PayOS;
import vn.payos.core.ClientOptions;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@Configuration
public class SpringbootBackendPayosApplication implements WebMvcConfigurer {

  @Value("${payos.client-id}")
  private String clientId;

  @Value("${payos.api-key}")
  private String apiKey;

  @Value("${payos.checksum-key}")
  private String checksumKey;

  @Value("${payos.payout-client-id}")
  private String payoutClientId;

  @Value("${payos.payout-api-key}")
  private String payoutApiKey;

  @Value("${payos.payout-checksum-key}")
  private String payoutChecksumKey;

  @Value("${payos.log-level}")
  private String logLevel;

  @Override
  public void addCorsMappings(@NonNull CorsRegistry registry) {
    registry
        .addMapping("/**")
        .allowedOrigins("*")
        .allowedMethods("*")
        .allowedHeaders("*")
        .exposedHeaders("*")
        .allowCredentials(false)
        .maxAge(3600); // Max age of the CORS pre-flight request
  }

  @Bean
  public PayOS payOS() {
    ClientOptions options =
        ClientOptions.builder()
            .clientId(clientId)
            .apiKey(apiKey)
            .checksumKey(checksumKey)
            .logLevel(ClientOptions.LogLevel.valueOf(logLevel.toUpperCase()))
            .build();
    return new PayOS(options);
  }

  @Bean
  public PayOS payOSPayout() {
    ClientOptions options =
        ClientOptions.builder()
            .clientId(payoutClientId)
            .apiKey(payoutApiKey)
            .checksumKey(payoutChecksumKey)
            .logLevel(ClientOptions.LogLevel.valueOf(logLevel.toUpperCase()))
            .build();
    return new PayOS(options);
  }

  public static void main(String[] args) {
    SpringApplication.run(SpringbootBackendPayosApplication.class, args);
  }
}

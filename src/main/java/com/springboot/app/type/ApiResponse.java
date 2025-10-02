package com.springboot.app.type;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
  private Integer error;
  private String message;
  private T data;

  public static <T> ApiResponse<T> success(T data) {
    return new ApiResponse<>(0, "success", data);
  }

  public static <T> ApiResponse<T> success(String message, T data) {
    return new ApiResponse<>(0, message, data);
  }

  public static <T> ApiResponse<T> error(String message) {
    return new ApiResponse<>(-1, message, null);
  }

  public static <T> ApiResponse<T> error(int code, String message) {
    return new ApiResponse<>(code, message, null);
  }
}

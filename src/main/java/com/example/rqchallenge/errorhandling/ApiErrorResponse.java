package com.example.rqchallenge.errorhandling;

/**
 * Error response used by the exception handlers in {@link ControllerApiExceptionAdvice}
 *
 */
public class ApiErrorResponse {
  private String errorCode;
  private String message;

  public String getErrorCode() {
    return errorCode;
  }

  public void setErrorCode(String errorCode) {
    this.errorCode = errorCode;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }
}

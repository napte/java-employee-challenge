package com.example.rqchallenge.errorhandling;

import org.springframework.http.HttpStatus;

@SuppressWarnings("serial")
public class EmployeeServiceException extends RuntimeException {
  private final HttpStatus status;
  private final String code;
  private final String message;

  public EmployeeServiceException(HttpStatus error, String code, String message) {
    this.status = error;
    this.code = code;
    this.message = message;
  }

  public HttpStatus getStatus() {
    return status;
  }

  public String getCode() {
    return code;
  }

  public String getMessage() {
    return message;
  }
}

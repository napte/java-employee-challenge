package com.example.rqchallenge.errorhandling;

public enum ErrorCodes {
  INTERNAL_SERVER_ERROR("INTERNAL_SERVER_ERROR"),
  JSON_PROCESSING_ERROR("JSON_PROCESSING_ERROR"),
  EMPLOYEES_LIST_EMPTY("EMPLOYEES_LIST_EMPTY"),
  DUMMY_API_CLIENT_ERROR("DUMMY_API_CLIENT_ERROR"),
  DUMMY_API_SERVER_ERROR("DUMMY_API_SERVER_ERROR");

  private final String code;

  ErrorCodes(String code) {
    this.code = code;
  }

  public String getCode() {
    return this.code;
  }
}

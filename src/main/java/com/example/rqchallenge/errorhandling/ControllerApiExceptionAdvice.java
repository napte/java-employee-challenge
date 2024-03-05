package com.example.rqchallenge.errorhandling;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "com.example.rqchallenge")
public class ControllerApiExceptionAdvice {

  @ExceptionHandler(EmployeeServiceException.class)
  public ResponseEntity<ApiErrorResponse> handleEmployeeServiceException(
      final EmployeeServiceException ex) {
    ApiErrorResponse apiErrorResponse = new ApiErrorResponse();
    apiErrorResponse.setErrorCode(ex.getCode());
    apiErrorResponse.setMessage(ex.getMessage());

    return ResponseEntity.status(ex.getStatus()).body(apiErrorResponse);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiErrorResponse> handleException(final Exception ex) {
    ApiErrorResponse apiErrorResponse = new ApiErrorResponse();
    apiErrorResponse.setErrorCode(HttpStatus.INTERNAL_SERVER_ERROR.name());
    apiErrorResponse.setMessage(ex.getMessage());

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiErrorResponse);
  }
}

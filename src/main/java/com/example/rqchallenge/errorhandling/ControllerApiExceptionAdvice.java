package com.example.rqchallenge.errorhandling;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;

@RestControllerAdvice(basePackages = "com.example.rqchallenge")
public class ControllerApiExceptionAdvice {

  private static final String UNKNOWN_ERROR = "UNKNOWN_ERROR";

  @ExceptionHandler(EmployeeServiceException.class)
  public ResponseEntity<ApiErrorResponse> handleEmployeeServiceException(
      final EmployeeServiceException ex) {
    ApiErrorResponse apiErrorResponse = new ApiErrorResponse();
    apiErrorResponse.setErrorCode(ex.getCode());
    apiErrorResponse.setMessage(ex.getMessage());

    return ResponseEntity.status(ex.getStatus()).body(apiErrorResponse);
  }

  @ExceptionHandler(HttpStatusCodeException.class)
  public ResponseEntity<ApiErrorResponse> handleHttpStatusCodeException(
      final HttpStatusCodeException ex) {
    HttpStatusCodeException httpStatusCodeException = (HttpStatusCodeException) ex;
    ApiErrorResponse apiErrorResponse = new ApiErrorResponse();
    apiErrorResponse.setErrorCode(httpStatusCodeException.getStatusCode().name());
    apiErrorResponse.setMessage(ex.getMessage());

    return ResponseEntity.status(httpStatusCodeException.getStatusCode()).body(apiErrorResponse);
  }

  @ExceptionHandler(RestClientException.class)
  public ResponseEntity<ApiErrorResponse> handleRestClientException(final RestClientException ex) {
    ApiErrorResponse apiErrorResponse = new ApiErrorResponse();
    apiErrorResponse.setErrorCode(UNKNOWN_ERROR);
    apiErrorResponse.setMessage(ex.getMessage());

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiErrorResponse);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiErrorResponse> handleException(final Exception ex) {
    ApiErrorResponse apiErrorResponse = new ApiErrorResponse();
    apiErrorResponse.setErrorCode(HttpStatus.INTERNAL_SERVER_ERROR.name());
    apiErrorResponse.setMessage(ex.getMessage());

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiErrorResponse);
  }
}

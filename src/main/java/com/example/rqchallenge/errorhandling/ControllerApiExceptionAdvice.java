package com.example.rqchallenge.errorhandling;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.HttpClientErrorException.TooManyRequests;

@RestControllerAdvice(basePackages = "com.example.rqchallenge")
public class ControllerApiExceptionAdvice {
  private static final String RETRY_AFTER_HEADER = "retry-after";

  private static final Logger logger = LoggerFactory.getLogger(ControllerApiExceptionAdvice.class);

  private static final String UNKNOWN_ERROR = "UNKNOWN_ERROR";

  @ExceptionHandler(EmployeeServiceException.class)
  public ResponseEntity<ApiErrorResponse> handleEmployeeServiceException(
      final EmployeeServiceException ex) {
    ApiErrorResponse apiErrorResponse = new ApiErrorResponse();
    apiErrorResponse.setErrorCode(ex.getCode());
    apiErrorResponse.setMessage(ex.getMessage());

    return ResponseEntity.status(ex.getStatus()).body(apiErrorResponse);
  }

  @ExceptionHandler(TooManyRequests.class)
  public ResponseEntity<ApiErrorResponse> handleTooManyRequestsException(final TooManyRequests ex) {
    logger.error("Exception in Dummy API service call", ex);

    HttpStatusCodeException httpStatusCodeException = (HttpStatusCodeException) ex;
    ApiErrorResponse apiErrorResponse = new ApiErrorResponse();
    apiErrorResponse.setErrorCode(httpStatusCodeException.getStatusCode().name());
    apiErrorResponse.setMessage(ex.getMessage());

    List<String> retryAfterHeader = ex.getResponseHeaders().get(RETRY_AFTER_HEADER);
    logger.warn("Retry after " + retryAfterHeader);

    return ResponseEntity
        .status(httpStatusCodeException.getStatusCode())
        .header(RETRY_AFTER_HEADER, retryAfterHeader.get(0))
        .body(apiErrorResponse);
  }

  @ExceptionHandler(HttpStatusCodeException.class)
  public ResponseEntity<ApiErrorResponse> handleHttpStatusCodeException(
      final HttpStatusCodeException ex) {
    logger.error("Exception in Dummy API service call", ex);

    HttpStatusCodeException httpStatusCodeException = (HttpStatusCodeException) ex;
    ApiErrorResponse apiErrorResponse = new ApiErrorResponse();
    apiErrorResponse.setErrorCode(httpStatusCodeException.getStatusCode().name());
    apiErrorResponse.setMessage(ex.getMessage());

    return ResponseEntity.status(httpStatusCodeException.getStatusCode()).body(apiErrorResponse);
  }

  @ExceptionHandler(RestClientException.class)
  public ResponseEntity<ApiErrorResponse> handleRestClientException(final RestClientException ex) {
    logger.error("Unknown exception from Dummy API service call", ex);

    ApiErrorResponse apiErrorResponse = new ApiErrorResponse();
    apiErrorResponse.setErrorCode(UNKNOWN_ERROR);
    apiErrorResponse.setMessage(ex.getMessage());

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiErrorResponse);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiErrorResponse> handleException(final Exception ex) {
    logger.error("Unexpected error in processing request", ex);

    ApiErrorResponse apiErrorResponse = new ApiErrorResponse();
    apiErrorResponse.setErrorCode(HttpStatus.INTERNAL_SERVER_ERROR.name());
    apiErrorResponse.setMessage(ex.getMessage());

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiErrorResponse);
  }
}

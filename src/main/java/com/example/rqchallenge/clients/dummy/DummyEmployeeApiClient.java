package com.example.rqchallenge.clients.dummy;

import static java.util.stream.Collectors.toList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException.TooManyRequests;
import org.springframework.web.client.HttpServerErrorException.GatewayTimeout;
import org.springframework.web.client.RestTemplate;
import com.example.rqchallenge.clients.dummy.dto.Constants;
import com.example.rqchallenge.clients.dummy.dto.EmployeeDetailsResponseDto;
import com.example.rqchallenge.clients.dummy.dto.EmployeeDto;
import com.example.rqchallenge.clients.dummy.dto.EmployeesListResponseDto;
import com.example.rqchallenge.errorhandling.EmployeeServiceException;
import com.example.rqchallenge.errorhandling.ErrorCodes;
import com.example.rqchallenge.model.Employee;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class DummyEmployeeApiClient {
  private static final Logger logger = LoggerFactory.getLogger(DummyEmployeeApiClient.class);

  static final String BASE_URL = "https://dummy.restapiexample.com/api/v1";

  @Autowired
  private RestTemplate restTemplate;
  @Autowired
  private ObjectMapper objectMapper;

  @Retryable(value = {TooManyRequests.class, GatewayTimeout.class},
      backoff = @Backoff(delay = 1000, multiplier = 2))
  public List<Employee> getAllEmployees() {
    logger.info("Fetching employees...");
    ResponseEntity<String> employeesListResponse =
        restTemplate.getForEntity(BASE_URL + "/employees", String.class);

    if (!employeesListResponse.getStatusCode().is2xxSuccessful()) {
      logger.error("Failure in Dummy API, status {}", employeesListResponse.getStatusCode());
      throw new EmployeeServiceException(employeesListResponse.getStatusCode(),
          employeesListResponse.getStatusCode().is4xxClientError()
              ? ErrorCodes.DUMMY_API_CLIENT_ERROR.getCode()
              : ErrorCodes.DUMMY_API_SERVER_ERROR.getCode(),
          employeesListResponse.getBody());
    }

    try {
      EmployeesListResponseDto employeesListResponseDto =
          objectMapper.readValue(employeesListResponse.getBody(), EmployeesListResponseDto.class);

      if (!employeesListResponseDto.getStatus().equals(Constants.STATUS_SUCCESS)) {
        logger
            .error("Dummy API returned status {} - ", employeesListResponseDto.getStatus(),
                employeesListResponseDto.getMessage());
        throw new EmployeeServiceException(HttpStatus.INTERNAL_SERVER_ERROR,
            ErrorCodes.DUMMY_API_SERVER_ERROR.getCode(), employeesListResponseDto.getMessage());
      }

      logger.info("Fetched details of {} employees", employeesListResponseDto.getData().size());
      List<Employee> result = employeesListResponseDto
          .getData()
          .stream()
          .map(empDto -> getEmployee(empDto))
          .collect(toList());
      return result;
    } catch (JsonProcessingException e) {
      logger.error("Error processing response from Dummy API", e);
      throw new EmployeeServiceException(HttpStatus.INTERNAL_SERVER_ERROR,
          ErrorCodes.JSON_PROCESSING_ERROR.getCode(), e.getMessage());
    }
  }

  public Employee getEmployeeById(long id) {
    logger.info("Fetching details for employee {}", id);
    ResponseEntity<String> employeesListResponse =
        restTemplate.getForEntity(BASE_URL + "/employee/" + id, String.class);

    if (!employeesListResponse.getStatusCode().is2xxSuccessful()) {
      logger.error("Failure in Dummy API, status {}", employeesListResponse.getStatusCode());
      throw new EmployeeServiceException(employeesListResponse.getStatusCode(),
          employeesListResponse.getStatusCode().is4xxClientError()
              ? ErrorCodes.DUMMY_API_CLIENT_ERROR.getCode()
              : ErrorCodes.DUMMY_API_SERVER_ERROR.getCode(),
          employeesListResponse.getBody());
    }

    try {
      EmployeeDetailsResponseDto employeesDetailsResponseDto =
          objectMapper.readValue(employeesListResponse.getBody(), EmployeeDetailsResponseDto.class);

      if (!employeesDetailsResponseDto.getStatus().equals(Constants.STATUS_SUCCESS)) {
        logger
            .error("Dummy API returned status {} - ", employeesDetailsResponseDto.getStatus(),
                employeesDetailsResponseDto.getMessage());
        throw new EmployeeServiceException(HttpStatus.INTERNAL_SERVER_ERROR,
            ErrorCodes.DUMMY_API_SERVER_ERROR.getCode(), employeesDetailsResponseDto.getMessage());
      }

      EmployeeDto employeeDto = employeesDetailsResponseDto.getData();
      logger.info("Successfully fetched details for employee {}", employeeDto.getId());
      return getEmployee(employeeDto);
    } catch (JsonProcessingException e) {
      logger.error("Error processing response from Dummy API", e);
      throw new EmployeeServiceException(HttpStatus.INTERNAL_SERVER_ERROR,
          ErrorCodes.JSON_PROCESSING_ERROR.getCode(), e.getMessage());
    }
  }

  Employee getEmployee(EmployeeDto employeeDto) {
    Employee employee = new Employee();
    employee.setId(employeeDto.getId());
    employee.setName(employeeDto.getName());
    employee.setSalary(employeeDto.getSalary());
    employee.setAge(employeeDto.getAge());
    return employee;
  }

  public Employee createEmployee(Employee employee) {
    logger.info("Create employee, name= {}", employee.getName());
    ResponseEntity<String> createEmployeeResponse =
        restTemplate.postForEntity(BASE_URL + "/create", getEmployeeDto(employee), String.class);

    if (!createEmployeeResponse.getStatusCode().is2xxSuccessful()) {
      logger.error("Failure in Dummy API, status {}", createEmployeeResponse.getStatusCode());
      throw new EmployeeServiceException(createEmployeeResponse.getStatusCode(),
          createEmployeeResponse.getStatusCode().is4xxClientError()
              ? ErrorCodes.DUMMY_API_CLIENT_ERROR.getCode()
              : ErrorCodes.DUMMY_API_SERVER_ERROR.getCode(),
          createEmployeeResponse.getBody());
    }

    try {
      EmployeeDetailsResponseDto employeesDetailsResponseDto = objectMapper
          .readValue(createEmployeeResponse.getBody(), EmployeeDetailsResponseDto.class);

      if (!employeesDetailsResponseDto.getStatus().equals(Constants.STATUS_SUCCESS)) {
        logger
            .error("Dummy API returned status {} - ", employeesDetailsResponseDto.getStatus(),
                employeesDetailsResponseDto.getMessage());
        throw new EmployeeServiceException(HttpStatus.INTERNAL_SERVER_ERROR,
            ErrorCodes.DUMMY_API_SERVER_ERROR.getCode(), employeesDetailsResponseDto.getMessage());
      }

      EmployeeDto employeeDto = employeesDetailsResponseDto.getData();
      logger
          .info("Successfully added employee {}, id = {}", employeeDto.getName(),
              employeeDto.getId());
      return getEmployee(employeeDto);
    } catch (JsonProcessingException e) {
      logger.error("Error processing response from Dummy API", e);
      throw new EmployeeServiceException(HttpStatus.INTERNAL_SERVER_ERROR,
          ErrorCodes.JSON_PROCESSING_ERROR.getCode(), e.getMessage());
    }
  }

  EmployeeDto getEmployeeDto(Employee employee) {
    EmployeeDto employeeDto = new EmployeeDto();
    employeeDto.setId(employee.getId());
    employeeDto.setName(employee.getName());
    employeeDto.setSalary(employee.getSalary());
    employeeDto.setAge(employee.getAge());
    return employeeDto;
  }

  public void deleteEmployeeById(long id) {
    logger.info("Delete employee {}", id);
    restTemplate.delete(BASE_URL + "/delete/" + id);
    logger.info("Successfully deleted employee {}", id);
  }
}

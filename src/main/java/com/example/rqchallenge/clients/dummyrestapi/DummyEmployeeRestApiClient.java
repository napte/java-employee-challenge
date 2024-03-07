package com.example.rqchallenge.clients.dummyrestapi;

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
import org.springframework.web.client.HttpServerErrorException.GatewayTimeout;
import org.springframework.web.client.HttpServerErrorException.ServiceUnavailable;
import org.springframework.web.client.RestTemplate;
import com.example.rqchallenge.clients.dummyrestapi.dto.Constants;
import com.example.rqchallenge.clients.dummyrestapi.dto.EmployeeDetailsResponseDto;
import com.example.rqchallenge.clients.dummyrestapi.dto.EmployeeDto;
import com.example.rqchallenge.clients.dummyrestapi.dto.EmployeesListResponseDto;
import com.example.rqchallenge.dao.IEmployeeDataSource;
import com.example.rqchallenge.errorhandling.EmployeeServiceException;
import com.example.rqchallenge.errorhandling.ErrorCodes;
import com.example.rqchallenge.model.Employee;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * A data source for employees that fetches information from the dummy rest API.<br>
 * 
 * @see https://dummy.restapiexample.com/
 */
@Component
public class DummyEmployeeRestApiClient implements IEmployeeDataSource {
  private static final Logger logger = LoggerFactory.getLogger(DummyEmployeeRestApiClient.class);

  static final String BASE_URL = "https://dummy.restapiexample.com/api/v1";

  @Autowired
  private RestTemplate restTemplate;
  @Autowired
  private ObjectMapper objectMapper;

  @Override
  @Retryable(value = {ServiceUnavailable.class, GatewayTimeout.class},
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

  @Override
  @Retryable(value = {ServiceUnavailable.class, GatewayTimeout.class},
      backoff = @Backoff(delay = 1000, multiplier = 2))
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

      // XXX: The Dummy Rest API returns successful response with null data for invalid employee ID
      if (employeeDto == null) {
        String errorMsg = String.format("Employee with id %d not found", id);
        logger.error(errorMsg);
        throw new EmployeeServiceException(HttpStatus.NOT_FOUND,
            ErrorCodes.EMPLOYEE_NOT_FOUND.getCode(), errorMsg);
      }

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

  @Override
  @Retryable(value = {ServiceUnavailable.class, GatewayTimeout.class},
      backoff = @Backoff(delay = 1000, multiplier = 2))
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

  @Override
  @Retryable(value = {ServiceUnavailable.class, GatewayTimeout.class},
      backoff = @Backoff(delay = 1000, multiplier = 2))
  public void deleteEmployeeById(long id) {
    logger.info("Delete employee {}", id);

    // XXX: Not invoking getEmployeeById() since Dummy Rest API returns 429 Too Many Requests error
    /**
     * 
     * //Dummy delete API does not fail for a non-existing ID, try fetching employee details first
     * 
     * getEmployeeById(id); // throws NOT_FOUND error if employee with ID not present
     */

    restTemplate.delete(BASE_URL + "/delete/" + id);
    logger.info("Successfully deleted employee {}", id);
  }
}

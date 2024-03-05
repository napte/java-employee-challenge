package com.example.rqchallenge.clients.dummy;

import static java.util.stream.Collectors.toList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import com.example.rqchallenge.clients.dummy.dto.Constants;
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

  @Autowired
  private RestTemplate restTemplate;

  @Autowired
  private ObjectMapper objectMapper;

  public List<Employee> getAllEmployees() {
    try {
      ResponseEntity<String> employeesListResponse = restTemplate
          .getForEntity("https://dummy.restapiexample.com/api/v1/employees", String.class);

      if (employeesListResponse.getStatusCode().is2xxSuccessful()) {
        EmployeesListResponseDto employeesListResponseDto =
            objectMapper.readValue(employeesListResponse.getBody(), EmployeesListResponseDto.class);

        if (employeesListResponseDto.getStatus().equals(Constants.STATUS_SUCCESS)) {
          logger.info("Fetched details of {} employees", employeesListResponseDto.getData().size());
          List<Employee> result = employeesListResponseDto
              .getData()
              .stream()
              .map(empDto -> getEmployee(empDto))
              .collect(toList());
          return result;
        } else {
          logger
              .error("Dummy API returned status {} - ", employeesListResponseDto.getStatus(),
                  employeesListResponseDto.getMessage());
          throw new EmployeeServiceException(HttpStatus.INTERNAL_SERVER_ERROR,
              ErrorCodes.DUMMY_API_SERVER_ERROR.getCode(), employeesListResponseDto.getMessage());
        }
      } else {
        logger.error("Failure in Dummy API, status {}", employeesListResponse.getStatusCode());
        throw new EmployeeServiceException(employeesListResponse.getStatusCode(),
            employeesListResponse.getStatusCode().is4xxClientError()
                ? ErrorCodes.DUMMY_API_CLIENT_ERROR.getCode()
                : ErrorCodes.DUMMY_API_SERVER_ERROR.getCode(),
            employeesListResponse.getBody());
      }
    } catch (RestClientException e) {
      logger.error("Unexpected exception in Dummy API", e);
      throw new EmployeeServiceException(HttpStatus.INTERNAL_SERVER_ERROR,
          ErrorCodes.DUMMY_API_SERVER_ERROR.getCode(), e.getMessage());
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
}
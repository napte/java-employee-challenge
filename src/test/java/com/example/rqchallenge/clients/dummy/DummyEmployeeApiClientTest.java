package com.example.rqchallenge.clients.dummy;

import static java.util.stream.Collectors.toSet;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpClientErrorException.TooManyRequests;
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
import com.fasterxml.jackson.databind.node.ObjectNode;

public class DummyEmployeeApiClientTest {
  private static final long EMP_ID1 = 100;
  private static final long EMP_ID2 = 101;
  private static final String EMP1_NAME = "Bob";
  private static final int EMP1_AGE = 35;
  private static final int EMP1_SALARY = 50000;
  @Mock
  private RestTemplate mockRestTemplate;
  @Spy
  private ObjectMapper objectMapper;

  @InjectMocks
  private DummyEmployeeApiClient dummyEmployeeApiClient;

  @BeforeEach
  void setup() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void testGetAllEmployeesSuccess() throws JsonProcessingException {
    String url = DummyEmployeeApiClient.BASE_URL + "/employees";
    EmployeesListResponseDto employeesListResponseDto = new EmployeesListResponseDto();
    employeesListResponseDto.setStatus(Constants.STATUS_SUCCESS);
    EmployeeDto emp1 = new EmployeeDto();
    emp1.setId(EMP_ID1);
    EmployeeDto emp2 = new EmployeeDto();
    emp2.setId(EMP_ID2);
    List<EmployeeDto> employeeList = List.of(emp1, emp2);
    employeesListResponseDto.setData(employeeList);
    String employeesListJson = objectMapper.writeValueAsString(employeesListResponseDto);
    when(mockRestTemplate.getForEntity(eq(url), eq(String.class)))
        .thenReturn(ResponseEntity.ok(employeesListJson));

    List<Employee> employees = dummyEmployeeApiClient.getAllEmployees();

    assertEquals(employeeList.size(), employees.size());
    Set<Long> employeeIds = employees.stream().map(Employee::getId).collect(toSet());
    assertEquals(employeeIds, Set.of(EMP_ID1, EMP_ID2));
  }

  @Test
  void testGetAllEmployeesSuccess2() throws JsonProcessingException {
    String url = DummyEmployeeApiClient.BASE_URL + "/employees";
    EmployeesListResponseDto employeesListResponseDto = new EmployeesListResponseDto();
    employeesListResponseDto.setStatus(Constants.STATUS_SUCCESS);
    EmployeeDto emp = new EmployeeDto();
    emp.setId(EMP_ID1);
    emp.setName(EMP1_NAME);
    emp.setAge(EMP1_AGE);
    emp.setSalary(EMP1_SALARY);
    employeesListResponseDto.setData(Collections.singletonList(emp));
    String employeesListJson = objectMapper.writeValueAsString(employeesListResponseDto);
    when(mockRestTemplate.getForEntity(eq(url), eq(String.class)))
        .thenReturn(ResponseEntity.ok(employeesListJson));

    List<Employee> employees = dummyEmployeeApiClient.getAllEmployees();

    assertEquals(1, employees.size());
    Employee employee = employees.get(0);
    assertEquals(EMP_ID1, employee.getId());
    assertEquals(emp.getName(), employee.getName());
    assertEquals(emp.getAge(), employee.getAge());
    assertEquals(emp.getSalary(), employee.getSalary());
  }

  @Test
  void testGetAllEmployeesFailureStatusCodeServerError() throws JsonProcessingException {
    String url = DummyEmployeeApiClient.BASE_URL + "/employees";
    when(mockRestTemplate.getForEntity(eq(url), eq(String.class)))
        .thenReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal error"));

    EmployeeServiceException exception = assertThrows(EmployeeServiceException.class,
        () -> dummyEmployeeApiClient.getAllEmployees());

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatus());
    assertEquals(ErrorCodes.DUMMY_API_SERVER_ERROR.getCode(), exception.getCode());
  }

  @Test
  void testGetAllEmployeesFailureStatusCodeClientError() throws JsonProcessingException {
    String url = DummyEmployeeApiClient.BASE_URL + "/employees";
    when(mockRestTemplate.getForEntity(eq(url), eq(String.class)))
        .thenReturn(ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Bad Request"));

    EmployeeServiceException exception = assertThrows(EmployeeServiceException.class,
        () -> dummyEmployeeApiClient.getAllEmployees());

    assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
    assertEquals(ErrorCodes.DUMMY_API_CLIENT_ERROR.getCode(), exception.getCode());
  }

  @Test
  void testGetAllEmployeesFailureResponseBodyStatusNotSuccess() throws JsonProcessingException {
    String url = DummyEmployeeApiClient.BASE_URL + "/employees";
    EmployeesListResponseDto employeesListResponseDto = new EmployeesListResponseDto();
    employeesListResponseDto.setStatus(Constants.STATUS_FAILURE);
    String employeesListJson = objectMapper.writeValueAsString(employeesListResponseDto);
    when(mockRestTemplate.getForEntity(eq(url), eq(String.class)))
        .thenReturn(ResponseEntity.ok(employeesListJson));

    EmployeeServiceException exception = assertThrows(EmployeeServiceException.class,
        () -> dummyEmployeeApiClient.getAllEmployees());

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatus());
    assertEquals(ErrorCodes.DUMMY_API_SERVER_ERROR.getCode(), exception.getCode());
  }

  @Test
  void testGetAllEmployeesFailureResponseBodyGarbled() throws JsonProcessingException {
    String url = DummyEmployeeApiClient.BASE_URL + "/employees";
    String employeesListJson = "{\"status\": \"success\", \"data\": [garbled_data}";
    when(mockRestTemplate.getForEntity(eq(url), eq(String.class)))
        .thenReturn(ResponseEntity.ok(employeesListJson));

    EmployeeServiceException exception = assertThrows(EmployeeServiceException.class,
        () -> dummyEmployeeApiClient.getAllEmployees());

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatus());
    assertEquals(ErrorCodes.JSON_PROCESSING_ERROR.getCode(), exception.getCode());
  }

  @Test
  @Disabled
  void testGetAllEmployeesSuccessAfterOneRetry() throws JsonProcessingException {
    String url = DummyEmployeeApiClient.BASE_URL + "/employees";
    EmployeesListResponseDto employeesListResponseDto = new EmployeesListResponseDto();
    employeesListResponseDto.setStatus(Constants.STATUS_SUCCESS);
    final EmployeeDto emp = new EmployeeDto();
    employeesListResponseDto.setData(Collections.singletonList(emp));
    String employeesListJson = objectMapper.writeValueAsString(employeesListResponseDto);

    TooManyRequests dummyException = (TooManyRequests) HttpClientErrorException
        .create("", HttpStatus.TOO_MANY_REQUESTS, "", new HttpHeaders(), new byte[0],
            Charset.forName("UTF-8"));
    when(mockRestTemplate.getForEntity(eq(url), eq(String.class))).thenAnswer((invocation) -> {
      if (emp.getId() == 0) {
        emp.setId(EMP_ID1);
        throw dummyException;
      } else {
        return employeesListJson;
      }
    });

    List<Employee> employees = dummyEmployeeApiClient.getAllEmployees();

    assertEquals(1, employees.size());
    assertEquals(EMP_ID1, employees.get(0).getId());
    verify(mockRestTemplate, times(2)).getForEntity(eq(url), eq(String.class));
  }

  @Test
  void testGetEmployeeDetailsSuccess() throws JsonProcessingException {
    String url = DummyEmployeeApiClient.BASE_URL + "/employee/" + EMP_ID1;
    EmployeeDetailsResponseDto employeeDetailsResponseDto = new EmployeeDetailsResponseDto();
    employeeDetailsResponseDto.setStatus(Constants.STATUS_SUCCESS);
    EmployeeDto empDto = new EmployeeDto();
    empDto.setId(EMP_ID1);
    empDto.setName(EMP1_NAME);
    empDto.setAge(EMP1_AGE);
    empDto.setSalary(EMP1_SALARY);
    employeeDetailsResponseDto.setData(empDto);
    String employeesDetailsJson = objectMapper.writeValueAsString(employeeDetailsResponseDto);
    when(mockRestTemplate.getForEntity(eq(url), eq(String.class)))
        .thenReturn(ResponseEntity.ok(employeesDetailsJson));

    Employee employee = dummyEmployeeApiClient.getEmployeeById(EMP_ID1);
    assertEquals(EMP_ID1, employee.getId());
    assertEquals(empDto.getName(), employee.getName());
    assertEquals(empDto.getSalary(), employee.getSalary());
    assertEquals(empDto.getAge(), employee.getAge());
  }

  @Test
  void testGetEmployeeDetailsFailureStatusCodeServerError() throws JsonProcessingException {
    String url = DummyEmployeeApiClient.BASE_URL + "/employee/" + EMP_ID1;
    when(mockRestTemplate.getForEntity(eq(url), eq(String.class)))
        .thenReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal error"));

    EmployeeServiceException exception = assertThrows(EmployeeServiceException.class,
        () -> dummyEmployeeApiClient.getEmployeeById(EMP_ID1));

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatus());
    assertEquals(ErrorCodes.DUMMY_API_SERVER_ERROR.getCode(), exception.getCode());
  }

  @Test
  void testGetEmployeeDetailsFailureStatusCodeClientError() throws JsonProcessingException {
    String url = DummyEmployeeApiClient.BASE_URL + "/employee/" + EMP_ID1;
    when(mockRestTemplate.getForEntity(eq(url), eq(String.class)))
        .thenReturn(ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Bad Request"));

    EmployeeServiceException exception = assertThrows(EmployeeServiceException.class,
        () -> dummyEmployeeApiClient.getEmployeeById(EMP_ID1));

    assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
    assertEquals(ErrorCodes.DUMMY_API_CLIENT_ERROR.getCode(), exception.getCode());
  }

  @Test
  void testGetEmployeeDetailsFailureResponseBodyStatusNotSuccess() throws JsonProcessingException {
    String url = DummyEmployeeApiClient.BASE_URL + "/employee/" + EMP_ID1;
    EmployeeDetailsResponseDto employeesListResponseDto = new EmployeeDetailsResponseDto();
    employeesListResponseDto.setStatus(Constants.STATUS_FAILURE);
    String employeesListJson = objectMapper.writeValueAsString(employeesListResponseDto);
    when(mockRestTemplate.getForEntity(eq(url), eq(String.class)))
        .thenReturn(ResponseEntity.ok(employeesListJson));

    EmployeeServiceException exception = assertThrows(EmployeeServiceException.class,
        () -> dummyEmployeeApiClient.getEmployeeById(EMP_ID1));

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatus());
    assertEquals(ErrorCodes.DUMMY_API_SERVER_ERROR.getCode(), exception.getCode());
  }

  @Test
  void testGetEmployeeDetailsFailureResponseBodyGarbled() throws JsonProcessingException {
    String url = DummyEmployeeApiClient.BASE_URL + "/employee/" + EMP_ID1;
    String employeesDetailsJson = "{\"status\": \"success\", \"data\": [garbled_data}";
    when(mockRestTemplate.getForEntity(eq(url), eq(String.class)))
        .thenReturn(ResponseEntity.ok(employeesDetailsJson));

    EmployeeServiceException exception = assertThrows(EmployeeServiceException.class,
        () -> dummyEmployeeApiClient.getEmployeeById(EMP_ID1));

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatus());
    assertEquals(ErrorCodes.JSON_PROCESSING_ERROR.getCode(), exception.getCode());
  }

  @Test
  void testGetEmployeeDetailsFailureResponseBodyNull() throws JsonProcessingException {
    String url = DummyEmployeeApiClient.BASE_URL + "/employee/" + EMP_ID1;
    ObjectNode emptyData = objectMapper.createObjectNode();
    emptyData.put("status", Constants.STATUS_SUCCESS);
    emptyData.set("data", objectMapper.nullNode());
    String employeesEmptyDataJson = objectMapper.writeValueAsString(emptyData);
    when(mockRestTemplate.getForEntity(eq(url), eq(String.class)))
        .thenReturn(ResponseEntity.ok(employeesEmptyDataJson));

    EmployeeServiceException exception = assertThrows(EmployeeServiceException.class,
        () -> dummyEmployeeApiClient.getEmployeeById(EMP_ID1));

    assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
    assertEquals(ErrorCodes.EMPLOYEE_NOT_FOUND.getCode(), exception.getCode());
  }

  @Test
  void testCreateEmployeeSuccess() throws JsonProcessingException {
    String url = DummyEmployeeApiClient.BASE_URL + "/create";
    EmployeeDetailsResponseDto createEmployeeResponseDto = new EmployeeDetailsResponseDto();
    createEmployeeResponseDto.setStatus(Constants.STATUS_SUCCESS);
    Employee employee = new Employee();
    employee.setName(EMP1_NAME);
    employee.setAge(EMP1_AGE);
    employee.setSalary(EMP1_SALARY);

    EmployeeDto empDto = new EmployeeDto();
    empDto.setId(new Random().nextLong());
    empDto.setName(EMP1_NAME);
    empDto.setAge(EMP1_AGE);
    empDto.setSalary(EMP1_SALARY);
    createEmployeeResponseDto.setData(empDto);
    String createdEmployeesDetailsJson = objectMapper.writeValueAsString(createEmployeeResponseDto);
    when(mockRestTemplate
        .postForEntity(eq(url),
            argThat((EmployeeDto empDtoArg) -> empDtoArg.getName().equals(employee.getName())),
            eq(String.class))).thenReturn(ResponseEntity.ok(createdEmployeesDetailsJson));

    Employee employeeAdded = dummyEmployeeApiClient.createEmployee(employee);
    assertNotNull(employeeAdded.getId());
    assertEquals(employee.getName(), employeeAdded.getName());
    assertEquals(employee.getSalary(), employeeAdded.getSalary());
    assertEquals(employee.getAge(), employeeAdded.getAge());
  }

  @Test
  void testCreateEmployeeFailureStatusCodeServerError() throws JsonProcessingException {
    String url = DummyEmployeeApiClient.BASE_URL + "/create";
    when(mockRestTemplate.postForEntity(eq(url), any(), eq(String.class)))
        .thenReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal error"));

    EmployeeServiceException exception = assertThrows(EmployeeServiceException.class,
        () -> dummyEmployeeApiClient.createEmployee(new Employee()));

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatus());
    assertEquals(ErrorCodes.DUMMY_API_SERVER_ERROR.getCode(), exception.getCode());
  }

  @Test
  void testCreateEmployeeFailureStatusCodeClientError() throws JsonProcessingException {
    String url = DummyEmployeeApiClient.BASE_URL + "/create";
    when(mockRestTemplate.postForEntity(eq(url), any(), eq(String.class)))
        .thenReturn(ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Bad Request"));

    EmployeeServiceException exception = assertThrows(EmployeeServiceException.class,
        () -> dummyEmployeeApiClient.createEmployee(new Employee()));

    assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
    assertEquals(ErrorCodes.DUMMY_API_CLIENT_ERROR.getCode(), exception.getCode());
  }

  @Test
  void testCreateEmployeeFailureResponseBodyStatusNotSuccess() throws JsonProcessingException {
    String url = DummyEmployeeApiClient.BASE_URL + "/create";
    EmployeeDetailsResponseDto employeesListResponseDto = new EmployeeDetailsResponseDto();
    employeesListResponseDto.setStatus(Constants.STATUS_FAILURE);
    String employeesListJson = objectMapper.writeValueAsString(employeesListResponseDto);
    when(mockRestTemplate.postForEntity(eq(url), any(), eq(String.class)))
        .thenReturn(ResponseEntity.ok(employeesListJson));

    EmployeeServiceException exception = assertThrows(EmployeeServiceException.class,
        () -> dummyEmployeeApiClient.createEmployee(new Employee()));

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatus());
    assertEquals(ErrorCodes.DUMMY_API_SERVER_ERROR.getCode(), exception.getCode());
  }

  @Test
  void testCreateEmployeeFailureResponseBodyGarbled() throws JsonProcessingException {
    String url = DummyEmployeeApiClient.BASE_URL + "/create";
    String employeesDetailsJson = "{\"status\": \"success\", \"data\": [garbled_data}";
    when(mockRestTemplate.postForEntity(eq(url), any(), eq(String.class)))
        .thenReturn(ResponseEntity.ok(employeesDetailsJson));

    EmployeeServiceException exception = assertThrows(EmployeeServiceException.class,
        () -> dummyEmployeeApiClient.createEmployee(new Employee()));

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatus());
    assertEquals(ErrorCodes.JSON_PROCESSING_ERROR.getCode(), exception.getCode());
  }

  @Test
  void testDeleteEmployee() throws JsonProcessingException {
    String url = DummyEmployeeApiClient.BASE_URL + "/employee/" + EMP_ID1;
    EmployeeDetailsResponseDto employeeDetailsResponseDto = new EmployeeDetailsResponseDto();
    employeeDetailsResponseDto.setStatus(Constants.STATUS_SUCCESS);
    EmployeeDto empDto = new EmployeeDto();
    empDto.setId(EMP_ID1);
    empDto.setName(EMP1_NAME);
    empDto.setAge(EMP1_AGE);
    empDto.setSalary(EMP1_SALARY);
    employeeDetailsResponseDto.setData(empDto);
    String employeesDetailsJson = objectMapper.writeValueAsString(employeeDetailsResponseDto);
    when(mockRestTemplate.getForEntity(eq(url), eq(String.class)))
        .thenReturn(ResponseEntity.ok(employeesDetailsJson));
    doNothing().when(mockRestTemplate).delete(any());

    dummyEmployeeApiClient.deleteEmployeeById(EMP_ID1);

    verify(mockRestTemplate, times(1))
        .delete(DummyEmployeeApiClient.BASE_URL + "/delete/" + EMP_ID1);
  }

  @Test
  void testDeleteEmployeeNotFound() throws JsonProcessingException {
    String url = DummyEmployeeApiClient.BASE_URL + "/employee/" + EMP_ID1;
    ObjectNode emptyData = objectMapper.createObjectNode();
    emptyData.put("status", Constants.STATUS_SUCCESS);
    emptyData.set("data", objectMapper.nullNode());
    String employeesEmptyDataJson = objectMapper.writeValueAsString(emptyData);
    when(mockRestTemplate.getForEntity(eq(url), eq(String.class)))
        .thenReturn(ResponseEntity.ok(employeesEmptyDataJson));

    doNothing().when(mockRestTemplate).delete(any());

    EmployeeServiceException exception = assertThrows(EmployeeServiceException.class,
        () -> dummyEmployeeApiClient.deleteEmployeeById(EMP_ID1));

    assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
    assertEquals(ErrorCodes.EMPLOYEE_NOT_FOUND.getCode(), exception.getCode());
  }
}

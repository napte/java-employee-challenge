package com.example.rqchallenge.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import com.example.rqchallenge.dao.IEmployeeDataSource;
import com.example.rqchallenge.errorhandling.EmployeeServiceException;
import com.example.rqchallenge.errorhandling.ErrorCodes;
import com.example.rqchallenge.model.Employee;

public class EmployeeServiceTest {
  @Mock
  private IEmployeeDataSource mockEmployeeDataSource;

  @InjectMocks
  private EmployeeService employeeService;

  @BeforeEach
  void setup() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void testGetAllEmployees() {
    when(mockEmployeeDataSource.getAllEmployees())
        .thenReturn(Collections.singletonList(new Employee()));

    List<Employee> employees = employeeService.getAllEmployees();

    verify(mockEmployeeDataSource, times(1)).getAllEmployees();
    assertEquals(1, employees.size());
  }

  @Test
  void testGetEmployeeById() {
    long empId = 123L;

    Employee employee = new Employee();
    when(mockEmployeeDataSource.getEmployeeById(empId)).thenReturn(employee);

    Employee employeeResult = employeeService.getEmployeeById(empId);

    verify(mockEmployeeDataSource, times(1)).getEmployeeById(eq(empId));
    assertEquals(employee, employeeResult);
  }

  @Test
  void testCreateEmployee() {
    Employee employee = new Employee();
    employeeService.createEmployee(employee);

    verify(mockEmployeeDataSource, times(1)).createEmployee(eq(employee));
  }

  @Test
  void testDeleteEmployeeById() {
    long empId = 123L;
    doNothing().when(mockEmployeeDataSource).deleteEmployeeById(empId);

    employeeService.deleteEmployeeById(empId);

    verify(mockEmployeeDataSource, times(1)).deleteEmployeeById(eq(empId));
  }

  @Test
  void testGetEmployeesByName() {
    String searchString = "Jon";

    Employee jon = new Employee();
    jon.setName("Jon");
    Employee jonny = new Employee();
    jonny.setName("Jonny");
    Employee janardan = new Employee();
    janardan.setName("Janardan");
    Employee joanna = new Employee();
    joanna.setName("Joanna");
    Employee jonathan = new Employee();
    jonathan.setName("Jonathan Seagull");
    Employee sean = new Employee();
    sean.setName("Sean Jones");
    List<Employee> employees = List.of(jon, jonny, janardan, joanna, jonathan, sean);
    when(mockEmployeeDataSource.getAllEmployees()).thenReturn(employees);

    List<Employee> searchResult = employeeService.getEmployeesByName(searchString);
    assertEquals(4, searchResult.size());
  }

  @Test
  void testGetHighestSalary() {
    Employee jon = new Employee();
    jon.setName("Jon");
    jon.setSalary(25000);

    Employee jonny = new Employee();
    jonny.setName("Jonny");
    jonny.setSalary(15000);

    Employee janardan = new Employee();
    janardan.setName("Janardan");
    janardan.setSalary(25000);

    Employee joanna = new Employee();
    joanna.setName("Joanna");
    joanna.setSalary(20000);

    List<Employee> employees = List.of(jon, jonny, janardan, joanna);
    when(mockEmployeeDataSource.getAllEmployees()).thenReturn(employees);

    int highestSalary = employeeService.getHighestSalary();
    assertEquals(25000, highestSalary);
  }

  @Test
  void testGetHighestSalaryEmployeeListEmpty() {
    when(mockEmployeeDataSource.getAllEmployees()).thenReturn(Collections.emptyList());

    EmployeeServiceException exception = Assertions
        .assertThrows(EmployeeServiceException.class, () -> employeeService.getHighestSalary());
    assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
    assertEquals(ErrorCodes.EMPLOYEES_LIST_EMPTY.getCode(), exception.getCode());
  }

  @Test
  void testGetTopNBySalary() {
    Employee jon = new Employee();
    jon.setName("Jon");
    jon.setSalary(25000);

    Employee jonny = new Employee();
    jonny.setName("Jonny");
    jonny.setSalary(15000);

    Employee janardan = new Employee();
    janardan.setName("Janardan");
    janardan.setSalary(35000);

    Employee joanna = new Employee();
    joanna.setName("Joanna");
    joanna.setSalary(28000);

    List<Employee> employees = List.of(jon, jonny, janardan, joanna);
    when(mockEmployeeDataSource.getAllEmployees()).thenReturn(employees);

    List<Employee> topNEmployees = employeeService.getTopNBySalary(2);
    assertEquals(2, topNEmployees.size());
    assertEquals("Janardan", topNEmployees.get(0).getName());
    assertEquals("Joanna", topNEmployees.get(1).getName());
  }

  @Test
  void testGetTopNBySalaryLargeN() {
    Employee jon = new Employee();
    jon.setName("Jon");
    jon.setSalary(25000);

    Employee jonny = new Employee();
    jonny.setName("Jonny");
    jonny.setSalary(15000);

    Employee janardan = new Employee();
    janardan.setName("Janardan");
    janardan.setSalary(35000);

    Employee joanna = new Employee();
    joanna.setName("Joanna");
    joanna.setSalary(28000);

    List<Employee> employees = List.of(jon, jonny, janardan, joanna);
    when(mockEmployeeDataSource.getAllEmployees()).thenReturn(employees);

    List<Employee> topNEmployees = employeeService.getTopNBySalary(10);
    assertEquals(4, topNEmployees.size());
    assertEquals("Janardan", topNEmployees.get(0).getName());
  }

  @Test
  void testGetTopNBySalaryEmptyEmployeeList() {
    when(mockEmployeeDataSource.getAllEmployees()).thenReturn(Collections.emptyList());

    List<Employee> topNEmployees = employeeService.getTopNBySalary(10);
    assertTrue(topNEmployees.isEmpty());
  }

}

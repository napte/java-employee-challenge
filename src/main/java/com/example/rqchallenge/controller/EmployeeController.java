package com.example.rqchallenge.controller;

import static java.util.stream.Collectors.toList;
import java.io.IOException;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import com.example.rqchallenge.model.Employee;
import com.example.rqchallenge.service.IEmployeeService;

@RestController
public class EmployeeController implements IEmployeeController {
  @Autowired
  private IEmployeeService employeeService;

  @Override
  public ResponseEntity<List<Employee>> getAllEmployees() throws IOException {
    return ResponseEntity.ok(employeeService.getAllEmployees());
  }

  @Override
  public ResponseEntity<List<Employee>> getEmployeesByNameSearch(String searchString) {
    return ResponseEntity.ok(employeeService.getEmployeesByName(searchString));
  }

  @Override
  public ResponseEntity<Employee> getEmployeeById(String id) {
    return ResponseEntity.ok(employeeService.getEmployeeById(Long.parseLong(id)));
  }

  @Override
  public ResponseEntity<Integer> getHighestSalaryOfEmployees() {
    return ResponseEntity.ok(employeeService.getHighestSalary());
  }

  @Override
  public ResponseEntity<List<String>> getTopTenHighestEarningEmployeeNames() {
    List<Employee> topNEmployees = employeeService.getTopNBySalary(10);
    List<String> employeeNames = topNEmployees.stream().map(Employee::getName).collect(toList());
    return ResponseEntity.ok(employeeNames);
  }

  @Override
  public ResponseEntity<Employee> createEmployee(Employee employeeInput) {
    return ResponseEntity.ok(employeeService.createEmployee(employeeInput));
  }

  @Override
  public ResponseEntity<Void> deleteEmployeeById(String id) {
    employeeService.deleteEmployeeById(Long.parseLong(id));
    return ResponseEntity.noContent().build();
  }

}

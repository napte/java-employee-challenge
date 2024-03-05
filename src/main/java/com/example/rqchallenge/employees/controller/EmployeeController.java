package com.example.rqchallenge.employees.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import com.example.rqchallenge.employees.model.Employee;
import com.example.rqchallenge.employees.service.IEmployeeService;

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
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ResponseEntity<Employee> getEmployeeById(String id) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ResponseEntity<Integer> getHighestSalaryOfEmployees() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ResponseEntity<List<String>> getTopTenHighestEarningEmployeeNames() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ResponseEntity<Employee> createEmployee(Map<String, Object> employeeInput) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ResponseEntity<String> deleteEmployeeById(String id) {
    // TODO Auto-generated method stub
    return null;
  }

}

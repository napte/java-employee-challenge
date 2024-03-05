package com.example.rqchallenge.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.rqchallenge.clients.dummy.DummyEmployeeApiClient;
import com.example.rqchallenge.model.Employee;
import com.example.rqchallenge.service.IEmployeeService;

@Service
public class EmployeeService implements IEmployeeService {
  @Autowired
  private DummyEmployeeApiClient employeeApiClient;

  @Override
  public List<Employee> getAllEmployees() {
    return employeeApiClient.getAllEmployees();
  }

}
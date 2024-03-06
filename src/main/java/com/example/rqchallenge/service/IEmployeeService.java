package com.example.rqchallenge.service;

import java.util.List;
import com.example.rqchallenge.model.Employee;

public interface IEmployeeService {
  List<Employee> getAllEmployees();

  Employee getEmployeeById(long id);

  Employee createEmployee(Employee employeeInput);

  void deleteEmployeeById(long id);

  List<Employee> getEmployeesByName(String searchString);

  Integer getHighestSalary();

  List<Employee> getTopNBySalary(int count);
}

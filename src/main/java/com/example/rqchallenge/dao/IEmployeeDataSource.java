package com.example.rqchallenge.dao;

import java.util.List;
import com.example.rqchallenge.model.Employee;

/**
 * Data source for employees
 *
 */
public interface IEmployeeDataSource {

  List<Employee> getAllEmployees();

  Employee getEmployeeById(long id);

  Employee createEmployee(Employee employee);

  void deleteEmployeeById(long id);

}

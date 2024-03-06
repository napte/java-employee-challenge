package com.example.rqchallenge.service;

import java.util.List;
import com.example.rqchallenge.model.Employee;

public interface IEmployeeService {
  List<Employee> getAllEmployees();

  Employee getEmployeeById(long id);

  Employee createEmployee(Employee employeeInput);

  void deleteEmployeeById(long id);

  /**
   * Searches all employees whose name contains or matches the specified input string.
   * 
   * @param searchString search string
   * @return List of matching employees
   */
  List<Employee> getEmployeesByName(String searchString);

  /**
   * Return the highest salary among all the employees
   * 
   * @return highest salary
   */
  Integer getHighestSalary();

  /**
   * Return a list of the top <tt>N</tt> employees based on their salaries
   * 
   * @param count count of top records to fetch
   * @return resultant top N employees
   */
  List<Employee> getTopNBySalary(int count);
}

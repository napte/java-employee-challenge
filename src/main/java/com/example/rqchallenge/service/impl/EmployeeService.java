package com.example.rqchallenge.service.impl;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import java.util.Comparator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import com.example.rqchallenge.dao.IEmployeeDataSource;
import com.example.rqchallenge.errorhandling.EmployeeServiceException;
import com.example.rqchallenge.errorhandling.ErrorCodes;
import com.example.rqchallenge.model.Employee;
import com.example.rqchallenge.service.IEmployeeService;

@Service
public class EmployeeService implements IEmployeeService {
  private static final Logger logger = LoggerFactory.getLogger(EmployeeService.class);

  @Autowired
  private IEmployeeDataSource employeeDataSource;

  @Override
  public List<Employee> getAllEmployees() {
    return employeeDataSource.getAllEmployees();
  }

  @Override
  public Employee getEmployeeById(long id) {
    return employeeDataSource.getEmployeeById(id);
  }

  @Override
  public Employee createEmployee(Employee employee) {
    return employeeDataSource.createEmployee(employee);
  }

  @Override
  public void deleteEmployeeById(long id) {
    employeeDataSource.deleteEmployeeById(id);
  }

  @Override
  public List<Employee> getEmployeesByName(String searchString) {
    logger.info("Search employees with name matching {}", searchString);

    List<Employee> employeesList = employeeDataSource.getAllEmployees();
    List<Employee> searchResult = employeesList
        .stream()
        .filter(employee -> employee.getName().contains(searchString)) // Requirement about matching
                                                                       // the case is not clear.
                                                                       // For case-insensitive
                                                                       // search, do
                                                                       // employee.getName().toLowerCase().contains(searchString.toLowerCase())
        .collect(toList());

    logger.info("Found {} employees", searchResult.size());
    return searchResult;
  }

  @Override
  public Integer getHighestSalary() {
    logger.info("Get highest salary among employees");

    List<Employee> employeesList = employeeDataSource.getAllEmployees();
    if (employeesList.isEmpty()) {
      logger.error("Employees list is empty");
      throw new EmployeeServiceException(HttpStatus.NOT_FOUND,
          ErrorCodes.EMPLOYEES_LIST_EMPTY.getCode(),
          "Employees list is empty, cannot return max salary");
    }

    Integer maxSalary =
        employeesList.stream().map(Employee::getSalary).max(Comparator.naturalOrder()).get();

    logger.info("Max salary = {}", maxSalary);
    return maxSalary;
  }

  @Override
  public List<Employee> getTopNBySalary(int count) {
    logger.info("Get top {} employees by salary", count);
    List<Employee> employeesList = employeeDataSource.getAllEmployees();

    List<Employee> topN = employeesList
        .stream()
        .sorted(comparing(Employee::getSalary).reversed())
        .limit(count)
        .collect(toList());

    logger.info("Fetched list of {} employees ordered by salary", topN.size());
    return topN;
  }

}

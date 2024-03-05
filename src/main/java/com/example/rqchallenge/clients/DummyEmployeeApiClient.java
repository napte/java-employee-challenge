package com.example.rqchallenge.clients;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import com.example.rqchallenge.employees.model.Employee;

@Component
public class DummyEmployeeApiClient {
  @Autowired
  private RestTemplate restTemplate;

  public List<Employee> getAllEmployees() {
    ResponseEntity<String> employeesResppnse = restTemplate
        .getForEntity("https://dummy.restapiexample.com/api/v1/employees", String.class);
    System.out.println(employeesResppnse.getBody());
    return null;
  }
}

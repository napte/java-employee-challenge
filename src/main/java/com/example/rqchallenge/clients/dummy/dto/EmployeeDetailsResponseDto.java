package com.example.rqchallenge.clients.dummy.dto;

public class EmployeeDetailsResponseDto extends BaseApiResponseDto {
  private EmployeeDto data;

  public EmployeeDto getData() {
    return data;
  }

  public void setData(EmployeeDto data) {
    this.data = data;
  }
}

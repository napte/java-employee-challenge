package com.example.rqchallenge.clients.dummy.dto;

import java.util.List;

public class EmployeesListResponseDto extends BaseApiResponseDto {
  private List<EmployeeDto> data;

  public List<EmployeeDto> getData() {
    return data;
  }

  public void setData(List<EmployeeDto> data) {
    this.data = data;
  }
}

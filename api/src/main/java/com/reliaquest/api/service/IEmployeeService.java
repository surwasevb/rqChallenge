package com.reliaquest.api.service;

import com.reliaquest.api.model.Employee;
import com.reliaquest.api.model.EmployeeCreateRequest;
import java.util.List;
import java.util.Optional;

public interface IEmployeeService {

    List<Employee> getAllEmployees();

    Optional<Employee> getEmployeeById(String id);

    List<Employee> getEmployeesByNameSearch(String searchString);

    Optional<Employee> createEmployee(EmployeeCreateRequest employeeCreateRequest);

    Optional<String> deleteEmployee(String id);

    Integer getHighestSalaryOfEmployees();

    List<String> getTopTenHighestEarningEmployeeNames();
}

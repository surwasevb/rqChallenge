package com.reliaquest.api.controller;

import static org.springframework.http.HttpStatus.*;

import com.reliaquest.api.model.Employee;
import com.reliaquest.api.model.EmployeeCreateRequest;
import com.reliaquest.api.service.IEmployeeService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/employee")
public class EmployeeController implements IEmployeeController<Employee, EmployeeCreateRequest> {

    private final Logger logger = LoggerFactory.getLogger(EmployeeController.class);

    private final IEmployeeService employeeService;

    public EmployeeController(IEmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @Override
    public ResponseEntity<List<Employee>> getAllEmployees() throws RuntimeException {
        logger.info("Received request for Getting all employees");
        List<Employee> employees = employeeService.getAllEmployees();
        logger.info("Returning all employees successfully");
        return ResponseEntity.ok(employees);
    }

    @Override
    public ResponseEntity<List<Employee>> getEmployeesByNameSearch(String searchString) {
        logger.info("Received request for Getting employees by name search: {}", searchString);
        List<Employee> employees = employeeService.getEmployeesByNameSearch(searchString);
        logger.info("Returning employees by name search successfully");
        return ResponseEntity.ok(employees);
    }

    @Override
    public ResponseEntity<Employee> getEmployeeById(String id) {
        logger.info("Received request for Getting employee by id: {}", id);
        return employeeService.getEmployeeById(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.status(
                        NOT_FOUND)
                .build());
    }

    @Override
    public ResponseEntity<Integer> getHighestSalaryOfEmployees() {
        logger.info("Received request for Getting highest salary of employees");
        Integer salary = employeeService.getHighestSalaryOfEmployees();
        logger.info("Returning highest salary of employees successfully: {}", salary);
        return ResponseEntity.ok(salary);
    }

    @Override
    public ResponseEntity<List<String>> getTopTenHighestEarningEmployeeNames() {
        logger.info("Received request for Getting top ten highest earning employee names");
        List<String> employeeNames = employeeService.getTopTenHighestEarningEmployeeNames();
        logger.info("Returning top ten highest earning employee names successfully");
        return ResponseEntity.ok(employeeNames);
    }

    @Override
    public ResponseEntity<Employee> createEmployee(@RequestBody @Valid EmployeeCreateRequest employeeCreateRequest) {
        logger.info("Received request for Creating employee: {}", employeeCreateRequest.getName());
        return employeeService
                .createEmployee(employeeCreateRequest)
                .map(e -> ResponseEntity.status(CREATED).body(e))
                .orElseGet(() -> ResponseEntity.status(INTERNAL_SERVER_ERROR).build());
    }

    @Override
    public ResponseEntity<String> deleteEmployeeById(String id) {
        logger.info("Received request for Deleting employee by id: {}", id);
        Optional<String> employeeName = employeeService.deleteEmployee(id);
        return employeeName.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.status(NOT_FOUND)
                .build());
    }
}

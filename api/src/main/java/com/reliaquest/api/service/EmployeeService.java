package com.reliaquest.api.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.reliaquest.api.exception.EmployeeNotFoundException;
import com.reliaquest.api.model.Employee;
import com.reliaquest.api.model.EmployeeCreateRequest;
import com.reliaquest.api.model.EmployeeResponse;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class EmployeeService implements IEmployeeService {

    private final Logger logger = LoggerFactory.getLogger(EmployeeService.class);

    @Value("${employeeEndpoint}")
    private String employeeEndpoint;

    private final RestTemplate restTemplate;

    private final ObjectMapper objectMapper;

    public EmployeeService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public List<Employee> getAllEmployees() {
        logger.info("Calling server apis to get all employees");
        EmployeeResponse employeeData = restTemplate
                .getForEntity(employeeEndpoint, EmployeeResponse.class)
                .getBody();

        if (employeeData == null) {
            logger.error("Failed to get employee information from server");
            throw new EmployeeNotFoundException("Failed to get employee information from server");
        }
        logger.info(
                "Received response containing all employees information:employee count: {}",
                employeeData.getData().size());

        return processResponse(employeeData);
    }

    @Override
    public Optional<Employee> getEmployeeById(String id) {
        logger.info("Calling server apis to get employee by id: {}", id);
        EmployeeResponse response = restTemplate
                .getForEntity(employeeEndpoint + "/{id}", EmployeeResponse.class, id)
                .getBody();

        if (response == null || response.getData() == null) {
            logger.error("Failed to get employee by id: {}", id);
            throw new EmployeeNotFoundException("Failed to get employee by id: " + id);
        }
        logger.info("Successfully received response containing employee information by id: {}", id);

        return Optional.of(objectMapper.convertValue(response.getData(), Employee.class));
    }

    @Override
    public List<Employee> getEmployeesByNameSearch(String searchString) {
        logger.info("Processing request to fetch employee by given name: {}", searchString);
        List<Employee> employeeList = getAllEmployees().stream()
                .filter(employee -> employee.getName().toLowerCase().contains(searchString.toLowerCase()))
                .toList();

        if (employeeList.isEmpty()) {
            logger.error("No employee found with given name: {}", searchString);
            throw new EmployeeNotFoundException("No employee found with given name: " + searchString);
        }

        return employeeList;
    }

    @Override
    public Optional<Employee> createEmployee(EmployeeCreateRequest employeeCreateRequest) {
        logger.info("Calling server apis to create employee with name: {}", employeeCreateRequest.getName());
        EmployeeResponse response = restTemplate
                .postForEntity(employeeEndpoint, employeeCreateRequest, EmployeeResponse.class)
                .getBody();

        if (response == null || response.getData() == null) {
            logger.error("Failed to create employee {}", employeeCreateRequest.getName());
            throw new RuntimeException("Failed to create employee");
        }

        return Optional.of(objectMapper.convertValue(response.getData(), Employee.class));
    }

    @Override
    public Optional<String> deleteEmployee(String id) {
        logger.info("Calling server apis to delete employee by id: {}", id);
        Optional<Employee> employee = getEmployeeById(id);
        if (employee.isPresent()) {
            logger.info(
                    "Employee found with name: {}, calling delete api to delete that employee",
                    employee.get().getName());
            HttpEntity<String> body =
                    new HttpEntity<>("{\"name\":\"" + employee.get().getName() + "\"}", new HttpHeaders() {
                        {
                            set("Content-Type", "application/json");
                        }
                    });

            JsonNode result = restTemplate
                    .exchange(employeeEndpoint, HttpMethod.DELETE, body, JsonNode.class)
                    .getBody();

            if (Optional.ofNullable(result).map(r -> r.get("data").asBoolean()).orElse(false)) {
                logger.info("Employee deleted successfully");
                return Optional.of(employee.get().getName());
            } else {
                logger.error("Failed to delete employee");
                throw new RuntimeException("Failed to delete employee");
            }
        } else {
            logger.error("Employee not found with id: {}", id);
            throw new EmployeeNotFoundException("Employee not found with id: " + id);
        }
    }

    @Override
    public Integer getHighestSalaryOfEmployees() {
        logger.info("Processing request to get highest salary of employees");
        return getAllEmployees().stream()
                .max(Comparator.comparingInt(Employee::getSalary))
                .map(Employee::getSalary)
                .orElse(null);
    }

    @Override
    public List<String> getTopTenHighestEarningEmployeeNames() {
        logger.info("Processing request to get top ten highest earning employee names");
        return getAllEmployees().stream()
                .sorted(Comparator.comparingInt(Employee::getSalary).reversed())
                .limit(10)
                .map(Employee::getName)
                .toList();
    }

    private List<Employee> processResponse(EmployeeResponse response) {
        logger.info("Processing response to list of employees data");
        List<Employee> data = objectMapper.convertValue(response.getData(), new TypeReference<List<Employee>>() {});
        logger.info("Successfully processed response to list of employees data: employee count: {}", data.size());
        return data;
    }
}

package com.reliaquest.api.controller;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD;

import com.reliaquest.api.model.Employee;
import com.reliaquest.api.model.EmployeeCreateRequest;
import com.reliaquest.api.service.IEmployeeService;
import java.util.*;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest
@DirtiesContext(classMode = AFTER_EACH_TEST_METHOD)
public class EmployeeControllerTest {

    @Mock
    private IEmployeeService employeeService;

    @InjectMocks
    private EmployeeController employeeController;

    @Test
    public void testGetAllEmployees() {

        List<Employee> employees = new ArrayList<>();
        for (String name : asList("Test Employee", "Test Employee 2", "Test Employee 3", "Test Employee 4")) {
            employees.add(getEmployee(name));
        }

        Mockito.when(employeeService.getAllEmployees()).thenReturn(employees);
        ResponseEntity<List<Employee>> responseEntity = employeeController.getAllEmployees();

        assertTrue(responseEntity.getStatusCode().is2xxSuccessful());
        assertEquals(4, Objects.requireNonNull(responseEntity.getBody()).size());
        assertArrayEquals(responseEntity.getBody().toArray(), employees.toArray());
    }

    @Test
    public void testGetEmployeesByNameSearch() {

        List<Employee> employees = new ArrayList<>();
        for (String name : asList("Test Employee", "Test Employee 2", "Test Employee 3", "Raju Employee")) {
            employees.add(getEmployee(name));
        }

        Mockito.when(employeeService.getEmployeesByNameSearch("Test Employee")).thenReturn(employees.subList(0, 3));
        ResponseEntity<List<Employee>> responseEntity = employeeController.getEmployeesByNameSearch("Test Employee");

        assertTrue(responseEntity.getStatusCode().is2xxSuccessful());
        assertEquals(3, Objects.requireNonNull(responseEntity.getBody()).size());
        assertArrayEquals(
                responseEntity.getBody().toArray(), employees.subList(0, 3).toArray());
    }

    @Test
    public void testGetEmployeeById() {

        Employee employee = getEmployee("Test Employee");

        Mockito.when(employeeService.getEmployeeById(employee.getId().toString()))
                .thenReturn(Optional.of(employee));
        ResponseEntity<Employee> responseEntity =
                employeeController.getEmployeeById(employee.getId().toString());

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertSame(responseEntity.getBody(), employee);
    }

    @Test
    public void testGetEmployeeByIdNotFound() {
        String employeeId = UUID.randomUUID().toString();
        Mockito.when(employeeService.getEmployeeById(employeeId)).thenReturn(Optional.empty());

        ResponseEntity<Employee> responseEntity = employeeController.getEmployeeById(employeeId);

        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
    }

    @Test
    public void testGetHighestSalaryOfEmployees() {

        Mockito.when(employeeService.getHighestSalaryOfEmployees()).thenReturn(495973);
        ResponseEntity<Integer> responseEntity = employeeController.getHighestSalaryOfEmployees();

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(495973, responseEntity.getBody());
    }

    @Test
    public void testGetTopTenHighestEarningEmployeeNames() {

        List<String> employees = IntStream.range(0, 10)
                .collect(ArrayList::new, (list, i) -> list.add("Test Employee " + i), ArrayList::addAll);

        Mockito.when(employeeService.getTopTenHighestEarningEmployeeNames()).thenReturn(employees);
        ResponseEntity<List<String>> responseEntity = employeeController.getTopTenHighestEarningEmployeeNames();

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(10, Objects.requireNonNull(responseEntity.getBody()).size());
        assertArrayEquals(responseEntity.getBody().toArray(), employees.toArray());
    }

    @Test
    public void testCreateEmployee() {

        Employee employee = getEmployee("Test Employee");
        EmployeeCreateRequest employeeCreateRequest = EmployeeCreateRequest.builder()
                .name("Test Employee")
                .age(35)
                .salary(100000)
                .title(" Software Engineer")
                .build();

        Mockito.when(employeeService.createEmployee(employeeCreateRequest)).thenReturn(Optional.of(employee));
        ResponseEntity<Employee> responseEntity = employeeController.createEmployee(employeeCreateRequest);

        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
        assertSame(employee, responseEntity.getBody());
    }

    @Test
    public void testDeleteEmployeeById() {
        Employee employee = getEmployee("Test Employee");

        Mockito.when(employeeService.deleteEmployee(employee.getId().toString()))
                .thenReturn(Optional.of(employee.getName()));
        ResponseEntity<String> responseEntity =
                employeeController.deleteEmployeeById(employee.getId().toString());

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertSame(employee.getName(), responseEntity.getBody());
    }

    @Test
    public void testDeleteEmployeeByIdNotFound() {
        Employee employee = getEmployee("Test Employee");

        Mockito.when(employeeService.deleteEmployee(employee.getId().toString()))
                .thenReturn(Optional.empty());
        ResponseEntity<String> responseEntity =
                employeeController.deleteEmployeeById(employee.getId().toString());

        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
    }

    private Employee getEmployee(String name) {
        return Employee.builder()
                .id(UUID.randomUUID())
                .name(name)
                .age(35)
                .salary(100000)
                .title("Software Engineer")
                .build();
    }
}

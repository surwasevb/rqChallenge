package com.reliaquest.api.service;

import static com.reliaquest.api.util.FileUtil.readEmployeeDataFromFile;
import static com.reliaquest.api.util.FileUtil.readSingleEmployeeResponseFromFile;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD;

import com.fasterxml.jackson.databind.JsonNode;
import com.reliaquest.api.exception.EmployeeNotFoundException;
import com.reliaquest.api.model.Employee;
import com.reliaquest.api.model.EmployeeCreateRequest;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = AFTER_EACH_TEST_METHOD)
public class EmployeeServiceTest {

    @Autowired
    private IEmployeeService employeeService;

    @Value("${employeeEndpoint}")
    private String employeeEndpoint;

    private static MockWebServer mockWebServer;

    @BeforeAll
    public static void setUp() throws Exception {
        mockWebServer = new MockWebServer();
        mockWebServer.start(8112);
    }

    @AfterAll
    public static void tearDown() throws Exception {
        mockWebServer.shutdown();
    }

    @Test
    void testShouldReturnAllEmployeesData() throws Exception {
        JsonNode employeeData = readEmployeeDataFromFile();

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(employeeData.toString()));

        List<Employee> employees = employeeService.getAllEmployees();
        RecordedRequest recordedRequest = mockWebServer.takeRequest();

        assertEquals("GET", recordedRequest.getMethod());
        assertEquals(employeeEndpoint, recordedRequest.getPath());
        assertEquals(50, employees.size());
        assertEquals("Carolyne Koss", employees.get(0).getName());
    }

    @Test
    void testShouldReturnEmployeeById() throws Exception {
        JsonNode employeeData = readSingleEmployeeResponseFromFile();

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(employeeData.toString()));

        Optional<Employee> employee = employeeService.getEmployeeById(
                employeeData.get("data").get("id").asText());
        RecordedRequest recordedRequest = mockWebServer.takeRequest();

        assertEquals("GET", recordedRequest.getMethod());
        assertEquals(employeeEndpoint + "/" + employee.get().getId(), recordedRequest.getPath());
        assertEquals("Carolyne Koss", employee.get().getName());
    }

    @Test
    void testShouldThrowErrorForReturnEmployeeById() throws Exception {
        String employeeId = UUID.randomUUID().toString();

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(""));

        EmployeeNotFoundException employeeNotFoundException =
                assertThrows(EmployeeNotFoundException.class, () -> employeeService.getEmployeeById(employeeId));
        RecordedRequest recordedRequest = mockWebServer.takeRequest();

        assertEquals("GET", recordedRequest.getMethod());
        assertEquals("Failed to get employee by id: " + employeeId, employeeNotFoundException.getMessage());
    }

    @Test
    void testShouldReturnEmployeeByNameSearch() throws Exception {
        JsonNode employeeData = readEmployeeDataFromFile();

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(employeeData.toString()));

        List<Employee> employees = employeeService.getEmployeesByNameSearch("Carolyne Koss");
        RecordedRequest recordedRequest = mockWebServer.takeRequest();

        assertEquals("GET", recordedRequest.getMethod());
        assertEquals(employeeEndpoint, recordedRequest.getPath());
        assertEquals(1, employees.size());
        assertEquals("Carolyne Koss", employees.get(0).getName());
    }

    @Test
    void testShouldCreateEmployee() throws Exception {
        JsonNode employeeData = readSingleEmployeeResponseFromFile();
        EmployeeCreateRequest employeeCreateRequest = EmployeeCreateRequest.builder()
                .name(employeeData.get("data").get("employee_name").asText())
                .age(employeeData.get("data").get("employee_age").asInt())
                .salary(employeeData.get("data").get("employee_salary").asInt())
                .title(employeeData.get("data").get("employee_title").asText())
                .build();

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(employeeData.toString()));

        Optional<Employee> employee = employeeService.createEmployee(employeeCreateRequest);
        RecordedRequest recordedRequest = mockWebServer.takeRequest();

        assertEquals("POST", recordedRequest.getMethod());
        assertEquals(employeeEndpoint, recordedRequest.getPath());
        assertEquals(employeeCreateRequest.getName(), employee.get().getName());
    }

    @Test
    void testShouldThrowErrorCreateEmployee() throws Exception {
        JsonNode employeeData = readSingleEmployeeResponseFromFile();
        EmployeeCreateRequest employeeCreateRequest = EmployeeCreateRequest.builder()
                .name(employeeData.get("data").get("employee_name").asText())
                .age(employeeData.get("data").get("employee_age").asInt())
                .salary(employeeData.get("data").get("employee_salary").asInt())
                .title(employeeData.get("data").get("employee_title").asText())
                .build();

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(400)
                .setHeader("Content-Type", "application/json")
                .setBody(""));

        RuntimeException runtimeException =
                assertThrows(RuntimeException.class, () -> employeeService.createEmployee(employeeCreateRequest));
        RecordedRequest recordedRequest = mockWebServer.takeRequest();

        assertEquals("POST", recordedRequest.getMethod());
        assertEquals(employeeEndpoint, recordedRequest.getPath());
        assertTrue(runtimeException.getMessage().contains("Bad Request"));
    }

    @Test
    void testShouldThrowErrorEmptyResponseCreateEmployee() throws Exception {
        JsonNode employeeData = readSingleEmployeeResponseFromFile();
        EmployeeCreateRequest employeeCreateRequest = EmployeeCreateRequest.builder()
                .name(employeeData.get("data").get("employee_name").asText())
                .age(employeeData.get("data").get("employee_age").asInt())
                .salary(employeeData.get("data").get("employee_salary").asInt())
                .title(employeeData.get("data").get("employee_title").asText())
                .build();

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(""));

        RuntimeException runtimeException =
                assertThrows(RuntimeException.class, () -> employeeService.createEmployee(employeeCreateRequest));
        RecordedRequest recordedRequest = mockWebServer.takeRequest();

        assertEquals("POST", recordedRequest.getMethod());
        assertEquals(employeeEndpoint, recordedRequest.getPath());
        assertTrue(runtimeException.getMessage().contains("Failed to create employee"));
    }

    @Test
    void testShouldDeleteEmployeeById() throws Exception {
        JsonNode employeeData = readSingleEmployeeResponseFromFile();

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(employeeData.toString()));
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("{\"data\": true, \"status\":\"success\"}"));

        String employeeId = employeeData.get("data").get("id").asText();
        Optional<String> response = employeeService.deleteEmployee(employeeId);
        RecordedRequest recordedRequest1 = mockWebServer.takeRequest();
        RecordedRequest recordedRequest2 = mockWebServer.takeRequest();

        assertEquals("DELETE", recordedRequest2.getMethod());
        assertEquals("GET", recordedRequest1.getMethod());
        assertEquals(employeeEndpoint, recordedRequest2.getPath());
        assertEquals(employeeEndpoint + "/" + employeeId, recordedRequest1.getPath());
        assertEquals("{\"name\":\"Carolyne Koss\"}", recordedRequest2.getBody().readUtf8());
        assertEquals(employeeData.get("data").get("employee_name").asText(), response.get());
    }

    @Test
    void testShouldThrowErrorForDeleteEmployeeById() throws Exception {
        JsonNode employeeData = readSingleEmployeeResponseFromFile();

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(employeeData.toString()));
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("{\"data\": false, \"status\":\"success\"}"));

        String employeeId = employeeData.get("data").get("id").asText();
        RuntimeException runtimeException =
                assertThrows(RuntimeException.class, () -> employeeService.deleteEmployee(employeeId));
        RecordedRequest recordedRequest1 = mockWebServer.takeRequest();
        RecordedRequest recordedRequest2 = mockWebServer.takeRequest();

        assertEquals("DELETE", recordedRequest2.getMethod());
        assertEquals("GET", recordedRequest1.getMethod());
        assertEquals(employeeEndpoint, recordedRequest2.getPath());
        assertEquals(employeeEndpoint + "/" + employeeId, recordedRequest1.getPath());
        assertEquals("{\"name\":\"Carolyne Koss\"}", recordedRequest2.getBody().readUtf8());
        assertEquals("Failed to delete employee", runtimeException.getMessage());
    }

    @Test
    void testShouldThrowEmployeeNotFoundExceptionForUnknownEmployeeDeletionById() throws Exception {
        String employeeId = UUID.randomUUID().toString();
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(404)
                .setHeader("Content-Type", "application/json")
                .setBody(""));

        EmployeeNotFoundException employeeNotFoundException =
                assertThrows(EmployeeNotFoundException.class, () -> employeeService.deleteEmployee(employeeId));
        RecordedRequest recordedRequest1 = mockWebServer.takeRequest();

        assertEquals("Employee not found", employeeNotFoundException.getMessage());
        assertEquals("GET", recordedRequest1.getMethod());
        assertEquals(employeeEndpoint + "/" + employeeId, recordedRequest1.getPath());
    }

    @Test
    void testShouldReturnHighestSalaryOfEmployees() throws Exception {
        JsonNode employeeData = readEmployeeDataFromFile();

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(employeeData.toString()));

        Integer salary = employeeService.getHighestSalaryOfEmployees();
        RecordedRequest recordedRequest = mockWebServer.takeRequest();

        assertEquals(495973, salary);
        assertEquals("GET", recordedRequest.getMethod());
        assertEquals(employeeEndpoint, recordedRequest.getPath());
    }

    @Test
    void testShouldReturnTopTenHighestEarningEmployeeNames() throws Exception {
        JsonNode employeeData = readEmployeeDataFromFile();

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(employeeData.toString()));

        List<String> employees = employeeService.getTopTenHighestEarningEmployeeNames();
        RecordedRequest recordedRequest = mockWebServer.takeRequest();

        assertEquals(10, employees.size());
        assertTrue(employees.contains("Miss Lorraine Swaniawski"));
        assertEquals("GET", recordedRequest.getMethod());
        assertEquals(employeeEndpoint, recordedRequest.getPath());
    }
}

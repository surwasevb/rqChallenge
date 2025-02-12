package com.reliaquest.api;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.reliaquest.api.util.ApiConstants.*;
import static com.reliaquest.api.util.FileUtil.readEmployeeDataFromFile;
import static com.reliaquest.api.util.FileUtil.readSingleEmployeeResponseFromFile;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.reliaquest.api.model.EmployeeCreateRequest;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;

@SpringBootTest
@AutoConfigureMockMvc
@EnableWireMock(@ConfigureWireMock(name = "localhost", port = 8112))
@DirtiesContext(classMode = AFTER_EACH_TEST_METHOD)
public class ApiApplicationTest {

    @Autowired
    private MockMvc mockMvc;

    @Value("${employeeEndpoint}")
    private String employeeEndpoint;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testShouldReturn200FromEmployeesDataEndpoint() throws Exception {
        JsonNode employeeData = readEmployeeDataFromFile();

        stubFor(WireMock.get(urlEqualTo(employeeEndpoint))
                .willReturn(ok().withHeader("Content-Type", "application/json").withBody(employeeData.toString())));

        mockMvc.perform(get(GET_EMPLOYEE_URI).accept("application/json"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", Matchers.hasSize(50)));
    }

    @Test
    public void testShouldReturn200FromHighestSalaryEndpoint() throws Exception {
        JsonNode employeeData = readEmployeeDataFromFile();

        stubFor(WireMock.get(urlEqualTo(employeeEndpoint))
                .willReturn(ok().withHeader("Content-Type", "application/json").withBody(employeeData.toString())));

        mockMvc.perform(get(GET_HIGHEST_SALARY_URI))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", Matchers.equalTo(495973)));
    }

    @Test
    public void testShouldReturn200FromTopTenHighestEarningEmployeeNamesEndpoint() throws Exception {
        JsonNode employeeData = readEmployeeDataFromFile();

        stubFor(WireMock.get(urlEqualTo(employeeEndpoint))
                .willReturn(ok().withHeader("Content-Type", "application/json").withBody(employeeData.toString())));

        mockMvc.perform(get(GET_TOP_TEN_EMPLOYEE_NAMES_URI))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", Matchers.hasSize(10)));
    }

    @Test
    public void testShouldReturn200FromEmployeeByIdEndpoint() throws Exception {
        JsonNode employeeData = readSingleEmployeeResponseFromFile();
        JsonNode employee = employeeData.get("data");
        stubFor(WireMock.get(
                        urlEqualTo(employeeEndpoint + "/" + employee.get("id").asText()))
                .willReturn(ok().withHeader("Content-Type", "application/json").withBody(employeeData.toString())));

        mockMvc.perform(get(GET_EMPLOYEE_ID_URI, employee.get("id").asText()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath(
                        "$['employee_name']",
                        Matchers.equalTo(employee.get("employee_name").asText())));
    }

    @Test
    public void testShouldReturn200FromEmployeesByNameSearchEndpoint() throws Exception {
        JsonNode employeeData = readEmployeeDataFromFile();
        String employeeName = "Mills";

        stubFor(WireMock.get(urlEqualTo(employeeEndpoint))
                .willReturn(ok().withHeader("Content-Type", "application/json").withBody(employeeData.toString())));

        mockMvc.perform(get(GET_EMPLOYEE_BY_NAME_URI, employeeName))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", Matchers.hasSize(2)));
    }

    @Test
    public void testShouldReturn404ForUnknownEmployeeSearch() throws Exception {
        JsonNode employeeData = readEmployeeDataFromFile();
        String employeeName = "unknown employee";

        stubFor(WireMock.get(urlEqualTo(employeeEndpoint))
                .willReturn(ok().withHeader("Content-Type", "application/json").withBody(employeeData.toString())));

        mockMvc.perform(get(GET_EMPLOYEE_BY_NAME_URI, employeeName))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath(
                        "$.['message']", Matchers.equalTo("No employee found with given name: " + employeeName)));
    }

    @Test
    public void testShouldReturn200FromCreateEmployeeEndpoint() throws Exception {
        JsonNode employeeData = readSingleEmployeeResponseFromFile();
        EmployeeCreateRequest employeeCreateRequest = EmployeeCreateRequest.builder()
                .name(employeeData.get("data").get("employee_name").asText())
                .salary(employeeData.get("data").get("employee_salary").asInt())
                .age(employeeData.get("data").get("employee_age").asInt())
                .title(employeeData.get("data").get("employee_title").asText())
                .build();

        stubFor(WireMock.post(urlEqualTo(employeeEndpoint))
                .willReturn(ok().withHeader("Content-Type", "application/json").withBody(employeeData.toString())));

        mockMvc.perform(post(GET_EMPLOYEE_URI)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(employeeCreateRequest)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$['employee_name']", Matchers.equalTo(employeeCreateRequest.getName())));
    }

    @Test
    public void testShouldReturn200FromDeleteEmployeeEndpoint() throws Exception {
        JsonNode employeeData = readSingleEmployeeResponseFromFile();
        JsonNode employee = employeeData.get("data");
        JsonNode result = objectMapper.createObjectNode().put("data", true).put("status", "Successfully deleted");

        stubFor(WireMock.get(
                        urlEqualTo(employeeEndpoint + "/" + employee.get("id").asText()))
                .willReturn(ok().withHeader("Content-Type", "application/json").withBody(employeeData.toString())));

        stubFor(delete(urlEqualTo(employeeEndpoint))
                .withRequestBody(equalToJson(
                        "{\"name\":\"" + employee.get("employee_name").asText() + "\"}"))
                .willReturn(ok().withHeader("Content-Type", "application/json").withBody(result.toString())));

        mockMvc.perform(delete(GET_EMPLOYEE_ID_URI, employee.get("id").asText()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath(
                        "$", Matchers.equalTo(employee.get("employee_name").asText())));
    }

    @Test
    public void testShouldReturn404OnEmptyGetAllEmployeesData() throws Exception {
        stubFor(WireMock.get(urlEqualTo(employeeEndpoint)).willReturn(ok()));

        mockMvc.perform(get(GET_EMPLOYEE_URI).accept("application/json"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(
                        jsonPath("$['message']", Matchers.equalTo("Failed to get employee information from server")));
    }

    @Test
    public void testShouldReturn429FromGetAllEmployeesDataEndpoint() throws Exception {
        stubFor(WireMock.get(urlEqualTo(employeeEndpoint))
                .willReturn(ResponseDefinitionBuilder.responseDefinition()
                        .withStatus(HttpStatus.TOO_MANY_REQUESTS.value())
                        .withBody("Too many requests to server")));

        mockMvc.perform(get(GET_EMPLOYEE_URI).accept("application/json"))
                .andDo(print())
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$['message']", Matchers.equalTo("Too many requests to server")));
    }

    @Test
    public void testShouldReturn500FromGetAllEmployeesDataEndpoint() throws Exception {
        stubFor(WireMock.get(urlEqualTo(employeeEndpoint))
                .willReturn(ResponseDefinitionBuilder.responseDefinition()
                        .withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .withBody("Internal Server Error")));

        mockMvc.perform(get(GET_EMPLOYEE_URI).accept("application/json"))
                .andDo(print())
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$['message']", Matchers.equalTo("Internal Server Error")));
    }

    @Test
    public void testShouldReturn400FromCreateEmployeeEndpoint() throws Exception {

        mockMvc.perform(post(GET_EMPLOYEE_URI)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Test Employee\",\"salary\":100000,\"title\":\"Software Engineer\"}"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$['message']", Matchers.equalTo("age : must not be null")));
    }
}

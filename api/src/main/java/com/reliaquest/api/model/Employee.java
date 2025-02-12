package com.reliaquest.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Employee {

    @JsonProperty("id")
    private UUID id;

    @JsonProperty("employee_name")
    private String name;

    @JsonProperty("employee_salary")
    private Integer salary;

    @JsonProperty("employee_age")
    private int age;

    @JsonProperty("employee_title")
    private String title;

    @JsonProperty("employee_email")
    private String email;
}

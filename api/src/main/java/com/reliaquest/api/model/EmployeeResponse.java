package com.reliaquest.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class EmployeeResponse {

    private final String status;
    private final JsonNode data;
}

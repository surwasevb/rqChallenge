package com.reliaquest.api.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;

public class FileUtil {

    public static JsonNode readEmployeeDataFromFile() throws IOException {
        File file = new File("../api/src/test/resources/employee-all-data.json");
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(file, JsonNode.class);
    }

    public static JsonNode readSingleEmployeeResponseFromFile() throws IOException {
        File file = new File("../api/src/test/resources/employee-data.json");
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(file, JsonNode.class);
    }
}

package com.genderapi.stepdefinitions;

import io.cucumber.java.en.*;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.datatable.DataTable;

import static org.junit.Assert.*;

import java.net.*;
import java.io.*;
import java.util.Map;
import java.util.List;

import com.fasterxml.jackson.databind.*;
import com.genderapi.utils.*;
import com.github.tomakehurst.wiremock.WireMockServer;

public class GenderizeSteps {

    private String response;
    private String name;
    private String country;
    private String[] names;
    private WireMockServer mockServer;
    private JsonNode responseNode;
    private int statusCode;

    public GenderizeSteps() {
        // No-arg constructor required by Cucumber
    }

    @Before
    public void setup() {
        mockServer = MockServer.startMockServer();
    }

    @Given("I have the name {string}")
    public void i_have_the_name(String name) {
        this.name = name;
        this.names = null;
        this.country = null;
    }

    @Given("I have the name {string} and country {string}")
    public void i_have_name_and_country(String name, String country) {
        this.name = name;
        this.country = country;
        this.names = null;
    }

    @Given("I have multiple names {string}")
    public void i_have_multiple_names(String namesCsv) {
        this.names = namesCsv.split(",\\s*");
        this.name = null;
        this.country = null;
    }

    @When("I send a request to Genderize API")
    public void i_send_request() throws Exception {
        String baseUrl = "http://localhost:8080";
        String urlStr;
        URL url;
        HttpURLConnection conn;

        try {
            if (names != null) {
                urlStr = baseUrl + "?" + String.join("&",
                    java.util.Arrays.stream(names)
                        .map(n -> {
                            try {
                                return "name[]=" + URLEncoder.encode(n, "UTF-8");
                            } catch (UnsupportedEncodingException e) {
                                throw new RuntimeException("Error encoding URL", e);
                            }
                        })
                        .toArray(String[]::new));
            } else if (country != null) {
                urlStr = baseUrl + "?name=" + URLEncoder.encode(name, "UTF-8") 
                        + "&country_id=" + country;
            } else {
                urlStr = baseUrl + "?name=" + URLEncoder.encode(name, "UTF-8");
            }

            url = new URL(urlStr);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            
            statusCode = conn.getResponseCode();
            
            // Handle different status codes appropriately
            InputStream inputStream;
            if (statusCode == 404 || statusCode == 422 || statusCode == 400) {
                inputStream = conn.getErrorStream();
            } else {
                inputStream = conn.getInputStream();
            }
            
            BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
            response = in.lines().reduce("", (acc, line) -> acc + line);
            in.close();
            
            // For error responses that are not in JSON format, create a JSON error response
            if ((statusCode == 404 || statusCode == 422 || statusCode == 400) && !response.trim().startsWith("{")) {
                response = "{\"error\":\"" + response.replace("\"", "\\\"") + "\"}";
            }
            
            // Parse response for assertions
            ObjectMapper mapper = new ObjectMapper();
            responseNode = mapper.readTree(response);
            
        } catch (Exception e) {
            // Create an error response for any exceptions
            response = "{\"error\":\"" + e.getMessage().replace("\"", "\\\"") + "\"}";
            responseNode = new ObjectMapper().readTree(response);
        }
    }

    @Then("the response should contain:")
    public void verify_response_fields(DataTable dataTable) {
        List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
        
        for (Map<String, String> row : rows) {
            String fieldName = row.get("field");
            String expectedValue = row.get("value");
            
            JsonNode actualNode = responseNode.get(fieldName);
            String actualValue = actualNode != null ? 
                (actualNode.isTextual() ? actualNode.asText() : actualNode.toString()) : 
                null;
            
            // Handle empty string values
            if (expectedValue.equals("\"\"")) {
                assertTrue("Expected empty string but got: " + actualValue,
                    actualValue == null || actualValue.isEmpty() || actualValue.equals("\"\""));
                continue;
            }
            
            // Handle numeric values
            if (expectedValue.matches("-?\\d+(\\.\\d+)?")) {
                if (actualNode != null && actualNode.isNumber()) {
                    assertEquals(
                        Double.parseDouble(expectedValue),
                        actualNode.asDouble(),
                        0.001
                    );
                    continue;
                }
            }
            
            // Handle boolean/null values
            if ("null".equalsIgnoreCase(expectedValue)) {
                assertTrue(actualNode == null || actualNode.isNull());
                continue;
            }
            
            // Handle error messages and string values
            assertEquals(
                "Field '" + fieldName + "' mismatch",
                expectedValue, 
                actualValue
            );
        }
    }

    @Then("the response should contain {int} predictions")
    public void verify_prediction_count(int count) {
        assertTrue(responseNode.isArray());
        assertEquals(count, responseNode.size());
    }

    @Then("prediction {int} should contain:")
    public void verify_prediction_fields(int index, DataTable dataTable) {
        JsonNode prediction = responseNode.get(index - 1);
        List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
        
        for (Map<String, String> row : rows) {
            String fieldName = row.get("field");
            String expectedValue = row.get("value");
            
            JsonNode actualNode = prediction.get(fieldName);
            String actualValue = actualNode != null ? 
                (actualNode.isTextual() ? actualNode.asText() : actualNode.toString()) : 
                null;
            
            // Handle numeric values
            if (expectedValue.matches("-?\\d+(\\.\\d+)?")) {
                if (actualNode != null && actualNode.isNumber()) {
                    assertEquals(
                        Double.parseDouble(expectedValue),
                        actualNode.asDouble(),
                        0.001
                    );
                    continue;
                }
            }
            
            // Handle boolean/null values
            if ("null".equalsIgnoreCase(expectedValue)) {
                assertTrue(actualNode == null || actualNode.isNull());
                continue;
            }
            
            // Handle string values
            assertEquals(
                "Field '" + fieldName + "' mismatch",
                expectedValue, 
                actualValue
            );
        }
    }

    @After
    public void cleanup() {
        if (mockServer != null && mockServer.isRunning()) {
            mockServer.stop();
        }
    }
}
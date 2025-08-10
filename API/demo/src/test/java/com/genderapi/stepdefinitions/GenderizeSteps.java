package com.genderapi.stepdefinitions;

import io.cucumber.java.en.*;
import io.cucumber.java.After;
import io.cucumber.java.Before;

import static org.junit.Assert.*;

import java.net.*;
import java.io.*;
import java.util.List;

import com.fasterxml.jackson.databind.*;
import com.genderapi.utils.MockServer;
import com.github.tomakehurst.wiremock.WireMockServer;

public class GenderizeSteps {

    private String response;
    private String name;
    private String country;
    private String[] names;
    private WireMockServer mockServer;

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

    @Given("I have an empty name")
    public void i_have_empty_name() {
        this.name = "";
        this.names = null;
        this.country = null;
    }

    @io.cucumber.java.Before
    public void setup() {
        mockServer = MockServer.startMockServer();
    }

    @When("I send a request to Genderize API")
    public void i_send_request() throws Exception {
        String baseUrl = "http://localhost:8080";

        String urlStr;
        if (names != null) {
            urlStr = baseUrl + "?" + String.join("&",
                java.util.Arrays.stream(names)
                    .map(n -> "name[]=" + n)
                    .toArray(String[]::new));
        } else if (country != null) {
            urlStr = baseUrl + "?name=" + URLEncoder.encode(name, "UTF-8") + "&country_id=" + country;
        } else {
            urlStr = baseUrl + "?name=" + URLEncoder.encode(name, "UTF-8");
        }

        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        response = in.lines().reduce("", (acc, line) -> acc + line);
        in.close();
    }

    @Then("the response should contain gender {string}")
    public void response_should_contain_gender(String expectedGender) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(response);
        assertEquals(expectedGender, root.get("gender").asText());
    }

    @Then("the response should contain genders for all names")
    public void response_should_contain_genders_for_all_names() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(response);
        assertTrue(root.isArray());
        assertEquals(names.length, root.size());
    }

    @Then("the response should return an error or empty result")
    public void response_should_return_error_or_empty() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(response);
        assertTrue(root.get("gender") == null || root.get("gender").isNull());
    }

    @After
    public void cleanup() {
        if (mockServer != null && mockServer.isRunning()) {
            mockServer.stop();
            mockServer.resetAll();
        }
    }
    public void tearDown() {
        if (mockServer != null) {
            mockServer.stop();
        }
    }
}

package com.genderapi.utils;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import com.github.tomakehurst.wiremock.WireMockServer;

public class MockServer {
    public static WireMockServer startMockServer() {
        System.out.println("Starting mock server...");
        WireMockServer wireMockServer = new WireMockServer(8080);
        wireMockServer.start();
        configureFor("localhost", 8080);
        System.out.println("Mock server started successfully.");

        // 🔹 Stub for single name
        wireMockServer.stubFor(get(urlPathMatching(".*"))
            .withQueryParam("name", equalTo("Emily"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"name\":\"Emily\",\"gender\":\"female\",\"probability\":0.99,\"count\":1000}")));

        // 🔹 Stub for name + country (IT)
        wireMockServer.stubFor(get(urlPathMatching(".*"))
            .withQueryParam("name", equalTo("Andrea"))
            .withQueryParam("country_id", equalTo("IT"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"name\":\"Andrea\",\"gender\":\"male\",\"probability\":0.85,\"count\":500}")));

        // 🔹 Stub for name + country (US)
        wireMockServer.stubFor(get(urlPathMatching(".*"))
            .withQueryParam("name", equalTo("Andrea"))
            .withQueryParam("country_id", equalTo("US"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"name\":\"Andrea\",\"gender\":\"female\",\"probability\":0.88,\"count\":600}")));

        // 🔹 Stub for empty name
        wireMockServer.stubFor(get(urlPathMatching(".*"))
            .withQueryParam("name", equalTo(""))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"name\":\"\",\"gender\":null,\"probability\":null,\"count\":0}")));

        // 🔹 Stub for multiple names
        wireMockServer.stubFor(get(urlPathMatching(".*"))
            .withQueryParam("name[]", equalTo("John"))
            .withQueryParam("name[]", equalTo("Sarah"))
            .withQueryParam("name[]", equalTo("Alex"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("[{\"name\":\"John\",\"gender\":\"male\",\"probability\":0.98,\"count\":1200}," +
                         "{\"name\":\"Sarah\",\"gender\":\"female\",\"probability\":0.97,\"count\":1100}," +
                         "{\"name\":\"Alex\",\"gender\":\"male\",\"probability\":0.85,\"count\":900}]")));

        // Default stub for any unmatched request
        wireMockServer.stubFor(get(urlPathMatching(".*"))
            .atPriority(10)
            .willReturn(aResponse()
                .withStatus(500)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"error\":\"No matching mock response found\"}")));

        return wireMockServer;
    }
}

package com.genderapi.utils;

import com.github.tomakehurst.wiremock.WireMockServer;
import static com.github.tomakehurst.wiremock.client.WireMock.*;

public class MockServer {

    public static WireMockServer startMockServer() {
        WireMockServer wireMockServer = new WireMockServer(8080);
        wireMockServer.start();
        
        // Single name response (Peter)
        wireMockServer.stubFor(get(urlPathEqualTo("/"))
            .withQueryParam("name", equalTo("peter"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody("{\"count\":1094417,\"name\":\"peter\",\"gender\":\"male\",\"probability\":1}")));
        
        // Country-specific responses (Kim)
        wireMockServer.stubFor(get(urlPathEqualTo("/"))
            .withQueryParam("name", equalTo("kim"))
            .withQueryParam("country_id", equalTo("US"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody("{\"count\":28672,\"name\":\"kim\",\"country_id\":\"US\",\"gender\":\"female\",\"probability\":0.95}")));
        
        wireMockServer.stubFor(get(urlPathEqualTo("/"))
            .withQueryParam("name", equalTo("kim"))
            .withQueryParam("country_id", equalTo("DK"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody("{\"count\":3626,\"name\":\"kim\",\"country_id\":\"DK\",\"gender\":\"male\",\"probability\":0.96}")));
        
        // Batch response
        wireMockServer.stubFor(get(urlPathEqualTo("/"))
            .withQueryParam("name[]", containing("peter"))
            .withQueryParam("name[]", containing("lois"))
            .withQueryParam("name[]", containing("stewie"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody("[{\"count\":1094417,\"name\":\"peter\",\"gender\":\"male\",\"probability\":1}," +
                          "{\"count\":50141,\"name\":\"lois\",\"gender\":\"female\",\"probability\":0.98}," +
                          "{\"count\":361,\"name\":\"stewie\",\"gender\":\"male\",\"probability\":0.94}]")));
        
        // Edge cases
        // Empty name
        wireMockServer.stubFor(get(urlPathEqualTo("/"))
            .withQueryParam("name", equalTo(""))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody("{\"name\":\"\",\"gender\":null,\"probability\":0.0,\"count\":0}")));

        // Invalid name (peter2)
        wireMockServer.stubFor(get(urlPathEqualTo("/"))
            .withQueryParam("name", equalTo("peter2"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody("{\"name\":\"peter2\",\"gender\":null,\"probability\":0.0,\"count\":0}")));

        // Invalid country ID
        wireMockServer.stubFor(get(urlPathEqualTo("/"))
            .withQueryParam("name", equalTo("kim"))
            .withQueryParam("country_id", equalTo("US2"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody("{\"name\":\"kim\",\"country_id\":\"US2\",\"gender\":null,\"probability\":0.0,\"count\":0}")));

        // Numeric name
        wireMockServer.stubFor(get(urlPathEqualTo("/"))
            .withQueryParam("name", equalTo("234"))
            .willReturn(aResponse()
                .withStatus(404)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"error\":\"Uh oh. 234 is unknown to us\"}")));
        
        return wireMockServer;
    }
}
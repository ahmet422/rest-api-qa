package com.mindtek.bookstore.wiremock;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.testng.Assert.assertEquals;

import com.github.tomakehurst.wiremock.WireMockServer;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** Demonstrates WireMock for stubbing HTTP dependencies in isolated tests (Course Reference). */
public class WireMockBasicsTest {

  private WireMockServer wireMockServer;

  @BeforeMethod
  public void startServer() {
    wireMockServer = new WireMockServer(0);
    wireMockServer.start();
  }

  @AfterMethod
  public void stopServer() {
    if (wireMockServer != null) {
      wireMockServer.stop();
    }
  }

  @Test
  public void stubsSimpleGetResponse() throws Exception {
    wireMockServer.stubFor(
        get(urlEqualTo("/health"))
            .willReturn(aResponse().withStatus(200).withBody("{\"status\":\"UP\"}")));

    HttpClient client = HttpClient.newHttpClient();
    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(URI.create(wireMockServer.baseUrl() + "/health"))
            .GET()
            .build();

    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

    assertEquals(response.statusCode(), 200);
    assertEquals(response.body(), "{\"status\":\"UP\"}");
  }
}

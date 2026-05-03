package com.mindtek.bookstore.api;

import io.restassured.RestAssured;
import java.nio.file.Files;
import java.nio.file.Path;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** Writes OpenAPI JSON to target/openapi.json for Spectral linting in CI. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class OpenApiExportIT extends AbstractTestNGSpringContextTests {

  @LocalServerPort private int port;

  @BeforeClass(alwaysRun = true)
  public void setupRestAssured() {
    RestAssured.port = port;
    RestAssured.baseURI = "http://localhost";
  }

  @Test
  public void exportOpenApiDocumentToTarget() throws Exception {
    String json =
        RestAssured.given()
            .when()
            .get("/v3/api-docs")
            .then()
            .statusCode(200)
            .extract()
            .body()
            .asString();

    Path out = Path.of("target", "openapi.json");
    Files.createDirectories(out.getParent());
    Files.writeString(out, json);
  }
}

package com.mindtek.bookstore.api;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.Map;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class BookApiIT extends AbstractTestNGSpringContextTests {

    ///  comment

    @LocalServerPort
    private int port;

    @BeforeClass(alwaysRun = true)
    public void restAssuredPort() {
        RestAssured.port = port;
        RestAssured.baseURI = "http://localhost";
    }

    private static String adminToken() {
        return given()
                .contentType(ContentType.JSON)
                .body(Map.of("username", "admin", "password", "password"))
                .when()
                .post("/api/login")
                .then()
                .statusCode(200)
                .extract()
                .path("accessToken");
    }

    private static String userToken() {
        return given()
                .contentType(ContentType.JSON)
                .body(Map.of("username", "user", "password", "password"))
                .when()
                .post("/api/login")
                .then()
                .statusCode(200)
                .extract()
                .path("accessToken");
    }

    @Test
    public void loginRejectsBadPassword() {
        given().contentType(ContentType.JSON)
                .body(Map.of("username", "admin", "password", "wrong"))
                .when()
                .post("/api/login")
                .then()
                .statusCode(401)
                .body("code", equalTo("UNAUTHORIZED"))
                .body("requestId", notNullValue());
    }

    @Test
    public void listBooksRequiresAuth() {
        given().when().get("/api/books").then().statusCode(401);
    }

    @Test
    public void listBooksPaginatedAndFilter() {
        String token = adminToken();
        given().header("Authorization", "Bearer " + token)
                .when()
                .get("/api/books?page=0&size=5&category=Fiction")
                .then()
                .statusCode(200)
                .body("content", hasSize(greaterThan(0)))
                .body("totalElements", greaterThan(0));
    }

    @Test
    public void listBooksFilterByLanguage() {
        String token = adminToken();
        given().header("Authorization", "Bearer " + token)
                .when()
                .get("/api/books?language=Spanish&category=Fiction")
                .then()
                .statusCode(200)
                .body("content", hasSize(greaterThan(0)))
                .body("content[0].language", equalTo("Spanish"));
    }

    @Test
    public void invalidPageParameterType() {
        String token = adminToken();
        given().header("Authorization", "Bearer " + token)
                .when()
                .get("/api/books?page=abc")
                .then()
                .statusCode(400)
                .body("code", equalTo("BAD_REQUEST"))
                .body("fieldErrors", hasSize(0));
    }

    @Test
    public void pageBeyondLastReturnsEmptyContent() {
        String token = adminToken();
        int totalPages =
                given().header("Authorization", "Bearer " + token)
                        .when()
                        .get("/api/books?page=0&size=100")
                        .then()
                        .statusCode(200)
                        .extract()
                        .path("totalPages");
        given().header("Authorization", "Bearer " + token)
                .when()
                .get("/api/books?page=" + (totalPages + 5) + "&size=10")
                .then()
                .statusCode(200)
                .body("content", hasSize(0));
    }

    @Test
    public void userCannotDeleteBook() {
        int id =
                given().header("Authorization", "Bearer " + adminToken())
                        .when()
                        .get("/api/books?page=0&size=1")
                        .then()
                        .statusCode(200)
                        .extract()
                        .path("content[0].id");
        String token = userToken();
        given().header("Authorization", "Bearer " + token)
                .when()
                .delete("/api/books/" + id)
                .then()
                .statusCode(403)
                .body("code", equalTo("FORBIDDEN"));
    }

    @Test
    public void putIsIdempotent() {
        String token = adminToken();
        String create =
                """
                {
                  "title": "Put Idempotency",
                  "author": "Author",
                  "isbn": "8888888888888",
                  "price": 12.00,
                  "stock": 2,
                  "category": "Fiction",
                  "language": "English"
                }
                """;
        int id =
                given().header("Authorization", "Bearer " + token)
                        .contentType(ContentType.JSON)
                        .body(create)
                        .when()
                        .post("/api/books")
                        .then()
                        .statusCode(201)
                        .extract()
                        .path("id");
        String replace =
                """
                {
                  "title": "Put Idempotency Updated",
                  "author": "Author",
                  "isbn": "8888888888888",
                  "price": 15.00,
                  "stock": 3,
                  "category": "Fiction",
                  "language": "English"
                }
                """;
        given().header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(replace)
                .when()
                .put("/api/books/" + id)
                .then()
                .statusCode(200)
                .body("title", equalTo("Put Idempotency Updated"));
        given().header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(replace)
                .when()
                .put("/api/books/" + id)
                .then()
                .statusCode(200)
                .body("title", equalTo("Put Idempotency Updated"));
    }

    @Test
    public void deleteTwiceSecondReturns404() {
        String token = adminToken();
        String create =
                """
                {
                  "title": "Temp Delete Me",
                  "author": "T",
                  "isbn": "7777777777777",
                  "price": 1.00,
                  "stock": 0,
                  "category": "Fiction",
                  "language": "English"
                }
                """;
        int id =
                given().header("Authorization", "Bearer " + token)
                        .contentType(ContentType.JSON)
                        .body(create)
                        .when()
                        .post("/api/books")
                        .then()
                        .statusCode(201)
                        .extract()
                        .path("id");
        given().header("Authorization", "Bearer " + token)
                .when()
                .delete("/api/books/" + id)
                .then()
                .statusCode(204);
        given().header("Authorization", "Bearer " + token)
                .when()
                .delete("/api/books/" + id)
                .then()
                .statusCode(404)
                .body("code", equalTo("NOT_FOUND"));
    }

    @Test
    public void duplicateIsbnReturns409() {
        String token = adminToken();
        String create =
                """
                {
                  "title": "Dup A",
                  "author": "A",
                  "isbn": "6666666666666",
                  "price": 1.00,
                  "stock": 0,
                  "category": "Fiction",
                  "language": "English"
                }
                """;
        given().header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(create)
                .when()
                .post("/api/books")
                .then()
                .statusCode(201);
        given().header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(create)
                .when()
                .post("/api/books")
                .then()
                .statusCode(409)
                .body("code", equalTo("CONFLICT"));
    }

    @Test
    public void requestIdHeaderEchoed() {
        given().contentType(ContentType.JSON)
                .header("X-Request-Id", "trace-abc-123")
                .body(Map.of("username", "admin", "password", "wrong"))
                .when()
                .post("/api/login")
                .then()
                .statusCode(401)
                .header("X-Request-Id", "trace-abc-123")
                .body("requestId", equalTo("trace-abc-123"));
    }
}

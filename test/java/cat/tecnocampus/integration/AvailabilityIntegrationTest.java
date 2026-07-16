// java
package cat.tecnocampus.integration;

import cat.tecnocampus.security.authentication.AuthenticationRequest;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Sql(scripts = "classpath:data-test.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class AvailabilityIntegrationTest {

    @LocalServerPort
    private int port;

    private String vetToken;
    private String receptionistToken;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.baseURI = "http://localhost";
        vetToken = getJWTToken(1L); // según data.sql: person 1 = Veterinarian
        receptionistToken = getJWTToken(3L); // según data.sql: person 3 = Receptionist
    }

    static String getJWTToken(Long userId) {
        AuthenticationRequest req = new AuthenticationRequest(userId, "password123");
        return RestAssured
                .given()
                .contentType("application/json")
                .body(req)
                .when()
                .post("/loginJWT")
                .then()
                .statusCode(HttpStatus.OK.value())
                .header("Authorization", startsWith("Bearer "))
                .extract()
                .header("Authorization");
    }

    @Test
    void getAvailableVeterinarians() {
        RestAssured
                .given()
                .header("Authorization", vetToken)
                .when()
                .get("/api/veterinarian/available")
                .then()
                .statusCode(HttpStatus.OK.value());
    }

    @Test
    void createUpdateAndDeleteAvailabilityFlow() {
        String availabilityJson = """
            {
              "availabilityId": null,
              "dayOfWeek": 2,
              "startTime": "09:00",
              "endTime": "17:00",
              "periodStart": "2024-06-01",
              "periodEnd": "2026-12-31",
              "availabilityExceptions": []
            }
            """;

        // Create availability (as Receptionist)
        RestAssured
                .given()
                .contentType("application/json")
                .header("Authorization", receptionistToken)
                .body(availabilityJson)
                .when()
                .post("/api/veterinarian/1/availability")
                .then()
                .statusCode(anyOf(is(HttpStatus.CREATED.value()), is(HttpStatus.OK.value())));

        // Verify availability exists and has dayOfWeek = 2 (appended after 7 existing)
        RestAssured
                .given()
                .header("Authorization", receptionistToken)
                .when()
                .get("/api/veterinarian/1/availability")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("size()", greaterThan(7))
                .body("[7].dayOfWeek", equalTo(2));

        // Update availability (change periodStart) - attempt PUT
        String updateJson = """
            {
              "availabilityId": null,
              "dayOfWeek": 2,
              "startTime": "09:00",
              "endTime": "17:00",
              "periodStart": "2025-06-01",
              "periodEnd": "2025-12-31",
              "availabilityExceptions": []
            }
            """;

        RestAssured
                .given()
                .contentType("application/json")
                .header("Authorization", receptionistToken)
                .body(updateJson)
                .when()
                .put("/api/veterinarian/1/availability/1")
                .then()
                .statusCode(anyOf(is(HttpStatus.OK.value()), is(HttpStatus.NO_CONTENT.value())));

        // Add an availability exception
        String exceptionJson = """
            {
              "reason": "error",
              "dayOfWeek": 1,
              "startTime": "09:00",
              "endTime": "13:30",
              "periodStart": "2025-09-25",
              "periodEnd": "2025-12-20"
            }
            """;

        RestAssured
                .given()
                .contentType("application/json")
                .header("Authorization", receptionistToken)
                .body(exceptionJson)
                .when()
                .post("/api/veterinarian/1/availability/1/exception/")
                .then()
                .statusCode(anyOf(is(HttpStatus.CREATED.value()), is(HttpStatus.OK.value())));

        // Verify exception present
        RestAssured
                .given()
                .header("Authorization", receptionistToken)
                .when()
                .get("/api/veterinarian/1/availability")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("[0].availabilityExceptions.size()", greaterThanOrEqualTo(1))
                .body("[0].availabilityExceptions[0].reason", equalTo("error"));

        // Delete the exception (assume id 1)
        RestAssured
                .given()
                .header("Authorization", receptionistToken)
                .when()
                .delete("/api/veterinarian/1/availability/1/exception/1")
                .then()
                .statusCode(anyOf(is(HttpStatus.NO_CONTENT.value()), is(HttpStatus.OK.value())));

        // Delete the created availability (assume id 1 was the created one at index 7)
        RestAssured
                .given()
                .header("Authorization", receptionistToken)
                .when()
                .delete("/api/veterinarian/1/availability/1")
                .then()
                .statusCode(anyOf(is(HttpStatus.NO_CONTENT.value()), is(HttpStatus.OK.value())));

        // Final check: the initial 7 availabilities from `data.sql` should remain
        RestAssured
                .given()
                .header("Authorization", receptionistToken)
                .when()
                .get("/api/veterinarian/1/availability")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("size()", is(7));
    }
}

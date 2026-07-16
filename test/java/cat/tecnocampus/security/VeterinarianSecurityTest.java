package cat.tecnocampus.security;

import cat.tecnocampus.security.authentication.AuthenticationRequest;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class VeterinarianSecurityTest {

    @LocalServerPort
    private int port;

    private String vetToken;
    private String receptionistToken;
    private String inventoryManagerToken;
    private String adminToken;
    private String vetAssistantToken;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.baseURI = "http://localhost";
        vetToken = getJWTTokenOrNull(1L); // según data.sql suele ser Veterinarian
        receptionistToken = getJWTTokenOrNull(3L); // según data.sql suele ser Receptionist
        inventoryManagerToken = getJWTTokenOrNull(6L); // según data.sql suele ser Inventory Manager
        adminToken = getJWTTokenOrNull(5L); // según data.sql suele ser Admin
        vetAssistantToken = getJWTTokenOrNull(4L); // según data.sql suele ser Vet Assistant
    }

    private String getJWTTokenOrNull(Long userId) {
        AuthenticationRequest req = new AuthenticationRequest(userId, "password123");
        try {
            return RestAssured
                    .given()
                    .contentType("application/json")
                    .body(req)
                    .when()
                    .post("/loginJWT")
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .extract()
                    .header("Authorization");
        } catch (AssertionError ex) {
            return null;
        }
    }

    @Test
    void protectedEndpointsRequireAuthentication() {
        RestAssured
                .when()
                .get("/api/pet/owner/2")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value());

        RestAssured
                .when()
                .get("/api/medication")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    void receptionistCanAccessPetInvoicesVeterinarianButNotMedication() {
        assumeTokenPresent(receptionistToken, "Receptionist token missing - revisa data-test.sql");

        RestAssured
                .given()
                .header("Authorization", receptionistToken)
                .when()
                .get("/api/pet/owner/2")
                .then()
                .statusCode(HttpStatus.OK.value());

        RestAssured
                .given()
                .header("Authorization", receptionistToken)
                .when()
                .get("/api/invoices")
                .then()
                .statusCode(anyOf(is(HttpStatus.OK.value()), is(HttpStatus.NO_CONTENT.value())));

        RestAssured
                .given()
                .header("Authorization", receptionistToken)
                .when()
                .get("/api/veterinarian/visits")
                .then()
                .statusCode(anyOf(is(HttpStatus.OK.value()), is(HttpStatus.NO_CONTENT.value())));

        RestAssured
                .given()
                .header("Authorization", receptionistToken)
                .when()
                .get("/api/medication")
                .then()
                .statusCode(HttpStatus.FORBIDDEN.value());
    }

    @Test
    void veterinarianCanAccessMedicationAndVeterinarianButNotPetOrProfilesPost() {
        assumeTokenPresent(vetToken, "Veterinarian token missing - revisa data-test.sql");

        RestAssured
                .given()
                .header("Authorization", vetToken)
                .when()
                .get("/api/medication")
                .then()
                .statusCode(anyOf(is(HttpStatus.OK.value()), is(HttpStatus.NO_CONTENT.value())));

        RestAssured
                .given()
                .header("Authorization", vetToken)
                .when()
                .get("/api/veterinarian/visits")
                .then()
                .statusCode(anyOf(is(HttpStatus.OK.value()), is(HttpStatus.NO_CONTENT.value())));

        RestAssured
                .given()
                .header("Authorization", vetToken)
                .when()
                .get("/api/pet/owner/2")
                .then()
                .statusCode(HttpStatus.FORBIDDEN.value());

        RestAssured
                .given()
                .header("Authorization", vetToken)
                .when()
                .post("/profiles")
                .then()
                .statusCode(HttpStatus.FORBIDDEN.value());
    }

    @Test
    void adminHasAccessToProtectedEndpointsIncludingProfilesPost_foundDynamically() {
        String adminToken = findAdminTokenOrNull();
        assertNotNull(adminToken, "No se encontró un usuario admin en los ids 1..10; revisa data-test.sql");

        // endpoints que deben permitir ADMIN (no deben devolver 401/403)
        RestAssured
                .given()
                .header("Authorization", adminToken)
                .when()
                .get("/api/pet/owner/2")
                .then()
                .statusCode(not(anyOf(is(HttpStatus.UNAUTHORIZED.value()), is(HttpStatus.FORBIDDEN.value()))));

        RestAssured
                .given()
                .header("Authorization", adminToken)
                .when()
                .get("/api/medication")
                .then()
                .statusCode(not(anyOf(is(HttpStatus.UNAUTHORIZED.value()), is(HttpStatus.FORBIDDEN.value()))));

        // POST /profiles: puede devolver CREATED, OK, NO_CONTENT o 404 si no está implementado,
        // lo importante aquí es que no devuelva 401/403.
        RestAssured
                .given()
                .header("Authorization", adminToken)
                .contentType("application/json")
                .body("{}")
                .when()
                .post("/profiles")
                .then()
                .statusCode(not(anyOf(is(HttpStatus.UNAUTHORIZED.value()), is(HttpStatus.FORBIDDEN.value()))));
    }

    // java
    @Test
    void adminExplicitPermissions() {
        assumeTokenPresent(adminToken, "Admin token missing - revisa data-test.sql");

        // Admin debe poder acceder a endpoints protegidos (no 401/403)
        RestAssured
                .given()
                .header("Authorization", adminToken)
                .when()
                .get("/api/pet/owner/2")
                .then()
                .statusCode(not(anyOf(is(HttpStatus.UNAUTHORIZED.value()), is(HttpStatus.FORBIDDEN.value()))));

        RestAssured
                .given()
                .header("Authorization", adminToken)
                .when()
                .get("/api/medication")
                .then()
                .statusCode(not(anyOf(is(HttpStatus.UNAUTHORIZED.value()), is(HttpStatus.FORBIDDEN.value()))));

        RestAssured
                .given()
                .header("Authorization", adminToken)
                .contentType("application/json")
                .body("{}")
                .when()
                .post("/profiles")
                .then()
                .statusCode(not(anyOf(is(HttpStatus.UNAUTHORIZED.value()), is(HttpStatus.FORBIDDEN.value()))));
    }

    @Test
    void vetAssistantPermissions() {
        assumeTokenPresent(vetAssistantToken, "Vet Assistant token missing - revisa data-test.sql");

        // Vet assistant NO puede acceder a listados generales de veterinarios (según config)
        RestAssured
                .given()
                .header("Authorization", vetAssistantToken)
                .when()
                .get("/api/veterinarian/visits")
                .then()
                .statusCode(HttpStatus.FORBIDDEN.value());

        // Vet assistant puede invocar las operaciones concretas de visitas (PUT) permitidas;
        // aceptamos cualquier status excepto 401/403 (puede devolver 404 si no existe la visita)
        RestAssured
                .given()
                .header("Authorization", vetAssistantToken)
                .when()
                .put("/api/veterinarian/visits/1/initiate")
                .then()
                .statusCode(not(anyOf(is(HttpStatus.UNAUTHORIZED.value()), is(HttpStatus.FORBIDDEN.value()))));

        // Vet assistant no tiene acceso a medicación (solo VETERINARIAN o ADMIN)
        RestAssured
                .given()
                .header("Authorization", vetAssistantToken)
                .when()
                .get("/api/medication")
                .then()
                .statusCode(HttpStatus.FORBIDDEN.value());

        // Vet assistant no puede crear perfiles
        RestAssured
                .given()
                .header("Authorization", vetAssistantToken)
                .contentType("application/json")
                .body("{}")
                .when()
                .post("/profiles")
                .then()
                .statusCode(HttpStatus.FORBIDDEN.value());
    }


    @Test
    void inventoryManagerCanCreateMedicationBatch() {
        assumeTokenPresent(inventoryManagerToken, "Inventory Manager token missing - revisa data-test.sql");

        String batchJson = """
        {
          "lotNumber": 1,
          "receivedDate": "2024-01-01",
          "expiryDate": "2030-01-01",
          "initialQuantity": 10,
          "purchagePricePerUnit": 1,
          "storageLocation": "S",
          "reorderThreshold": 5
        }
        """;

        RestAssured
                .given()
                .contentType("application/json")
                .header("Authorization", inventoryManagerToken)
                .body(batchJson)
                .when()
                .post("/api/medication/1/batches")
                .then()
                .statusCode(anyOf(is(HttpStatus.CREATED.value()), is(HttpStatus.OK.value())));
    }

    private String findAdminTokenOrNull() {
        for (long id = 1; id <= 10; id++) {
            String token = getJWTTokenOrNull(id);
            if (token == null) continue;
            int status = RestAssured
                    .given()
                    .header("Authorization", token)
                    .contentType("application/json")
                    .body("{}")
                    .when()
                    .post("/profiles")
                    .then()
                    .extract()
                    .statusCode();
            if (status != HttpStatus.FORBIDDEN.value() && status != HttpStatus.UNAUTHORIZED.value()) {
                return token;
            }
        }
        return null;
    }

    private static void assumeTokenPresent(String token, String message) {
        if (token == null) {
            fail(message);
        }
    }
}

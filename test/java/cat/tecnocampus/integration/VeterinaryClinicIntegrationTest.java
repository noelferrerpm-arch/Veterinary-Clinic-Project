// java
package cat.tecnocampus.integration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Sql(scripts = "classpath:data-test.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class VeterinaryClinicIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private final ObjectMapper mapper = new ObjectMapper();

    private HttpHeaders loginAndGetHeaders() {
        Map<String, Object> loginBody = Map.of("userId", 1, "password", "password123");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> loginRequest = new HttpEntity<>(loginBody, headers);

        ResponseEntity<Void> loginResp = restTemplate.postForEntity("/loginJWT", loginRequest, Void.class);
        assertTrue(loginResp.getStatusCode().is2xxSuccessful(), "Login debe ser exitoso");
        String token = loginResp.getHeaders().getFirst("Authorization");
        assertNotNull(token, "Authorization header debe existir");

        HttpHeaders authHeaders = new HttpHeaders();
        authHeaders.set("Authorization", token);
        authHeaders.setContentType(MediaType.APPLICATION_JSON);
        return authHeaders;
    }

    @Test
    public void testGetVeterinariansAndAvailable() throws Exception {
        HttpHeaders headers = loginAndGetHeaders();

        ResponseEntity<String> vetsResp = restTemplate.exchange("/api/veterinarian", HttpMethod.GET, new HttpEntity<>(headers), String.class);
        assertEquals(HttpStatus.OK, vetsResp.getStatusCode());
        List<Map<String, Object>> vets = mapper.readValue(vetsResp.getBody(), new TypeReference<>() {});
        assertFalse(vets.isEmpty());

        ResponseEntity<String> availResp = restTemplate.exchange("/api/veterinarian/available", HttpMethod.GET, new HttpEntity<>(headers), String.class);
        assertEquals(HttpStatus.OK, availResp.getStatusCode());
    }

    @Test
    public void testAgendaExists() {
        HttpHeaders headers = loginAndGetHeaders();

        ResponseEntity<String> agendaResp = restTemplate.exchange("/api/veterinarian/1/agenda", HttpMethod.GET, new HttpEntity<>(headers), String.class);
        assertEquals(HttpStatus.OK, agendaResp.getStatusCode());
        assertTrue(agendaResp.getBody().contains("2025"));
    }

    @Test
    public void testCreateVeterinarian() {
        HttpHeaders headers = loginAndGetHeaders();

        Map<String, Object> newVet = Map.of(
                "firstName", "Test",
                "lastName", "Vet",
                "phoneNumber", "000",
                "email", "testvet@example.com",
                "address", "Calle Test",
                "licenseNumber", 99,
                "yearsOfExperience", 1
        );
        ResponseEntity<String> createVetResp = restTemplate.postForEntity("/api/veterinarian", new HttpEntity<>(newVet, headers), String.class);
        assertEquals(HttpStatus.CREATED, createVetResp.getStatusCode());
        assertTrue(createVetResp.getBody().contains("Test"));
    }

    @Test
    public void testCreateWalkInAndScheduledVisit() {
        HttpHeaders headers = loginAndGetHeaders();

        Map<String, Object> walkIn = Map.of(
                "petId", 1,
                "ownerId", 2,
                "agendaId", 3,
                "duration", 30,
                "reason", "PupaTest",
                "price", 20.0
        );
        ResponseEntity<String> walkInResp = restTemplate.postForEntity("/api/veterinarian/1/visits/walk-in", new HttpEntity<>(walkIn, headers), String.class);
        assertEquals(HttpStatus.CREATED, walkInResp.getStatusCode());
        assertTrue(walkInResp.getBody().contains("PupaTest"));

        Map<String, Object> scheduledVisit = Map.of(
                "petId", 1,
                "ownerId", 2,
                "agendaId", 3,
                "startDate", "2025-11-20T10:00:00",
                "duration", 15,
                "reason", "Getting spayed",
                "price", 1000
        );
        ResponseEntity<String> schedResp = restTemplate.postForEntity("/api/veterinarian/visit", new HttpEntity<>(scheduledVisit, headers), String.class);
        assertEquals(HttpStatus.CREATED, schedResp.getStatusCode());
        assertTrue(schedResp.getBody().contains("Getting spayed"));
    }

    @Test
    public void testVisitLifecycle() throws Exception {
        HttpHeaders headers = loginAndGetHeaders();

        ResponseEntity<String> allVisitsResp = restTemplate.exchange("/api/veterinarian/visits", HttpMethod.GET, new HttpEntity<>(headers), String.class);
        assertEquals(HttpStatus.OK, allVisitsResp.getStatusCode());
        List<Map<String, Object>> visits = mapper.readValue(allVisitsResp.getBody(), new TypeReference<>() {});
        assertFalse(visits.isEmpty());
        Long visitId = ((Number) visits.get(0).get("visitId")).longValue();

        ResponseEntity<String> initResp = restTemplate.exchange("/api/veterinarian/visits/" + visitId + "/initiate", HttpMethod.PUT, new HttpEntity<>(headers), String.class);
        assertEquals(HttpStatus.OK, initResp.getStatusCode());

        Map<String, String> diag = Map.of("diagnosis", "Fractura", "notes", "Reposo");
        HttpEntity<Map<String, String>> diagReq = new HttpEntity<>(diag, headers);
        ResponseEntity<String> diagResp = restTemplate.exchange("/api/veterinarian/visits/" + visitId + "/diagnosis", HttpMethod.PATCH, diagReq, String.class);
        assertEquals(HttpStatus.OK, diagResp.getStatusCode());
        assertTrue(diagResp.getBody().contains("Fractura"));

        ResponseEntity<String> completeResp = restTemplate.exchange("/api/veterinarian/visits/" + visitId + "/complete", HttpMethod.PUT, new HttpEntity<>(headers), String.class);
        assertEquals(HttpStatus.OK, completeResp.getStatusCode());

        Map<String, Object> treatment = Map.of(
                "treatmentId", 1,
                "name", "Nail clipper",
                "description", "Cuts animals nails",
                "cost", 50
        );
        ResponseEntity<String> treatResp = restTemplate.postForEntity("/api/veterinarian/visits/" + visitId + "/prescribe-treatment", new HttpEntity<>(treatment, headers), String.class);
        assertEquals(HttpStatus.CREATED, treatResp.getStatusCode());
    }

    @Test
    public void testIncompatibleAndDemand() {
        HttpHeaders headers = loginAndGetHeaders();

        ResponseEntity<String> incompatResp = restTemplate.exchange("/api/veterinarian/pet/1/incompatible/1", HttpMethod.GET, new HttpEntity<>(headers), String.class);
        assertEquals(HttpStatus.OK, incompatResp.getStatusCode());
        assertNotNull(incompatResp.getBody());

        ResponseEntity<String> demandResp = restTemplate.exchange("/api/veterinarian/demand/start/2024-01-01/end/2026-12-31", HttpMethod.GET, new HttpEntity<>(headers), String.class);
        assertEquals(HttpStatus.OK, demandResp.getStatusCode());
    }

    @Test
    public void testCreateAndDeleteVisit() throws Exception {
        HttpHeaders headers = loginAndGetHeaders();

        Map<String, Object> tempVisit = Map.of(
                "petId", 1,
                "ownerId", 2,
                "agendaId", 1,
                "startDate", "2025-11-20T11:00:00",
                "duration", 10,
                "reason", "ToDelete",
                "price", 10
        );
        ResponseEntity<String> createdTemp = restTemplate.postForEntity("/api/veterinarian/visit", new HttpEntity<>(tempVisit, headers), String.class);
        assertEquals(HttpStatus.CREATED, createdTemp.getStatusCode());
        Map<String, Object> createdTempMap = mapper.readValue(createdTemp.getBody(), new TypeReference<>() {});
        Long tempId = ((Number) createdTempMap.get("visitId")).longValue();

        ResponseEntity<Void> deleteResp = restTemplate.exchange("/api/veterinarian/visits/" + tempId, HttpMethod.DELETE, new HttpEntity<>(headers), Void.class);
        assertTrue(deleteResp.getStatusCode().is2xxSuccessful());

        ResponseEntity<String> getDeleted = restTemplate.exchange("/api/veterinarian/visits/" + tempId, HttpMethod.GET, new HttpEntity<>(headers), String.class);
        assertTrue(getDeleted.getStatusCode().is4xxClientError());
    }

    @Test
    public void testErrorExampleNonExistingAgenda() {
        HttpHeaders headers = loginAndGetHeaders();

        ResponseEntity<String> resp = restTemplate.exchange("/api/veterinarian/999/agenda", HttpMethod.GET, new HttpEntity<>(headers), String.class);
        assertTrue(resp.getStatusCode().is4xxClientError());
    }
}

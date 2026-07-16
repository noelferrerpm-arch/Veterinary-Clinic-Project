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

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Sql(scripts = "classpath:data-test.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class MedicationIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private final ObjectMapper mapper = new ObjectMapper();

    private HttpHeaders loginAndGetHeaders() {
        Map<String, Object> loginBody = Map.of("userId", 1, "password", "password123"); // inventory manager
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

    private Optional<Long> extractId(Map<String, Object> m, String... keys) {
        for (String k : keys) {
            if (m.containsKey(k) && m.get(k) instanceof Number) {
                return Optional.of(((Number) m.get(k)).longValue());
            }
        }
        // try find any numeric field named id-ish
        for (Map.Entry<String, Object> e : m.entrySet()) {
            if (e.getValue() instanceof Number && e.getKey().toLowerCase().contains("id")) {
                return Optional.of(((Number) e.getValue()).longValue());
            }
        }
        return Optional.empty();
    }

    @Test
    public void testCreateGetMedicationAndBatchFlow() throws Exception {
        HttpHeaders headers = loginAndGetHeaders();

        // 1) Create a medication
        Map<String, Object> newMed = Map.of(
                "name", "Plexifolito",
                "activeIngredient", "Cositas",
                "dosageUnit", 2,
                "unitPrice", 10
        );
        HttpEntity<Map<String, Object>> createMedReq = new HttpEntity<>(newMed, headers);
        ResponseEntity<String> createResp = restTemplate.postForEntity("/api/medication", createMedReq, String.class);
        assertEquals(HttpStatus.CREATED, createResp.getStatusCode());

        // 2) Get all medications and find created one
        ResponseEntity<String> allMedsResp = restTemplate.exchange("/api/medication", HttpMethod.GET, new HttpEntity<>(headers), String.class);
        assertEquals(HttpStatus.OK, allMedsResp.getStatusCode());
        List<Map<String, Object>> meds = mapper.readValue(allMedsResp.getBody(), new TypeReference<>() {});
        List<Map<String, Object>> found = meds.stream().filter(m -> "Plexifolito".equals(m.get("name"))).collect(Collectors.toList());
        assertFalse(found.isEmpty(), "La medicación creada debe aparecer en la lista");

        Long medId = extractId(found.get(0), "medicationId", "id", "medication_id").orElseThrow(() -> new AssertionError("No se pudo extraer id de la medicación"));

        // 3) Create a medication batch for that medication
        Map<String, Object> batch = new HashMap<>();
        batch.put("lotNumber", 1);
        batch.put("receivedDate", "2024-01-01");
        batch.put("expiryDate", "2030-01-01");
        batch.put("initialQuantity", 10);
        batch.put("purchagePricePerUnit", 1); // kept same key as ejemplo de calls
        batch.put("storageLocation", "Si");
        batch.put("reorderThreshold", 5);

        HttpEntity<Map<String, Object>> createBatchReq = new HttpEntity<>(batch, headers);
        ResponseEntity<String> createBatchResp = restTemplate.postForEntity("/api/medication/" + medId + "/batches", createBatchReq, String.class);
        assertEquals(HttpStatus.CREATED, createBatchResp.getStatusCode());

        // 4) Get batches of medication and verify created batch exists
        ResponseEntity<String> batchesResp = restTemplate.exchange("/api/medication/batches/" + medId, HttpMethod.GET, new HttpEntity<>(headers), String.class);
        assertEquals(HttpStatus.OK, batchesResp.getStatusCode());
        List<Map<String, Object>> batches = mapper.readValue(batchesResp.getBody(), new TypeReference<>() {});
        assertFalse(batches.isEmpty(), "Debe existir al menos un lote para la medicación creada");
        Long batchId = extractId(batches.get(0), "batchId", "id", "medicationBatchId", "target_id").orElseThrow();

        // 5) Sell some units
        ResponseEntity<String> sellResp = restTemplate.exchange("/api/medication/" + medId + "/buy/5", HttpMethod.GET, new HttpEntity<>(headers), String.class);
        assertEquals(HttpStatus.OK, sellResp.getStatusCode());
        assertTrue(sellResp.getBody().contains("Plexifolito"));

        // 6) Delete the batch (should return 2xx/204)
        ResponseEntity<Void> deleteResp = restTemplate.exchange("/api/medication/batches/" + batchId, HttpMethod.DELETE, new HttpEntity<>(headers), Void.class);
        assertTrue(deleteResp.getStatusCode().is2xxSuccessful());

        // After delete, batches list should be empty or decreased
        ResponseEntity<String> batchesAfterDel = restTemplate.exchange("/api/medication/batches/" + medId, HttpMethod.GET, new HttpEntity<>(headers), String.class);
        assertEquals(HttpStatus.OK, batchesAfterDel.getStatusCode());
    }

    @Test
    public void testIncompatibilityAddRemoveAndQueries() throws Exception {
        HttpHeaders headers = loginAndGetHeaders();

        // Use medications from data.sql: should exist ids 1..4
        // Add incompatibility between 1 and 3 (as in example)
        ResponseEntity<String> addResp = restTemplate.exchange("/api/medication/1/incompatibility/add/3", HttpMethod.PUT, new HttpEntity<>(headers), String.class);
        assertEquals(HttpStatus.OK, addResp.getStatusCode());
        assertNotNull(addResp.getBody());

        // Remove incompatibility
        ResponseEntity<String> removeResp = restTemplate.exchange("/api/medication/1/incompatibility/remove/3", HttpMethod.PUT, new HttpEntity<>(headers), String.class);
        assertEquals(HttpStatus.OK, removeResp.getStatusCode());

        // Query meds between dates (should return OK)
        ResponseEntity<String> betweenResp = restTemplate.exchange("/api/medication/start/2023-01-01/end/2025-01-01", HttpMethod.GET, new HttpEntity<>(headers), String.class);
        assertEquals(HttpStatus.OK, betweenResp.getStatusCode());

        // Query veterinarians related to a medication between dates (OK)
        ResponseEntity<String> vetsResp = restTemplate.exchange("/api/medication/1/start/2023-01-01/end/2025-01-01", HttpMethod.GET, new HttpEntity<>(headers), String.class);
        assertEquals(HttpStatus.OK, vetsResp.getStatusCode());
    }

    @Test
    public void testGetMedicationByIdAndErrorExample() {
        HttpHeaders headers = loginAndGetHeaders();

        // Existing med id 1 should return OK
        ResponseEntity<String> getOne = restTemplate.exchange("/api/medication/1", HttpMethod.GET, new HttpEntity<>(headers), String.class);
        assertEquals(HttpStatus.OK, getOne.getStatusCode());
        assertNotNull(getOne.getBody());

        // Non-existent medication should return 4xx
        ResponseEntity<String> notFound = restTemplate.exchange("/api/medication/999", HttpMethod.GET, new HttpEntity<>(headers), String.class);
        assertTrue(notFound.getStatusCode().is4xxClientError());
    }
}

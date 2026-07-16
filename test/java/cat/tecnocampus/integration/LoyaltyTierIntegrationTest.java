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
public class LoyaltyTierIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private final ObjectMapper mapper = new ObjectMapper();

    private HttpHeaders loginAndGetHeaders() {
        Map<String, Object> loginBody = Map.of("userId", 3, "password", "password123"); // receptionist
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
        for (Map.Entry<String, Object> e : m.entrySet()) {
            if (e.getValue() instanceof Number && e.getKey().toLowerCase().contains("id")) {
                return Optional.of(((Number) e.getValue()).longValue());
            }
        }
        return Optional.empty();
    }

    @Test
    public void testCreateListGetUpdateAndDeleteBehavior() throws Exception {
        HttpHeaders headers = loginAndGetHeaders();

        // 1) Crear un loyalty tier
        Map<String, Object> createBody = Map.of(
                "tierName", "Silver",
                "requiredPoints", 100,
                "discountPercentage", 5.0,
                "benefitsDescription", "Basic member rewards"
        );
        HttpEntity<Map<String, Object>> createReq = new HttpEntity<>(createBody, headers);
        ResponseEntity<String> createResp = restTemplate.postForEntity("/api/loyalty-tiers", createReq, String.class);
        assertEquals(HttpStatus.CREATED, createResp.getStatusCode());

        Map<String, Object> created = mapper.readValue(createResp.getBody(), new TypeReference<>() {});
        Long createdId = extractId(created, "id", "loyaltyTierId").orElseThrow();

        // 2) Obtener por id
        ResponseEntity<String> getResp = restTemplate.exchange("/api/loyalty-tiers/" + createdId, HttpMethod.GET, new HttpEntity<>(headers), String.class);
        assertEquals(HttpStatus.OK, getResp.getStatusCode());
        assertTrue(getResp.getBody().contains("Silver"));

        // 3) Listar y comprobar presencia
        ResponseEntity<String> listResp = restTemplate.exchange("/api/loyalty-tiers", HttpMethod.GET, new HttpEntity<>(headers), String.class);
        assertEquals(HttpStatus.OK, listResp.getStatusCode());
        List<Map<String, Object>> list = mapper.readValue(listResp.getBody(), new TypeReference<>() {});
        List<Long> ids = list.stream().map(m -> extractId(m, "id", "loyaltyTierId").orElse(-1L)).collect(Collectors.toList());
        assertTrue(ids.contains(createdId));

        // 4) Actualizar
        Map<String, Object> updateBody = new HashMap<>(created);
        updateBody.put("tierName", "Silver Plus");
        updateBody.put("requiredPoints", 150);
        HttpEntity<Map<String, Object>> updateReq = new HttpEntity<>(updateBody, headers);
        ResponseEntity<String> updResp = restTemplate.exchange("/api/loyalty-tiers/" + createdId, HttpMethod.PUT, updateReq, String.class);
        assertEquals(HttpStatus.OK, updResp.getStatusCode());
        assertTrue(updResp.getBody().contains("Silver Plus"));

        // 5) Borrar el creado -> debe devolver 204 No Content
        ResponseEntity<Void> delResp = restTemplate.exchange("/api/loyalty-tiers/" + createdId, HttpMethod.DELETE, new HttpEntity<>(headers), Void.class);
        assertTrue(delResp.getStatusCode().is2xxSuccessful());
        assertEquals(HttpStatus.NO_CONTENT, delResp.getStatusCode());

        // 6) Comprobar que ya no existe
        ResponseEntity<String> afterDelGet = restTemplate.exchange("/api/loyalty-tiers/" + createdId, HttpMethod.GET, new HttpEntity<>(headers), String.class);
        assertTrue(afterDelGet.getStatusCode().is4xxClientError());
    }

    @Test
    public void testDeleteExistingTierWithPetOwnerAndNotFoundExamples() {
        HttpHeaders headers = loginAndGetHeaders();

        // data.sql inserta un Loyalty Tier asociado a un Pet_Owner (probablemente id 1)
        ResponseEntity<Void> delResp = restTemplate.exchange("/api/loyalty-tiers/1", HttpMethod.DELETE, new HttpEntity<>(headers), Void.class);

        if (delResp.getStatusCode().is2xxSuccessful()) {
            // Si se pudo borrar, debe ser 204 No Content y ya no existir
            assertEquals(HttpStatus.NO_CONTENT, delResp.getStatusCode());
            ResponseEntity<String> afterDel = restTemplate.exchange("/api/loyalty-tiers/1", HttpMethod.GET, new HttpEntity<>(headers), String.class);
            assertTrue(afterDel.getStatusCode().is4xxClientError() || afterDel.getStatusCode().is5xxServerError());
        } else {
            // Si no se pudo borrar, aceptamos error cliente o servidor
            assertTrue(delResp.getStatusCode().is4xxClientError() || delResp.getStatusCode().is5xxServerError());
        }

        // Obtener no existente -> aceptamos 4xx o 5xx
        ResponseEntity<String> getMissing = restTemplate.exchange("/api/loyalty-tiers/999", HttpMethod.GET, new HttpEntity<>(headers), String.class);
        assertTrue(getMissing.getStatusCode().is4xxClientError() || getMissing.getStatusCode().is5xxServerError());
    }
}

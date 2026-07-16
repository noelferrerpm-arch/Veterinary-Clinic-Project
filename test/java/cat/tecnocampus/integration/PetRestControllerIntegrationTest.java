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
public class PetRestControllerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private final ObjectMapper mapper = new ObjectMapper();

    private HttpHeaders loginAndGetHeaders() {
        Map<String, Object> loginBody = Map.of("userId", 3, "password", "password123");
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
    public void testGetPetById_usesDataSql() throws Exception {
        HttpHeaders headers = loginAndGetHeaders();

        ResponseEntity<String> resp = restTemplate.exchange("/api/pet/1", HttpMethod.GET, new HttpEntity<>(headers), String.class);
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNotNull(resp.getBody());
        Map<String, Object> pet = mapper.readValue(resp.getBody(), new TypeReference<>() {});
        assertFalse(pet.isEmpty());
        assertTrue(pet.containsKey("name"));
        assertEquals("Buddy", String.valueOf(pet.get("name")));
    }

    @Test
    public void testGetPetOwnerAndFidelity_usesDataSql() throws Exception {
        HttpHeaders headers = loginAndGetHeaders();

        ResponseEntity<String> ownerResp = restTemplate.exchange("/api/pet/owner/2", HttpMethod.GET, new HttpEntity<>(headers), String.class);
        assertEquals(HttpStatus.OK, ownerResp.getStatusCode());
        assertNotNull(ownerResp.getBody());
        Map<String, Object> owner = mapper.readValue(ownerResp.getBody(), new TypeReference<>() {});
        assertFalse(owner.isEmpty());
        assertTrue(owner.containsKey("firstName"));
        assertEquals("Bob", String.valueOf(owner.get("firstName")));

        ResponseEntity<String> fidelityResp = restTemplate.exchange("/api/pet/owner/2/fidelity", HttpMethod.GET, new HttpEntity<>(headers), String.class);
        assertEquals(HttpStatus.OK, fidelityResp.getStatusCode());
        assertNotNull(fidelityResp.getBody());
        assertTrue(fidelityResp.getBody().contains("50"));
    }

    @Test
    public void testCreatePetOwner() throws Exception {
        HttpHeaders headers = loginAndGetHeaders();

        Map<String, Object> newOwner = Map.of(
                "firstName", "Paco",
                "lastName", "Sahur",
                "phoneNumber", "789769632",
                "email", "papapapapasahur+" + System.currentTimeMillis() + "@gmail.com",
                "address", "Some street over there,21",
                "loyaltyPoints", 0,
                "loyaltyTierId", 1,
                "pets", List.of()
        );

        ResponseEntity<String> createResp = restTemplate.postForEntity("/api/pet/owner", new HttpEntity<>(newOwner, headers), String.class);
        assertEquals(HttpStatus.CREATED, createResp.getStatusCode());
        assertNotNull(createResp.getBody());
        assertTrue(createResp.getBody().contains("Paco"));
    }

    @Test
    public void testCreatePet() throws Exception {
        HttpHeaders headers = loginAndGetHeaders();

        Map<String, Object> newPet = Map.of(
                "petOwnerId", 2,
                "petTypeId", 1,
                "name", "Lili",
                "microchipId", "mc-" + System.currentTimeMillis(),
                "dateOfBirth", "2020-03-03",
                "gender", "female",
                "breed", "Podenco",
                "color", "white",
                "weight", "10 kg"
        );

        ResponseEntity<String> createPetResp = restTemplate.postForEntity("/api/pet", new HttpEntity<>(newPet, headers), String.class);
        assertEquals(HttpStatus.CREATED, createPetResp.getStatusCode());
        assertNotNull(createPetResp.getBody());
        assertTrue(createPetResp.getBody().contains("Lili"));
    }

    @Test
    public void testGetPetHistory() throws Exception {
        HttpHeaders headers = loginAndGetHeaders();

        ResponseEntity<String> resp = restTemplate.exchange("/api/pet/1/history", HttpMethod.GET, new HttpEntity<>(headers), String.class);
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNotNull(resp.getBody());
        List<Object> history = mapper.readValue(resp.getBody(), new TypeReference<>() {});
        assertNotNull(history);
    }

    @Test
    public void testGetNonExistingPetShouldReturn4xx() {
        HttpHeaders headers = loginAndGetHeaders();

        ResponseEntity<String> resp = restTemplate.exchange("/api/pet/99999", HttpMethod.GET, new HttpEntity<>(headers), String.class);
        assertTrue(resp.getStatusCode().is4xxClientError(), "Pet inexistente debe devolver 4xx");
    }
}

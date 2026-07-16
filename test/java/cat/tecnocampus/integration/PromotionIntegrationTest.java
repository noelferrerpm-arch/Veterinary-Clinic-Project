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
public class PromotionIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private final ObjectMapper mapper = new ObjectMapper();

    private HttpHeaders loginAndGetHeaders() {
        Map<String, Object> loginBody = Map.of("userId", 5, "password", "password123");
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
    public void testCreatePromotion() throws Exception {
        HttpHeaders headers = loginAndGetHeaders();

        Map<String, Object> newPromotion = Map.of(
                "name", "Epic promotion",
                "description", "Test to check if createpromotion works",
                "discountCode", "1",
                "startDate", "2023-11-13",
                "endDate", "2030-11-13",
                "discounts", List.of()
        );

        ResponseEntity<String> resp = restTemplate.postForEntity("/api/promotion", new HttpEntity<>(newPromotion, headers), String.class);
        assertEquals(HttpStatus.CREATED, resp.getStatusCode());
        assertTrue(resp.getBody().contains("Epic promotion"));

        Map<String, Object> created = mapper.readValue(resp.getBody(), new TypeReference<>() {});
        assertNotNull(created.get("promotionId"));
    }

    @Test
    public void testGetPromotionByIdAndGetAll() throws Exception {
        HttpHeaders headers = loginAndGetHeaders();

        Map<String, Object> newPromotion = Map.of(
                "name", "Epic promotion",
                "description", "Test to check if createpromotion works",
                "discountCode", "1",
                "startDate", "2023-11-13",
                "endDate", "2030-11-13",
                "discounts", List.of()
        );

        ResponseEntity<String> resp = restTemplate.postForEntity("/api/promotion", new HttpEntity<>(newPromotion, headers), String.class);
        assertEquals(HttpStatus.CREATED, resp.getStatusCode());

        // se asume que data.sql contiene una promoción con id 1
        ResponseEntity<String> one = restTemplate.exchange("/api/promotion/1", HttpMethod.GET, new HttpEntity<>(headers), String.class);
        assertEquals(HttpStatus.OK, one.getStatusCode());
        assertNotNull(one.getBody());
        Map<String, Object> promo = mapper.readValue(one.getBody(), new TypeReference<>() {});
        assertTrue(promo.containsKey("name"));

        ResponseEntity<String> all = restTemplate.exchange("/api/promotion", HttpMethod.GET, new HttpEntity<>(headers), String.class);
        assertEquals(HttpStatus.OK, all.getStatusCode());
        List<Map<String, Object>> promos = mapper.readValue(all.getBody(), new TypeReference<>() {});
        assertFalse(promos.isEmpty());
    }

    @Test
    public void testUpdateAndDeletePromotion() throws Exception {
        HttpHeaders headers = loginAndGetHeaders();

        // crear para actualizar
        Map<String, Object> promo = Map.of(
                "name", "ToUpdate",
                "description", "Not updated",
                "discountCode", "1",
                "startDate", "2023-11-13",
                "endDate", "2030-11-13",
                "discounts", List.of()
        );
        ResponseEntity<String> createdResp = restTemplate.postForEntity("/api/promotion", new HttpEntity<>(promo, headers), String.class);
        assertEquals(HttpStatus.CREATED, createdResp.getStatusCode());
        Map<String, Object> created = mapper.readValue(createdResp.getBody(), new TypeReference<>() {});
        Long id = ((Number) created.get("promotionId")).longValue();

        // actualizar
        Map<String, Object> updated = Map.of(
                "name", "Updated Name",
                "description", "Updated",
                "discountCode", "3",
                "startDate", "2024-11-13",
                "endDate", "2028-11-13",
                "discounts", List.of()
        );
        ResponseEntity<String> updateResp = restTemplate.exchange("/api/promotion/" + id, HttpMethod.PUT, new HttpEntity<>(updated, headers), String.class);
        assertEquals(HttpStatus.OK, updateResp.getStatusCode());
        assertTrue(updateResp.getBody().contains("Updated Name"));

        // borrar
        ResponseEntity<Void> deleteResp = restTemplate.exchange("/api/promotion/" + id, HttpMethod.DELETE, new HttpEntity<>(headers), Void.class);
        assertTrue(deleteResp.getStatusCode().is2xxSuccessful());

        // comprobar borrado
        ResponseEntity<String> getDeleted = restTemplate.exchange("/api/promotion/" + id, HttpMethod.GET, new HttpEntity<>(headers), String.class);
        assertTrue(getDeleted.getStatusCode().is4xxClientError());
    }

    @Test
    public void testAddUpdateDeleteDiscountsAndGetDiscountsList() throws Exception {
        HttpHeaders headers = loginAndGetHeaders();

        Map<String, Object> newPromotion = Map.of(
                "name", "Epic promotion",
                "description", "Test to check if createpromotion works",
                "discountCode", "1",
                "startDate", "2023-11-13",
                "endDate", "2030-11-13",
                "discounts", List.of()
        );

        ResponseEntity<String> resp = restTemplate.postForEntity("/api/promotion", new HttpEntity<>(newPromotion, headers), String.class);
        assertEquals(HttpStatus.CREATED, resp.getStatusCode());

        // se asume data.sql tiene promocion con id 1; si no, crear una y usar su id.
        long promotionId = 1;

        Map<String, Object> discount1 = Map.of(
                "code", "777",
                "type", "PERCENTAGE",
                "discountValue", 20.00,
                "startDate", "2024-11-13",
                "endDate", "2028-11-13",
                "maxUses", 50,
                "usesCount", 0
        );

        ResponseEntity<String> addResp = restTemplate.postForEntity("/api/promotion/" + promotionId + "/discount/", new HttpEntity<>(discount1, headers), String.class);
        assertEquals(HttpStatus.CREATED, addResp.getStatusCode());
        Map<String, Object> added = mapper.readValue(addResp.getBody(), new TypeReference<>() {});
        assertEquals("777", added.get("code"));

        // agregar otro discount y luego actualizarlo
        Map<String, Object> discount2 = Map.of(
                "code", "555",
                "type", "PERCENTAGE",
                "discountValue", 20.00,
                "startDate", "2024-11-13",
                "endDate", "2028-11-13",
                "maxUses", 50,
                "usesCount", 0
        );
        ResponseEntity<String> add2Resp = restTemplate.postForEntity("/api/promotion/" + promotionId + "/discount/", new HttpEntity<>(discount2, headers), String.class);
        assertEquals(HttpStatus.CREATED, add2Resp.getStatusCode());
        Map<String, Object> added2 = mapper.readValue(add2Resp.getBody(), new TypeReference<>() {});
        assertEquals("555", added2.get("code"));

        Map<String, Object> discountUpdate = Map.of(
                "code", "555",
                "type", "PERCENTAGE",
                "discountValue", 10,
                "startDate", "2024-11-13",
                "endDate", "2029-11-13",
                "maxUses", 1000,
                "usesCount", 1
        );

        HttpEntity<Map<String, Object>> updateRequest = new HttpEntity<>(discountUpdate, headers);

        ResponseEntity<String> updateResp = restTemplate.exchange(
                "/api/promotion/" + promotionId + "/discount/2",
                HttpMethod.PUT,
                updateRequest,
                String.class
        );

        assertEquals(HttpStatus.OK, updateResp.getStatusCode());

        // parsear el body a Map y comprobar el valor numérico de discountValue
        Map<String, Object> updated = mapper.readValue(updateResp.getBody(), new com.fasterxml.jackson.core.type.TypeReference<>() {});
        Object dv = updated.get("discountValue");
        assertNotNull(dv);
        int dvInt = (dv instanceof Number) ? ((Number) dv).intValue() : Integer.parseInt(dv.toString());
        assertEquals(10, dvInt);

        // listar descuentos de la promoción
        ResponseEntity<String> listResp = restTemplate.exchange("/api/promotion/" + promotionId + "/discounts", HttpMethod.GET, new HttpEntity<>(headers), String.class);
        assertEquals(HttpStatus.OK, listResp.getStatusCode());
        List<Map<String, Object>> discounts = mapper.readValue(listResp.getBody(), new TypeReference<>() {});
        assertFalse(discounts.isEmpty());

        // borrar descuento actualizado
        ResponseEntity<Void> deleteDiscount = restTemplate.exchange("/api/promotion/" + promotionId + "/discount/2", HttpMethod.DELETE, new HttpEntity<>(headers), Void.class);
        assertTrue(deleteDiscount.getStatusCode().is2xxSuccessful());
    }

    @Test
    public void testAnalyticsAndErrorExample() {
        HttpHeaders headers = loginAndGetHeaders();

        ResponseEntity<String> analytics = restTemplate.exchange("/api/promotion/analytics", HttpMethod.GET, new HttpEntity<>(headers), String.class);
        assertEquals(HttpStatus.OK, analytics.getStatusCode());

        // error: promoción no existente
        ResponseEntity<String> notFound = restTemplate.exchange("/api/promotion/99999", HttpMethod.GET, new HttpEntity<>(headers), String.class);
        assertTrue(notFound.getStatusCode().is4xxClientError());
    }
}


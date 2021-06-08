package com.springtest.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.springtest.models.Cuenta;
import com.springtest.models.TransaccionDto;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@Tag("integracion_rt")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ControllerRestTemplateTest {


    @Autowired
    private TestRestTemplate client;

    private ObjectMapper objectMapper;

    @LocalServerPort
    private int puerto;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }


    @Test
    @Order(1)
    void testTransferir() throws JsonProcessingException {
        TransaccionDto dto = new TransaccionDto();
        dto.setMonto(new BigDecimal("100"));
        dto.setCuentaDestinoId(2L);
        dto.setCuentaOrigenId(1L);
        dto.setBancoId(1L);

        final ResponseEntity<String> response = client.postForEntity("http://localhost:"+ puerto +"/api/cuentas/transferir", dto, String.class);

        System.out.println("el Puerto: " + puerto);

        String json = response.getBody();
        System.out.println(json);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
        assertNotNull(json);
        assertTrue(json.contains("Transferencia realizada con exito"));

        final JsonNode jsonNode = objectMapper.readTree(json);
        assertEquals("Transferencia realizada con exito", jsonNode.path("mensaje").asText());
        assertEquals(LocalDate.now().toString(), jsonNode.path("date").asText());
        assertEquals("100", jsonNode.path("transaccion").path("monto").asText());
        assertEquals(1L, jsonNode.path("transaccion").path("cuentaOrigenId").asLong());

        Map<String, Object> resp = new HashMap<>();
        resp.put("date", LocalDate.now().toString());
        resp.put("status", "OK");
        resp.put("mensaje", "Transferencia realizada con exito");
        resp.put("transaccion", dto);

        assertEquals(objectMapper.writeValueAsString(resp), json);
    }


    @Test
    @Order(2)
    void testDetalle() {
        final ResponseEntity<Cuenta> respuesta = client.getForEntity("http://localhost:" + puerto + "/api/cuentas/1", Cuenta.class);
        Cuenta cuenta = respuesta.getBody();

        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, respuesta.getHeaders().getContentType());
        assertNotNull(cuenta);
        assertEquals("christian", cuenta.getPersona());
        assertEquals(1L, cuenta.getId());
        assertEquals("900.00", cuenta.getSaldo().toPlainString());
        assertEquals(new Cuenta(1L, "christian", new BigDecimal("900.00")), cuenta);
    }


    @Test
    @Order(3)
    void testListar() throws JsonProcessingException {
        final ResponseEntity<Cuenta[]> respuesta = client.getForEntity("http://localhost:" + puerto + "/api/cuentas", Cuenta[].class);
        List<Cuenta> cuentas = Arrays.asList(Objects.requireNonNull(respuesta.getBody()));

        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, respuesta.getHeaders().getContentType());

        assertEquals("christian", cuentas.get(0).getPersona());
        assertEquals(1L, cuentas.get(0).getId());
        assertEquals("900.00", cuentas.get(0).getSaldo().toPlainString());

        assertEquals("juana", cuentas.get(1).getPersona());
        assertEquals(2L, cuentas.get(1).getId());
        assertEquals("2100.00", cuentas.get(1).getSaldo().toPlainString());
        assertEquals(2, cuentas.size());

        // otra forma
        JsonNode json = objectMapper.readTree(objectMapper.writeValueAsString(cuentas));
        assertEquals("christian", json.get(0).path("persona").asText());
        assertEquals(1L, json.get(0).path("id").asLong());
        assertEquals("900.0", json.get(0).path("saldo").asText());

        assertEquals("juana", json.get(1).path("persona").asText());
        assertEquals(2L, json.get(1).path("id").asLong());
        assertEquals("2100.0", json.get(1).path("saldo").asText());
    }


    @Test
    @Order(4)
    void testGuardar() {
        Cuenta cuenta = new Cuenta(null, "pepa", new BigDecimal("3800"));

        final ResponseEntity<Cuenta> respuesta = client.postForEntity("http://localhost:" + puerto + "/api/cuentas", cuenta, Cuenta.class);

        assertEquals(HttpStatus.CREATED, respuesta.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, respuesta.getHeaders().getContentType());

        Cuenta cuentaCreada = respuesta.getBody();
        assertNotNull(cuentaCreada);
        assertEquals(3L, cuentaCreada.getId());
        assertEquals("pepa", cuentaCreada.getPersona());
        assertEquals("3800", cuentaCreada.getSaldo().toPlainString());
    }

    @Test
    @Order(5)
    void testEliminar() {
        ResponseEntity<Cuenta[]> respuesta = client.getForEntity("http://localhost:" + puerto + "/api/cuentas", Cuenta[].class);
        List<Cuenta> cuentas = Arrays.asList(Objects.requireNonNull(respuesta.getBody()));
        assertEquals(3, cuentas.size());


        client.delete("http://localhost:" + puerto + "/api/cuentas/3");

        respuesta = client.getForEntity("http://localhost:" + puerto + "/api/cuentas", Cuenta[].class);
        cuentas = Arrays.asList(Objects.requireNonNull(respuesta.getBody()));
        assertEquals(2, cuentas.size());
    }
}











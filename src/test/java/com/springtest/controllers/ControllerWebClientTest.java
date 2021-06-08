package com.springtest.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.springtest.models.Cuenta;
import com.springtest.models.TransaccionDto;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;


@Tag("integracion_wc")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureWebTestClient
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ControllerWebClientTest {

    private ObjectMapper objectMapper;

    @Autowired
    private WebTestClient client;


    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }



    @Test
    @Order(1)
    void testTransferir() throws JsonProcessingException {
        // given
        TransaccionDto dto = new TransaccionDto();
        dto.setCuentaOrigenId(1L);
        dto.setCuentaDestinoId(2L);
        dto.setBancoId(1L);
        dto.setMonto(new BigDecimal("100"));

        Map<String, Object> response = new HashMap<>();
        response.put("date", LocalDate.now().toString());
        response.put("status", "OK");
        response.put("mensaje", "Transferencia realizada con exito");
        response.put("transaccion", dto);

        // when
        client
                .post()
                .uri("/api/cuentas/transferir")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(dto)
                .exchange()
        // then
                .expectStatus()
                .isOk()
                .expectHeader()
                .contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .consumeWith(respuesta -> {
                    try {
                        JsonNode json = objectMapper.readTree(respuesta.getResponseBody());
                        assertEquals("Transferencia realizada con exito", json.path("mensaje").asText());
                        assertEquals(1L, json.path("transaccion").path("cuentaOrigenId").asLong());
                        assertEquals(LocalDate.now().toString(), json.path("date").asText());
                        assertEquals("100", json.path("transaccion").path("monto").asText());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                })
                .jsonPath("$.mensaje").isNotEmpty()
                .jsonPath("$.mensaje").value(is("Transferencia realizada con exito"))
                .jsonPath("$.mensaje").value(valor -> assertEquals("Transferencia realizada con exito", valor))
                .jsonPath("$.mensaje").isEqualTo("Transferencia realizada con exito")
                .jsonPath("$.transaccion.cuentaOrigenId").isEqualTo(dto.getCuentaOrigenId())
                .jsonPath("$.date").isEqualTo(LocalDate.now().toString())
                .json(objectMapper.writeValueAsString(response));
    }


    @Test
    @Order(2)
    void testDetalle() throws JsonProcessingException {
        Cuenta cuenta = new Cuenta(1L,"christian", new BigDecimal("900"));


        client.get().uri("/api/cuentas/1")
                .exchange()
                .expectStatus()
                .isOk()
                .expectHeader()
                .contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.persona").isEqualTo("christian")
                .jsonPath("$.saldo").isEqualTo(900)
                .json(objectMapper.writeValueAsString(cuenta));
    }


    @Test
    @Order(3)
    void testDetalle2() {
        client.get().uri("/api/cuentas/2")
                .exchange()
                .expectStatus()
                .isOk()
                .expectHeader()
                .contentType(MediaType.APPLICATION_JSON)
                .expectBody(Cuenta.class)
                .consumeWith(res -> {
                    Cuenta cuenta = res.getResponseBody();
                    assert cuenta != null;
                    assertEquals("juana", cuenta.getPersona());
                    assertEquals("2100.00", cuenta.getSaldo().toPlainString());
                });
    }

    @Test
    @Order(4)
    void testListar() {
        client.get().uri("/api/cuentas")
                .exchange()
                .expectStatus()
                .isOk()
                .expectHeader()
                .contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$[0].persona").isEqualTo("christian")
                .jsonPath("$[0].id").isEqualTo(1)
                .jsonPath("$[0].saldo").isEqualTo(900)
                .jsonPath("$[1].persona").isEqualTo("juana")
                .jsonPath("$[1].id").isEqualTo(2)
                .jsonPath("$[1].saldo").isEqualTo(2100)
                .jsonPath("$").isArray()
                .jsonPath("$").value(hasSize(2));
    }



    @Test
    @Order(5)
    void testListar2() {
        client.get().uri("/api/cuentas")
                .exchange()
                .expectStatus()
                .isOk()
                .expectHeader()
                .contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(Cuenta.class)
                .consumeWith(res -> {
                    List<Cuenta> cuentas = res.getResponseBody();
                    assert cuentas != null;
                    assertEquals(2, cuentas.size());
                    assertEquals(1L, cuentas.get(0).getId());
                    assertEquals("christian", cuentas.get(0).getPersona());
                    assertEquals(900, cuentas.get(0).getSaldo().intValue());
                    assertEquals(2L, cuentas.get(1).getId());
                    assertEquals("juana", cuentas.get(1).getPersona());
                    assertEquals(2100, cuentas.get(1).getSaldo().intValue());
                })
                .hasSize(2);
    }


    @Test
    @Order(6)
    void testGuardar() {
        //Given
        Cuenta cuenta = new Cuenta(null, "pepe", new BigDecimal("3000"));

        //when
        client.post().uri("/api/cuentas")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(cuenta)
                .exchange()
        // then
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.id").isEqualTo("3")
                .jsonPath("$.persona").isEqualTo("pepe")
                .jsonPath("$.persona").value(is("pepe"))
                .jsonPath("$.saldo").isEqualTo(3000);
    }


    @Test
    @Order(7)
    void testGuardar2() {
        //Given
        Cuenta cuenta = new Cuenta(null, "pepa", new BigDecimal("4000"));

        //when
        client.post().uri("/api/cuentas")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(cuenta)
                .exchange()
                // then
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(Cuenta.class)
                .consumeWith(res -> {
                    Cuenta c = res.getResponseBody();
                    assert c != null;
                    assertEquals(4L, c.getId());
                    assertEquals("pepa", c.getPersona());
                    assertEquals("4000", c.getSaldo().toPlainString());
                });
    }


    @Test
    @Order(8)
    void testEliminar() {

        client.get().uri("/api/cuentas")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(Cuenta.class)
                .hasSize(4);

        client.delete().uri("/api/cuentas/3")
                .exchange()
                .expectStatus().isNoContent()
                .expectBody().isEmpty();

        client.get().uri("/api/cuentas")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(Cuenta.class)
                .hasSize(3);
    }
}
















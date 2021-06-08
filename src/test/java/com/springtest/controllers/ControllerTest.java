package com.springtest.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springtest.Datos;
import com.springtest.models.Cuenta;
import com.springtest.models.TransaccionDto;
import com.springtest.services.CuentaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CuentaController.class)
class ControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private CuentaService cuentaService;

    ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    void testDetalle() throws Exception {
        // Given
        when(cuentaService.findById(1L)).thenReturn(Datos.crearCuenta001().orElse(null));

        // When
        mvc.perform(get("/api/cuentas/1").contentType(MediaType.APPLICATION_JSON))
                // Then
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.persona").value("christian"))
                .andExpect(jsonPath("$.saldo").value("1000"));

        verify(cuentaService).findById(1L);
    }


    @Test
    void testTransferir() throws Exception {
        // Given (body)
        TransaccionDto dto = new TransaccionDto();
        dto.setCuentaOrigenId(1L);
        dto.setCuentaDestinoId(2L);
        dto.setMonto(new BigDecimal("100"));
        dto.setBancoId(1L);

        System.out.println(objectMapper.writeValueAsString(dto));

        Map<String, Object> response = new HashMap<>();
        response.put("date", LocalDate.now().toString());
        response.put("status", "OK");
        response.put("mensaje", "Transferencia realizada con exito");
        response.put("transaccion", dto);


        System.out.println(objectMapper.writeValueAsString(response));


        //When (ejecuta la consulta)
        mvc.perform(post("/api/cuentas/transferir")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))

                //then
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.date").value(LocalDate.now().toString()))
                .andExpect(jsonPath("$.mensaje").value("Transferencia realizada con exito"))
                .andExpect(jsonPath("$.transaccion.cuentaOrigenId").value(dto.getCuentaOrigenId()))
                .andExpect(content().json(objectMapper.writeValueAsString(response)));
    }


    @Test
    void testListar() throws Exception {
        List<Cuenta> cuentas = Arrays.asList(Datos.crearCuenta001().orElse(null),
                Datos.crearCuenta002().orElse(null));

        when(cuentaService.findAll()).thenReturn(cuentas);

        // when
        mvc.perform(get("/api/cuentas").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].persona").value("christian"))
                .andExpect(jsonPath("$[1].persona").value("juana"))
                .andExpect(jsonPath("$[0].saldo").value("1000"))
                .andExpect(jsonPath("$[1].saldo").value("2000"))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(content().json(objectMapper.writeValueAsString(cuentas)));

        verify(cuentaService).findAll();
    }


    @Test
    void testGuardar() throws Exception {
        // Given
        Cuenta cuenta = new Cuenta(null, "pepe", new BigDecimal("3000"));
        when(cuentaService.save(any())).then(invocation -> {
            Cuenta c = invocation.getArgument(0);
            c.setId(3L);
            return c;
        });

        // When
        mvc.perform(post("/api/cuentas").contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(cuenta)))
        //then
                .andExpect(status().isCreated()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(3)))
                .andExpect(jsonPath("$.persona", is("pepe")))
                .andExpect(jsonPath("$.saldo", is(3000)));

        verify(cuentaService).save(any());
    }



}




















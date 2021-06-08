package com.springtest;

import com.springtest.exceptions.DineroInsuficienteException;
import com.springtest.models.Banco;
import com.springtest.models.Cuenta;
import com.springtest.repositories.BancoRepository;
import com.springtest.repositories.CuentaRepository;
import com.springtest.services.CuentaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class ServicesTest {

    @MockBean
    CuentaRepository cuentaRepository;

    @MockBean
    BancoRepository bancoRepository;

    @Autowired
    CuentaService service;

    @BeforeEach
    void setUp() {
        //cuentaRepository = mock(CuentaRepository.class);
        //bancoRepository = mock(BancoRepository.class);
        //service = new CuentaServiceImpl(cuentaRepository,bancoRepository);
    }

    @Test
    void contextLoads() {
        //GIVEN
        when(cuentaRepository.findById(1L)).thenReturn(Datos.crearCuenta001());
        when(cuentaRepository.findById(2L)).thenReturn(Datos.crearCuenta002());
        when(bancoRepository.findById(1L)).thenReturn(Datos.crearBanco());

        BigDecimal saldoOrigen = service.revisarSaldo(1L);
        BigDecimal saldoDestino = service.revisarSaldo(2L);

        assertEquals("1000", saldoOrigen.toPlainString());
        assertEquals("2000", saldoDestino.toPlainString());

        service.transferir(1L, 2L, new BigDecimal("100"), 1L);

        saldoOrigen = service.revisarSaldo(1L);
        saldoDestino = service.revisarSaldo(2L);

        assertEquals("900", saldoOrigen.toPlainString());
        assertEquals("2100", saldoDestino.toPlainString());

        int totalTransferenecias = service.revisarTotalTransferencias(1L);
        assertEquals(1, totalTransferenecias);

        verify(cuentaRepository, times(3)).findById(1L);
        verify(cuentaRepository, times(3)).findById(2L);
        verify(cuentaRepository,times(2)).save(any(Cuenta.class));

        verify(bancoRepository, times(2)).findById(1L);
        verify(bancoRepository).save(any(Banco.class));
    }



    @Test
    void contextLoadsError() {
        //GIVEN
        when(cuentaRepository.findById(1L)).thenReturn(Datos.crearCuenta001());
        when(cuentaRepository.findById(2L)).thenReturn(Datos.crearCuenta002());
        when(bancoRepository.findById(1L)).thenReturn(Datos.crearBanco());

        BigDecimal saldoOrigen = service.revisarSaldo(1L);
        BigDecimal saldoDestino = service.revisarSaldo(2L);

        assertEquals("1000", saldoOrigen.toPlainString());
        assertEquals("2000", saldoDestino.toPlainString());

        assertThrows(DineroInsuficienteException.class, () -> {
            service.transferir(1L, 2L, new BigDecimal("1200"), 1L);
        });

        saldoOrigen = service.revisarSaldo(1L);
        saldoDestino = service.revisarSaldo(2L);

        assertEquals("1000", saldoOrigen.toPlainString());
        assertEquals("2000", saldoDestino.toPlainString());

        int totalTransferenecias = service.revisarTotalTransferencias(1L);
        assertEquals(0, totalTransferenecias);

        verify(cuentaRepository, times(3)).findById(1L);
        verify(cuentaRepository, times(2)).findById(2L);
        verify(cuentaRepository, never()).save(any(Cuenta.class));

        verify(bancoRepository, times(1)).findById(1L);
        verify(bancoRepository, never()).save(any(Banco.class));
    }


    @Test
    void testFindAll() {
        //Given
        List<Cuenta> datos = Arrays.asList(Datos.crearCuenta001().orElse(null),
                Datos.crearCuenta002().orElse(null));

        when(cuentaRepository.findAll()).thenReturn(datos);


        //when
        List<Cuenta> cuentas = service.findAll();

        //then
        assertFalse(cuentas.isEmpty());
        assertEquals(2, cuentas.size());
        assertTrue(cuentas.contains(Datos.crearCuenta002().orElse(null)));

        verify(cuentaRepository).findAll();
    }

    @Test
    void testFindById(){
        //Given
        Cuenta dato = Datos.crearCuenta001().orElse(null);

        when(cuentaRepository.findById(anyLong())).thenReturn(java.util.Optional.ofNullable(dato));

        // when
        Cuenta cuenta = service.findById(1L);

        //then
        assertNotNull(cuenta);
        assertEquals("christian", cuenta.getPersona());
        assertEquals("1000", cuenta.getSaldo().toPlainString());

        verify(cuentaRepository).findById(1L);
    }


    @Test
    void testSave() {
        Cuenta cuentaPepe = new Cuenta(null, "pepe", new BigDecimal("3000"));
        when(cuentaRepository.save(any())).then(invocation -> {
            Cuenta c = invocation.getArgument(0);
            c.setId(3L);
            return c;
        });

        // when
        Cuenta cuenta = service.save(cuentaPepe);

        // then
        assertEquals("pepe", cuenta.getPersona());
        assertEquals(3, cuenta.getId());
        assertEquals("3000", cuenta.getSaldo().toPlainString());

        verify(cuentaRepository).save(any());
    }
}

package com.springtest;

import com.springtest.models.Cuenta;
import com.springtest.repositories.CuentaRepository;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Tag("integracion_jpa")
@DataJpaTest
public class RepositoriesTest {

    @Autowired
    CuentaRepository cuentaRepository;

    @Test
    void testFindById() {
        Optional<Cuenta> cuenta = cuentaRepository.findById(1L);
        assertTrue(cuenta.isPresent());
        assertEquals("christian", cuenta.orElse(null).getPersona());
    }

    @Test
    void testFindByPersona() {
        Optional<Cuenta> cuenta = cuentaRepository.findByPersona("christian");
        assertTrue(cuenta.isPresent());
        assertEquals("christian", cuenta.orElse(null).getPersona());
        assertEquals("1000.00", cuenta.orElse(null).getSaldo().toPlainString());
    }

    @Test
    void testFindByPersonaThrowException() {
        Optional<Cuenta> cuenta = cuentaRepository.findByPersona("rodri");
        assertThrows(NoSuchElementException.class, () -> {
            System.out.println(cuenta.get().getPersona());
        });
        assertFalse(cuenta.isPresent());
    }


    @Test
    void testFindAll() {
        List<Cuenta> cuentas = cuentaRepository.findAll();

        assertFalse(cuentas.isEmpty());
        assertEquals(2, cuentas.size());
    }

    @Test
    void testSave() {
        //given
        Cuenta cuentaP = new Cuenta(null, "car", new BigDecimal("3000"));
        cuentaRepository.save(cuentaP);

        //when
        Cuenta cuenta = cuentaRepository.findByPersona("car").orElse(null);

        //then
        assertNotNull(cuenta);
        assertEquals("car", cuenta.getPersona());
        assertEquals("3000", cuenta.getSaldo().toPlainString());
        assertEquals(3, cuenta.getId());
    }


    @Test
    void testUpdate() {
        //given
        Cuenta cuentaP = new Cuenta(null, "car", new BigDecimal("3000"));

        //when
        Cuenta cuenta = cuentaRepository.save(cuentaP);

        //then
        assertNotNull(cuenta);
        assertEquals("car", cuenta.getPersona());
        assertEquals("3000", cuenta.getSaldo().toPlainString());

        cuenta.setSaldo(new BigDecimal("3800"));
        final Cuenta cuentaActualizada = cuentaRepository.save(cuenta);

        //then
        assertNotNull(cuentaActualizada);
        assertEquals("car", cuentaActualizada.getPersona());
        assertEquals("3800", cuentaActualizada.getSaldo().toPlainString());
    }


    @Test
    void testDelete() {
        Cuenta cuenta = cuentaRepository.findById(2L).orElse(null);
        assertNotNull(cuenta);
        assertEquals("juana", cuenta.getPersona());

        cuentaRepository.delete(cuenta);

        assertThrows(NoSuchElementException.class, ()-> {
            cuentaRepository.findByPersona("juana").get().getPersona();
        });

        assertEquals(1, cuentaRepository.findAll().size());
    }
}

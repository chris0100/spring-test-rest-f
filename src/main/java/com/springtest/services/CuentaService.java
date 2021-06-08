package com.springtest.services;

import com.springtest.models.Cuenta;

import java.math.BigDecimal;
import java.util.List;

public interface CuentaService {

    Cuenta findById(Long id);
    int revisarTotalTransferencias(Long bancoId);
    BigDecimal revisarSaldo(Long cuentaId);
    void transferir(Long numCuentaOrigen, Long numCuentaDestino, BigDecimal monto, Long bancoId);
    List<Cuenta> findAll();
    Cuenta save(Cuenta cuenta);
    void deleteById(Long id);
}

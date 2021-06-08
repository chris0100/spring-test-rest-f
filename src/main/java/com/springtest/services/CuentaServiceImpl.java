package com.springtest.services;

import com.springtest.models.Banco;
import com.springtest.models.Cuenta;
import com.springtest.repositories.BancoRepository;
import com.springtest.repositories.CuentaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CuentaServiceImpl implements CuentaService {

    @Autowired
    private CuentaRepository cuentaRepository;

    @Autowired
    private BancoRepository bancoRepository;


    @Override
    @Transactional(readOnly = true)
    public Cuenta findById(Long id) {
        return cuentaRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public int revisarTotalTransferencias(Long bancoId) {
        Banco banco = bancoRepository.findById(bancoId).orElse(null);
        return banco.getTotalTransferencias();
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal revisarSaldo(Long cuentaId) {
        Cuenta cuenta = cuentaRepository.findById(cuentaId).orElse(null);
        return cuenta.getSaldo();
    }

    @Override
    @Transactional
    public void transferir(Long numCuentaOrigen, Long numCuentaDestino, BigDecimal monto, Long bancoId) {
        Cuenta cuentaOrigen = cuentaRepository.findById(numCuentaOrigen).orElse(null);
        cuentaOrigen.debito(monto);
        cuentaRepository.save(cuentaOrigen);

        Cuenta cuentaDestino = cuentaRepository.findById(numCuentaDestino).orElse(null);
        cuentaDestino.credito(monto);
        cuentaRepository.save(cuentaDestino);

        Banco banco = bancoRepository.findById(bancoId).orElse(null);
        int totalTransferencia = banco.getTotalTransferencias();
        banco.setTotalTransferencias(++totalTransferencia);
        bancoRepository.save(banco);
    }


    @Override
    @Transactional(readOnly = true)
    public List<Cuenta> findAll() {
        return cuentaRepository.findAll();
    }

    @Override
    @Transactional
    public Cuenta save(Cuenta cuenta) {
        return cuentaRepository.save(cuenta);
    }


    @Override
    @Transactional
    public void deleteById(Long id) {
        cuentaRepository.deleteById(id);
    }
}

package com.springtest.models;

import com.springtest.exceptions.DineroInsuficienteException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;


@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name = "cuentas")
public class Cuenta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String persona;
    private BigDecimal saldo;


    public void debito(BigDecimal monto){
        BigDecimal nuevoSaldo = this.saldo.subtract(monto);
        if (nuevoSaldo.compareTo(BigDecimal.ZERO) < 0) {
            throw new DineroInsuficienteException("Dinero Insuficiente en la cuenta");
        }
        this.saldo = nuevoSaldo;
    }

    public void credito(BigDecimal monto){
        this.saldo = this.saldo.add(monto);
    }

}

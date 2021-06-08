package com.springtest.controllers;

import com.springtest.models.Cuenta;
import com.springtest.models.TransaccionDto;
import com.springtest.services.CuentaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import static org.springframework.http.HttpStatus.*;

@RestController
@RequestMapping("/api/cuentas")
public class CuentaController {

    @Autowired
    private CuentaService cuentaService;

    @GetMapping("/{id}")
    public ResponseEntity detalle(@PathVariable("id") Long id){
        Cuenta cuenta = null;
        try{
            cuenta = cuentaService.findById(id);
        } catch (NoSuchElementException e){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(cuenta);
    }

    @PostMapping("/transferir")
    public ResponseEntity<?> transferir(@RequestBody TransaccionDto dto){
        cuentaService.transferir(dto.getCuentaOrigenId(), dto.getCuentaDestinoId(), dto.getMonto(), dto.getBancoId());

        Map<String, Object> response = new HashMap<>();
        response.put("date", LocalDate.now().toString());
        response.put("status", "OK");
        response.put("mensaje", "Transferencia realizada con exito");
        response.put("transaccion", dto);

        return ResponseEntity.ok(response);
    }


    @GetMapping
    @ResponseStatus(OK)
    public List<Cuenta> listar(){
        return cuentaService.findAll();
    }


    @PostMapping
    @ResponseStatus(CREATED)
    public Cuenta guardar(@RequestBody Cuenta cuenta){
        return cuentaService.save(cuenta);
    }



    @DeleteMapping("/{id}")
    @ResponseStatus(NO_CONTENT)
    public void eliminar(@PathVariable("id") Long id){
        cuentaService.deleteById(id);
    }


}














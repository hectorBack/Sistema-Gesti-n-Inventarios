package com.comision.CFE.Controllers;

import com.comision.CFE.DTO.TrabajadorRequestDTO;
import com.comision.CFE.DTO.TrabajadorResponseDTO;
import com.comision.CFE.Services.TrabajadorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@RestController
@RequestMapping("/api/trabajadores")
@RequiredArgsConstructor
public class TrabajadorController {

    private final TrabajadorService trabajadorService;


    // 1. LISTAR TODOS
    @GetMapping
    public ResponseEntity<List<TrabajadorResponseDTO>> listar() {
        return ResponseEntity.ok(trabajadorService.listarTodos());
    }

    // 2. OBTENER POR ID
    @GetMapping("/{id}")
    public ResponseEntity<TrabajadorResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(trabajadorService.buscarPorId(id));
    }

    // 3. GUARDAR (POST)
    @PostMapping
    public ResponseEntity<TrabajadorResponseDTO> guardar(@RequestBody TrabajadorRequestDTO request) {
        TrabajadorResponseDTO nuevo = trabajadorService.guardar(request);
        return new ResponseEntity<>(nuevo, HttpStatus.CREATED);
    }

    // 4. ACTUALIZAR (PUT)
    @PutMapping("/{id}")
    public ResponseEntity<TrabajadorResponseDTO> actualizar(
            @PathVariable Long id,
            @RequestBody TrabajadorRequestDTO request) {
        return ResponseEntity.ok(trabajadorService.actualizar(id, request));
    }

    // 5. ELIMINAR (DELETE)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        trabajadorService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

}

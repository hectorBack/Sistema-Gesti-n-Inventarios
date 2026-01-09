package com.comision.CFE.Controllers;

import com.comision.CFE.DTO.MaterialRequestDTO;
import com.comision.CFE.DTO.MaterialResponseDTO;
import com.comision.CFE.Services.MaterialService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/materiales")
@RequiredArgsConstructor
public class MaterialController {

    private final MaterialService materialService;

    @GetMapping
    public ResponseEntity<Page<MaterialResponseDTO>> listarMateriales(
            @RequestParam(required = false) String filtro,
            @RequestParam(required = false) String categoria,
            @RequestParam(defaultValue = "false") boolean critico,
            @RequestParam(defaultValue = "0") int page) {

        Page<MaterialResponseDTO> paginaMateriales = materialService.listarConFiltros(filtro, categoria, critico, page);

        // Devolvemos el objeto Page completo (trae datos, total de páginas, etc.)
        return ResponseEntity.ok(paginaMateriales);
    }

    // OBTENER UNO SOLO
    @GetMapping("/{codigo}")
    public ResponseEntity<MaterialResponseDTO> obtenerUno(@PathVariable String codigo) {
        return ResponseEntity.ok(materialService.buscarPorCodigo(codigo));
    }


    @PostMapping
    public ResponseEntity<MaterialResponseDTO> guardar(@RequestBody MaterialRequestDTO request) {
        // @RequestBody es vital para recibir JSON desde el cliente
        MaterialResponseDTO creado = materialService.crear(request);
        return new ResponseEntity<>(creado, HttpStatus.CREATED);
    }

    // ACTUALIZAR (PUT)
    @PutMapping("/{codigo}")
    public ResponseEntity<MaterialResponseDTO> actualizar(@PathVariable String codigo, @RequestBody MaterialRequestDTO request) {
        return ResponseEntity.ok(materialService.actualizar(codigo, request));
    }

    // ELIMINAR (DELETE)
    @DeleteMapping("/{codigo}")
    public ResponseEntity<Void> eliminar(@PathVariable String codigo) {
        materialService.eliminar(codigo);
        return ResponseEntity.noContent().build(); // Devuelve un 204 No Content (éxito sin cuerpo)
    }
}

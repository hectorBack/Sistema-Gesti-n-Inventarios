package com.comision.CFE.Controllers;

import com.comision.CFE.DTO.MovimientoRequestDTO;
import com.comision.CFE.DTO.MovimientoResponseDTO;
import com.comision.CFE.Entity.Movimiento;
import com.comision.CFE.Services.MaterialService;
import com.comision.CFE.Services.MovimientoService;
import com.comision.CFE.Services.ReporteService;
import com.comision.CFE.Services.TrabajadorService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController // Cambiado de @Controller
@RequestMapping("/api/movimientos")
@RequiredArgsConstructor
public class MovimientoController {

    private final MovimientoService movimientoService;
    private final MaterialService materialService;
    private final TrabajadorService trabajadorService;
    private final ReporteService reporteService;

    // 3. OBTENER UN MOVIMIENTO POR ID
    @GetMapping("/{id}")
    public ResponseEntity<MovimientoResponseDTO> obtenerPorId(@PathVariable Long id) {
        // Devuelve 200 OK con el movimiento encontrado
        return ResponseEntity.ok(movimientoService.obtenerPorId(id));
    }

    @PostMapping
    public ResponseEntity<?> guardarMovimiento(@RequestBody MovimientoRequestDTO request) {
        try {
            // Registramos el movimiento y devolvemos el resultado
            MovimientoResponseDTO resultado = movimientoService.registrar(request);
            return new ResponseEntity<>(resultado, HttpStatus.CREATED);
        } catch (Exception e) {
            // En REST, si hay un error (ej. Stock insuficiente), devolvemos un 400 Bad Request
            // con el mensaje de error en formato JSON
            Map<String, String> error = new HashMap<>();
            error.put("mensaje", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping
    public ResponseEntity<Page<MovimientoResponseDTO>> verDetalleMovimientos(
            @RequestParam(required = false) String filtro,
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam(defaultValue = "0") int page) {

        // Obtenemos la página de movimientos desde el servicio
        Page<MovimientoResponseDTO> paginaMov = movimientoService.listarHistorialFiltrado(filtro, tipo, fecha, page);

        // Devolvemos la página completa (incluye datos y metadata de paginación)
        return ResponseEntity.ok(paginaMov);
    }

    // 4. ACTUALIZAR (Normalmente solo para corrección de datos como el Trabajador)
    @PutMapping("/{id}")
    public ResponseEntity<MovimientoResponseDTO> actualizar(
            @PathVariable Long id,
            @RequestBody MovimientoRequestDTO request) {

        // Llamamos al método que creamos en el Service
        MovimientoResponseDTO actualizado = movimientoService.actualizarComentarioOTrabajador(id, request);
        return ResponseEntity.ok(actualizado);
    }

    // 5. ELIMINAR (Con reversión de stock automática)
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> eliminar(@PathVariable Long id) {
        try {
            movimientoService.eliminar(id);
            // En una API es buena práctica confirmar la acción
            Map<String, String> respuesta = new HashMap<>();
            respuesta.put("mensaje", "Movimiento eliminado y stock revertido correctamente");
            return ResponseEntity.ok(respuesta); // O ResponseEntity.noContent().build()
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "No se pudo eliminar: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/exportar/pdf")
    public void exportarAPdf(HttpServletResponse response) throws IOException {
        response.setContentType("application/pdf");
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=movimientos_" + LocalDate.now() + ".pdf";
        response.setHeader(headerKey, headerValue);

        List<Movimiento> movimientos = movimientoService.obtenerEntidadesParaReporte();
        reporteService.exportarMovimientosPDF(response, movimientos);
    }
}

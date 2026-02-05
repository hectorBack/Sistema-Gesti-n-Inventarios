package com.comision.CFE.Services;

import com.comision.CFE.DTO.MovimientoRequestDTO;
import com.comision.CFE.DTO.MovimientoResponseDTO;
import com.comision.CFE.Entity.Movimiento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface MovimientoService {

    MovimientoResponseDTO registrar(MovimientoRequestDTO request);
    List<MovimientoResponseDTO> listarHistorial();
    Page<MovimientoResponseDTO> listarHistorialFiltrado(String filtro, String tipo, LocalDate fecha, int pagina);

    MovimientoResponseDTO obtenerPorId(Long id);
    void eliminar(Long id);
    // Nota: La actualización de movimientos es delicada, se recomienda solo para errores de dedo (ej. el nombre del trabajador)
    MovimientoResponseDTO actualizarComentarioOTrabajador(Long id, MovimientoRequestDTO request);

    List<Movimiento> obtenerEntidadesParaReporte();

}

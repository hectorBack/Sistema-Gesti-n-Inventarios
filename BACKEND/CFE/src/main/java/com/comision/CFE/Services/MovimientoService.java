package com.comision.CFE.Services;

import com.comision.CFE.DTO.MovimientoRequestDTO;
import com.comision.CFE.DTO.MovimientoResponseDTO;
import org.springframework.data.domain.Page;

import java.util.List;

public interface MovimientoService {

    MovimientoResponseDTO registrar(MovimientoRequestDTO request);
    List<MovimientoResponseDTO> listarHistorial();
    Page<MovimientoResponseDTO> listarHistorialFiltrado(String filtro, String tipo, int pagina);

    MovimientoResponseDTO obtenerPorId(Long id);
    void eliminar(Long id);
    // Nota: La actualización de movimientos es delicada, se recomienda solo para errores de dedo (ej. el nombre del trabajador)
    MovimientoResponseDTO actualizarComentarioOTrabajador(Long id, MovimientoRequestDTO request);
}

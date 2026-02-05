package com.comision.CFE.Services.impl;

import com.comision.CFE.DTO.MovimientoRequestDTO;
import com.comision.CFE.DTO.MovimientoResponseDTO;
import com.comision.CFE.Entity.Material;
import com.comision.CFE.Entity.Movimiento;
import com.comision.CFE.Entity.TipoMovimiento;
import com.comision.CFE.Entity.Trabajador;
import com.comision.CFE.Exception.ResourceNotFoundException;
import com.comision.CFE.Repository.MaterialRepository;
import com.comision.CFE.Repository.MovimientoRepository;
import com.comision.CFE.Repository.TrabajadorRepository;
import com.comision.CFE.Services.MaterialService;
import com.comision.CFE.Services.MovimientoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MovimientoServiceImpl implements MovimientoService {

    private final MovimientoRepository movimientoRepository;
    private final MaterialService materialService; // Inyectamos la interfaz del servicio
    private final MaterialRepository materialRepository;
    private final TrabajadorRepository trabajadorRepository;

    @Override
    @Transactional
    public MovimientoResponseDTO registrar(MovimientoRequestDTO request) {
        // 1. Validar y actualizar stock
        materialService.actualizarStock(request.getMaterialCodigo(), request.getCantidad(), request.getTipo());

        // 2. Obtener objetos necesarios
        Material material = materialRepository.findById(request.getMaterialCodigo())
                .orElseThrow(() -> new ResourceNotFoundException("Material no encontrado"));

        Trabajador trabajador = trabajadorRepository.findByNombreCompleto(request.getTrabajador())
                .orElseThrow(() -> new ResourceNotFoundException("Trabajador no encontrado"));

        // 3. Crear y guardar el movimiento
        Movimiento movimiento = new Movimiento();
        movimiento.setMaterial(material);
        movimiento.setCantidad(request.getCantidad());
        movimiento.setTrabajador(trabajador);
        movimiento.setTipo(TipoMovimiento.valueOf(request.getTipo().toUpperCase()));
        movimiento.setFechaRegistro(LocalDateTime.now());

        // GUARDAMOS Y MAPEAMOS PARA RETORNAR
        Movimiento entidadGuardada = movimientoRepository.save(movimiento);
        return mapToResponseDTO(entidadGuardada);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovimientoResponseDTO> listarHistorial() {
        // Ordenar por ID o Fecha descendente para ver lo último arriba
        return movimientoRepository.findAll(Sort.by(Sort.Direction.DESC, "id"))
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MovimientoResponseDTO> listarHistorialFiltrado(String filtro, String tipo, LocalDate fecha, int pagina) {
        String search = (filtro != null) ? filtro : "";

        TipoMovimiento tipoEnum = null;
        if (tipo != null && !tipo.isEmpty()) {
            try {
                tipoEnum = TipoMovimiento.valueOf(tipo.toUpperCase());
            } catch (IllegalArgumentException e) {
                tipoEnum = null;
            }
        }

        // --- CAMBIO CLAVE: Lógica de Fechas ---
        LocalDateTime fechaInicio = null;
        LocalDateTime fechaFin = null;

        if (fecha != null) {
            fechaInicio = fecha.atStartOfDay(); // 2024-05-20T00:00:00
            fechaFin = fecha.atTime(LocalTime.MAX); // 2024-05-20T23:59:59.999
        }

        // Definimos 10 registros por página, ordenados por fecha descendente
        Pageable pageable = PageRequest.of(pagina, 10, Sort.by(Sort.Direction.DESC, "fechaRegistro"));

        // --- CAMBIO CLAVE: Enviamos fechaInicio y fechaFin al Repository ---
        return movimientoRepository.buscarConFiltros(search, tipoEnum, fechaInicio, fechaFin, pageable)
                .map(this::mapToResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public MovimientoResponseDTO obtenerPorId(Long id) {
        Movimiento mov = movimientoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movimiento no encontrado con ID: " + id));
        return mapToResponseDTO(mov);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        Movimiento mov = movimientoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se puede eliminar. ID no existe: " + id));

        // LÓGICA DE REVERSIÓN DE STOCK
        // Si borramos una SALIDA, el material recupera stock. Si borramos un INGRESO, pierde stock.
        String tipoReversion = mov.getTipo().name().equalsIgnoreCase("INGRESO") ? "SALIDA" : "INGRESO";

        materialService.actualizarStock(
                mov.getMaterial().getCodigo(),
                mov.getCantidad(),
                tipoReversion
        );

        movimientoRepository.delete(mov);
    }

    @Override
    @Transactional
    public MovimientoResponseDTO actualizarComentarioOTrabajador(Long id, MovimientoRequestDTO request) {
        Movimiento mov = movimientoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movimiento no encontrado"));

        // Solo permitimos cambiar el trabajador, no la cantidad ni el tipo
        // (Cambiar cantidad requeriría una lógica de recalculación compleja)
        Trabajador nuevoTrabajador = trabajadorRepository.findByNombreCompleto(request.getTrabajador())
                .orElseThrow(() -> new ResourceNotFoundException("Trabajador no encontrado"));

        mov.setTrabajador(nuevoTrabajador);

        return mapToResponseDTO(movimientoRepository.save(mov));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Movimiento> obtenerEntidadesParaReporte() {
        return movimientoRepository.findAll(Sort.by(Sort.Direction.DESC, "fechaRegistro"));
    }


    private MovimientoResponseDTO mapToResponseDTO(Movimiento entity) {
        MovimientoResponseDTO dto = new MovimientoResponseDTO();
        dto.setId(entity.getId());
        dto.setMaterialCodigo(entity.getMaterial().getCodigo());
        dto.setMaterialNombre(entity.getMaterial().getNombre());
        dto.setCantidad(entity.getCantidad());

        if (entity.getTrabajador() != null) {
            dto.setTrabajador(entity.getTrabajador().getNombreCompleto());
        }

        // FINGIMOS UN TEXTO PLANO PARA EVITAR ERRORES DE ENUM
        // Usamos name() y nos aseguramos de que no sea nulo
        dto.setTipo(entity.getTipo() != null ? entity.getTipo().name() : "");

        // Formateo de fecha más amigable (opcional pero recomendado)
        dto.setFecha(entity.getFechaRegistro().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")));

        return dto;
    }
}

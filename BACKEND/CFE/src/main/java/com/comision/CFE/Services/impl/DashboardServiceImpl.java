package com.comision.CFE.Services.impl;

import com.comision.CFE.DTO.DashboardResponseDTO;
import com.comision.CFE.Repository.MaterialRepository;
import com.comision.CFE.Repository.MovimientoRepository;
import com.comision.CFE.Repository.TrabajadorRepository;
import com.comision.CFE.Services.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final MaterialRepository materialRepository;
    private final MovimientoRepository movimientoRepository;
    private final TrabajadorRepository trabajadorRepository;

    @Override
    @Transactional(readOnly = true)
    public DashboardResponseDTO obtenerResumen() {
        LocalDateTime inicioHoy = LocalDate.now().atStartOfDay();

        // Consultas al Repo
        long totalMat = materialRepository.count();
        long stockCrit = materialRepository.countStockCritico();
        long totalTrab = trabajadorRepository.count();
        long movHoy = movimientoRepository.countMovimientosDelDia(inicioHoy);

        // Mapeo limpio a la clase interna
        List<DashboardResponseDTO.MovimientoRecienteDTO> recientes = movimientoRepository.findTop5ByOrderByFechaRegistroDesc()
                .stream()
                .map(m -> new DashboardResponseDTO.MovimientoRecienteDTO(
                        m.getMaterial().getNombre(),
                        m.getTipo().toString(),
                        m.getCantidad(),
                        m.getTrabajador().getNombreCompleto()
                ))
                .collect(Collectors.toList());

        List<Map<String, Object>> porCategoria = materialRepository.countMaterialesPorCategoria();

        return DashboardResponseDTO.builder()
                .totalMateriales(totalMat)
                .stockCritico(stockCrit)
                .movimientosHoy(movHoy)
                .totalTrabajadores(totalTrab)
                .movimientosRecientes(recientes) // Aquí ya no hay error de tipos
                .stockPorCategoria(porCategoria)
                .build();
    }
}

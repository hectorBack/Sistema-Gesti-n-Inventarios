package com.comision.CFE.Repository;

import com.comision.CFE.Entity.Movimiento;
import com.comision.CFE.Entity.TipoMovimiento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface MovimientoRepository extends JpaRepository<Movimiento, Long> {

    // Para ver todos los movimientos de un material específico (ej. todo lo de Herr-001)
    List<Movimiento> findByMaterialCodigoOrderByFechaRegistroDesc(String codigo);

    // Para ver qué materiales ha sacado o metido un trabajador específico
    List<Movimiento> findByTrabajadorNombreCompleto(String nombreCompleto);

    // Para filtrar por tipo (solo ingresos o solo salidas)
    List<Movimiento> findByTipo(TipoMovimiento tipo);

    @Query("SELECT m FROM Movimiento m WHERE " +
            "(LOWER(m.material.nombre) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
            " LOWER(m.trabajador.nombreCompleto) LIKE LOWER(CONCAT('%', :filtro, '%'))) AND " +
            "(CAST(:tipo AS string) IS NULL OR m.tipo = :tipo) AND " +
            "(CAST(:inicio AS timestamp) IS NULL OR m.fechaRegistro >= :inicio) AND " +
            "(CAST(:fin AS timestamp) IS NULL OR m.fechaRegistro <= :fin)")
    Page<Movimiento> buscarConFiltros(
            @Param("filtro") String filtro,
            @Param("tipo") TipoMovimiento tipo,
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin,
            Pageable pageable);
}

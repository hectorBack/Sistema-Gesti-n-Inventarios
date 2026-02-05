package com.comision.CFE.Repository;

import com.comision.CFE.Entity.Material;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;

public interface MaterialRepository extends JpaRepository<Material, String> {

    List<Material> findByCategoria(String categoria);

    List<Material> findByNombreContainingIgnoreCase(String nombre);

    // Busca por nombre o código ignorando mayúsculas/minúsculas y que contenga el texto
    @Query("SELECT m FROM Material m WHERE " +
            "(LOWER(m.nombre) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
            "LOWER(m.codigo) LIKE LOWER(CONCAT('%', :filtro, '%'))) AND " +
            "(:categoria IS NULL OR m.categoria = :categoria) AND " +
            "(:soloCritico = false OR m.stock <= 5)")
    Page<Material> buscarConFiltros(@Param("filtro") String filtro,
                                    @Param("categoria") String categoria,
                                    @Param("soloCritico") boolean soloCritico,
                                    Pageable pageable);

    // Contar materiales con stock bajo (ejemplo < 5 unidades)
    @Query("SELECT COUNT(m) FROM Material m WHERE m.stock < 5")
    long countStockCritico();

    // Obtener datos para la gráfica de pastel
    @Query("SELECT new map(m.categoria as nombre, COUNT(m) as cantidad) " +
            "FROM Material m GROUP BY m.categoria")
    List<Map<String, Object>> countMaterialesPorCategoria();

}

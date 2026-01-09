package com.comision.CFE.Repository;

import com.comision.CFE.Entity.Material;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

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

}

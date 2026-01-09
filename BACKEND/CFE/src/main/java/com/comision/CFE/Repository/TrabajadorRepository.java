package com.comision.CFE.Repository;

import com.comision.CFE.Entity.Trabajador;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TrabajadorRepository extends JpaRepository<Trabajador, Long> {

    // Para buscar al trabajador por su nombre exacto al procesar un registro
    Optional<Trabajador> findByNombreCompleto(String nombreCompleto);
}

package com.comision.CFE.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Trabajador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nombreCompleto; //

    // Relación inversa opcional: un trabajador puede tener muchos movimientos registrados
    @OneToMany(mappedBy = "trabajador")
    private List<Movimiento> movimientos;
}

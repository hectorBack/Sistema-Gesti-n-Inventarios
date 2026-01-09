package com.comision.CFE.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Movimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "material_codigo")
    private Material material;

    private Integer cantidad; // Cantidad movida

    @ManyToOne
    @JoinColumn(name = "trabajador_id")
    private Trabajador trabajador;

    @Enumerated(EnumType.STRING)
    private TipoMovimiento tipo; // INGRESO o SALIDA

    private LocalDateTime fechaRegistro = LocalDateTime.now();

}

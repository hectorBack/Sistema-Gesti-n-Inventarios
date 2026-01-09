package com.comision.CFE.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
@Entity
@Data
public class Material {

    @Id
    private String codigo; // Ej: "Herr-001"

    @Column(nullable = false)
    private String nombre; // "Nombre del Material"

    @Column(nullable = false)
    private String categoria; // "Categoría" o "Tipo de Material"

    private Integer ingresos = 0; // Suma total de ingresos
    private Integer salidas = 0;  // Suma total de salidas
    private Integer stock = 0;    // Saldo actual
}

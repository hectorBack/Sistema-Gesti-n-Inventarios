package com.comision.CFE.DTO;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MaterialResponseDTO {

    private String codigo;
    private String nombre;
    private String categoria;
    private Integer ingresos;
    private Integer salidas;
    private Integer stock;
    private String estadoStock; // Útil para poner colores (ej: "Bajo", "Normal")
}

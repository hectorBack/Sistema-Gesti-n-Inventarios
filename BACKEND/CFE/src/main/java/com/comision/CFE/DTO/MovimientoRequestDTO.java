package com.comision.CFE.DTO;

import lombok.Data;

@Data
public class MovimientoRequestDTO {
    private String materialCodigo; // El código Herr-XXX
    private Integer cantidad;      // Cuánto entra o sale
    private String trabajador;    // Quién lo hace
    private String tipo;          // "INGRESO" o "SALIDA"
}

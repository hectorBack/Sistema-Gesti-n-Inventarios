package com.comision.CFE.DTO;

import lombok.Data;

@Data
public class MovimientoResponseDTO {

    private Long id;
    private String materialNombre;
    private String materialCodigo;
    private Integer cantidad;
    private String trabajador;
    private String tipo;
    private String fecha;
}

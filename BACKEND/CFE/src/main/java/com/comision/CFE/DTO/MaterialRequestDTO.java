package com.comision.CFE.DTO;

import lombok.Data;

@Data
public class MaterialRequestDTO {
    private String codigo;
    private String nombre;
    private String categoria;
    private Integer stockInicial;
}

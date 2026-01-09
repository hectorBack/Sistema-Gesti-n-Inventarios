package com.comision.CFE.DTO;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TrabajadorResponseDTO {
    private Long id;
    private String nombreCompleto;
}

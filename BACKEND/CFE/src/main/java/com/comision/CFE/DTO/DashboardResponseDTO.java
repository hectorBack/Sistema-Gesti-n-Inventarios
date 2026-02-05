package com.comision.CFE.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class DashboardResponseDTO {

    private long totalMateriales;
    private long stockCritico;      // Materiales con stock < 5
    private long movimientosHoy;    // Movimientos realizados hoy
    private long totalTrabajadores;

    // Eliminamos la lista de Map y dejamos solo esta:
    private List<MovimientoRecienteDTO> movimientosRecientes;

    // Mantenemos esta para la gráfica (esta sí suele ser Map porque es dinámica)
    private List<Map<String, Object>> stockPorCategoria;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor // Es buena práctica agregar el constructor vacío para JSON
    public static class MovimientoRecienteDTO {
        private String material;
        private String tipo;
        private Integer cantidad;
        private String trabajador;
    }
}

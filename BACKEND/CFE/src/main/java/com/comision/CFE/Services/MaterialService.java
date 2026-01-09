package com.comision.CFE.Services;

import com.comision.CFE.DTO.MaterialRequestDTO;
import com.comision.CFE.DTO.MaterialResponseDTO;
import org.springframework.data.domain.Page;


import java.util.List;

public interface MaterialService {

    List<MaterialResponseDTO> listarTodos();
    MaterialResponseDTO buscarPorCodigo(String codigo);
    MaterialResponseDTO crear(MaterialRequestDTO request);
    void actualizarStock(String codigo, Integer cantidad, String tipo);
    Page<MaterialResponseDTO> listarConFiltros(String filtro, String categoria, boolean soloCritico, int pagina);

    MaterialResponseDTO actualizar(String codigo, MaterialRequestDTO request); // Actualizar
    void eliminar(String codigo); // Eliminar
}

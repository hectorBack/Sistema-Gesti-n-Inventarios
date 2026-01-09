package com.comision.CFE.Services;

import com.comision.CFE.DTO.TrabajadorRequestDTO;
import com.comision.CFE.DTO.TrabajadorResponseDTO;

import java.util.List;

public interface TrabajadorService {

    List<TrabajadorResponseDTO> listarTodos();
    TrabajadorResponseDTO buscarPorId(Long id);
    TrabajadorResponseDTO buscarPorNombre(String nombre);
    TrabajadorResponseDTO guardar(TrabajadorRequestDTO request);

    TrabajadorResponseDTO actualizar(Long id, TrabajadorRequestDTO request);
    void eliminar(Long id);
}

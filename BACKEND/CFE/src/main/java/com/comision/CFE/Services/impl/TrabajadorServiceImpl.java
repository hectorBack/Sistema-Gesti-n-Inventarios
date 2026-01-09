package com.comision.CFE.Services.impl;

import com.comision.CFE.DTO.TrabajadorRequestDTO;
import com.comision.CFE.DTO.TrabajadorResponseDTO;
import com.comision.CFE.Entity.Trabajador;
import com.comision.CFE.Exception.ResourceNotFoundException;
import com.comision.CFE.Repository.TrabajadorRepository;
import com.comision.CFE.Services.TrabajadorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TrabajadorServiceImpl implements TrabajadorService {

    private final TrabajadorRepository trabajadorRepository;

    @Override
    @Transactional(readOnly = true)
    public List<TrabajadorResponseDTO> listarTodos() {
        return trabajadorRepository.findAll()
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public TrabajadorResponseDTO buscarPorId(Long id) {
        Trabajador trabajador = trabajadorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trabajador no encontrado con ID: " + id));
        return mapToResponseDTO(trabajador);
    }

    @Override
    @Transactional(readOnly = true)
    public TrabajadorResponseDTO buscarPorNombre(String nombre) {
        // Buscamos nombres como "Julian Hernandez Moreno"
        Trabajador trabajador = trabajadorRepository.findByNombreCompleto(nombre)
                .orElseThrow(() -> new ResourceNotFoundException("No existe el trabajador: " + nombre));
        return mapToResponseDTO(trabajador);
    }



    @Override
    @Transactional
    public TrabajadorResponseDTO guardar(TrabajadorRequestDTO request) {
        Trabajador trabajador = new Trabajador();
        trabajador.setNombreCompleto(request.getNombreCompleto());

        Trabajador guardado = trabajadorRepository.save(trabajador);
        return mapToResponseDTO(guardado);
    }

    @Override
    @Transactional
    public TrabajadorResponseDTO actualizar(Long id, TrabajadorRequestDTO request) {
        Trabajador trabajador = trabajadorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se puede actualizar. Trabajador no encontrado con ID: " + id));

        trabajador.setNombreCompleto(request.getNombreCompleto());

        Trabajador actualizado = trabajadorRepository.save(trabajador);
        return mapToResponseDTO(actualizado);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        if (!trabajadorRepository.existsById(id)) {
            throw new ResourceNotFoundException("No se puede eliminar. Trabajador no encontrado con ID: " + id);
        }
        // Nota: Si el trabajador tiene movimientos, Postgres lanzará un error de llave foránea.
        trabajadorRepository.deleteById(id);
    }

    // Método auxiliar para convertir Entidad a DTO
    private TrabajadorResponseDTO mapToResponseDTO(Trabajador entity) {
        return TrabajadorResponseDTO.builder()
                .id(entity.getId())
                .nombreCompleto(entity.getNombreCompleto())
                .build();
    }
}

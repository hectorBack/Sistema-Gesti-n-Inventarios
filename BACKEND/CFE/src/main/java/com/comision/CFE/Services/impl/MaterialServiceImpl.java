package com.comision.CFE.Services.impl;

import com.comision.CFE.DTO.MaterialRequestDTO;
import com.comision.CFE.DTO.MaterialResponseDTO;
import com.comision.CFE.Entity.Material;
import com.comision.CFE.Exception.InsufficientStockException;
import com.comision.CFE.Exception.ResourceNotFoundException;
import com.comision.CFE.Repository.MaterialRepository;
import com.comision.CFE.Services.MaterialService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MaterialServiceImpl implements MaterialService {

    private final MaterialRepository materialRepository;

    @Override
    @Transactional(readOnly = true)
    public List<MaterialResponseDTO> listarTodos() {
        // Usamos el repositorio para obtener todo, pero aplicando un Sort
        return materialRepository.findAll(Sort.by(Sort.Direction.ASC, "codigo"))
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public MaterialResponseDTO buscarPorCodigo(String codigo) {
        Material material = materialRepository.findById(codigo)
                .orElseThrow(() -> new ResourceNotFoundException("Material no encontrado: " + codigo));
        return mapToResponseDTO(material);
    }

    @Override
    @Transactional
    public MaterialResponseDTO crear(MaterialRequestDTO request) {
        Material material = new Material();
        material.setCodigo(request.getCodigo());
        material.setNombre(request.getNombre());
        material.setCategoria(request.getCategoria());

        Integer inicial = (request.getStockInicial() != null) ? request.getStockInicial() : 0;
        // Inicializamos valores en 0 para el nuevo material
        material.setIngresos(0);
        material.setSalidas(0);
        material.setStock(inicial);

        return mapToResponseDTO(materialRepository.save(material));
    }

    @Override
    @Transactional
    public void actualizarStock(String codigo, Integer cantidad, String tipo) {
        Material material = materialRepository.findById(codigo)
                .orElseThrow(() -> new ResourceNotFoundException("Material no encontrado"));

        if ("SALIDA".equalsIgnoreCase(tipo)) {
            // Validamos stock antes de permitir la salida
            if (material.getStock() < cantidad) {
                throw new InsufficientStockException("Stock insuficiente de: " + material.getNombre());
            }

            // REEMPLAZAMOS la cantidad (No sumamos)
            material.setSalidas(cantidad);
            material.setIngresos(0); // Opcional: Limpiamos ingresos para que se vea que el último fue salida

            // El stock SÍ debe restarse para mantener la coherencia de la bodega
            material.setStock(material.getStock() - cantidad);
        } else {
            // REEMPLAZAMOS la cantidad (No sumamos)
            material.setIngresos(cantidad);
            material.setSalidas(0); // Opcional: Limpiamos salidas para que se vea que el último fue ingreso

            // El stock SÍ debe sumarse
            material.setStock(material.getStock() + cantidad);
        }

        materialRepository.save(material);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MaterialResponseDTO> listarConFiltros(String filtro, String categoria, boolean soloCritico, int pagina) {
        String cat = (categoria != null && !categoria.isEmpty()) ? categoria : null;
        String search = (filtro != null) ? filtro : "";

        // Configuramos: página actual, 10 registros por página, y orden por código
        Pageable pageable = PageRequest.of(pagina, 10, Sort.by("codigo"));

        return materialRepository.buscarConFiltros(search, cat, soloCritico, pageable)
                .map(this::mapToResponseDTO); // El método map de Page convierte automáticamente
    }

    @Override
    @Transactional
    public MaterialResponseDTO actualizar(String codigo, MaterialRequestDTO request) {
        // 1. Buscamos si existe
        Material material = materialRepository.findById(codigo)
                .orElseThrow(() -> new ResourceNotFoundException("No se puede actualizar. Material no encontrado: " + codigo));

        // 2. Actualizamos los campos permitidos
        material.setNombre(request.getNombre());
        material.setCategoria(request.getCategoria());

        // Si permites actualizar el stock inicial desde aquí (ten cuidado con la lógica de negocio)
        if(request.getStockInicial() != null) {
            material.setStock(request.getStockInicial());
        }

        // 3. Guardamos y mapeamos a respuesta
        return mapToResponseDTO(materialRepository.save(material));
    }

    @Override
    @Transactional
    public void eliminar(String codigo) {
        // Verificamos existencia antes de borrar para lanzar error 404 si no existe
        if (!materialRepository.existsById(codigo)) {
            throw new ResourceNotFoundException("No se puede eliminar. Material no encontrado: " + codigo);
        }
        materialRepository.deleteById(codigo);
    }

    private MaterialResponseDTO mapToResponseDTO(Material entity) {
        return MaterialResponseDTO.builder()
                .codigo(entity.getCodigo())
                .nombre(entity.getNombre())
                .categoria(entity.getCategoria())
                .ingresos(entity.getIngresos())
                .salidas(entity.getSalidas())
                .stock(entity.getStock())
                .estadoStock(entity.getStock() <= 5 ? "CRÍTICO" : "DISPONIBLE")
                .build();
    }
}

package com.comision.CFE.Controllers;

import com.comision.CFE.DTO.DashboardResponseDTO;
import com.comision.CFE.Services.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/resumen")
    public ResponseEntity<DashboardResponseDTO> obtenerResumen() {
        // Simplemente llamamos al service y devolvemos el DTO con un 200 OK
        return ResponseEntity.ok(dashboardService.obtenerResumen());
    }
}

package com.comision.CFE.Services;

import com.comision.CFE.Entity.Movimiento;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

public interface ReporteService {

    void exportarMovimientosPDF(HttpServletResponse response, List<Movimiento> movimientos) throws IOException;
}

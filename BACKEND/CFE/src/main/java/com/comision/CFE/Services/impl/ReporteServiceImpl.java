package com.comision.CFE.Services.impl;

import com.comision.CFE.Entity.Movimiento;
import com.comision.CFE.Services.ReporteService;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Stream;

@Service
public class ReporteServiceImpl implements ReporteService {

    @Override
    public void exportarMovimientosPDF(HttpServletResponse response, List<Movimiento> movimientos) throws IOException {

        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, response.getOutputStream());

        document.open();

        // 1. Encabezado (Título y logo simualdo)
        Font fontTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
        Paragraph title = new Paragraph("HISTORIAL DE MOVIMIENTOS DE INVENTARIO", fontTitulo);
        title.setAlignment(Paragraph.ALIGN_CENTER);
        document.add(title);

        Paragraph info = new Paragraph("Fecha de reporte: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        info.setAlignment(Paragraph.ALIGN_RIGHT);
        document.add(info);

        document.add(new Paragraph("\n")); // Espacio

        // 2. Tabla de Datos
        PdfPTable table = new PdfPTable(5); // 5 columnas
        table.setWidthPercentage(100);

        // Cabeceras de la tabla
        Stream.of("Fecha", "Material", "Tipo", "Cant.", "Responsable").forEach(columnTitle -> {
            PdfPCell header = new PdfPCell();
            header.setBackgroundColor(Color.LIGHT_GRAY);
            header.setPhrase(new Phrase(columnTitle));
            header.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(header);
        });

        // Llenar datos
        for (Movimiento mov : movimientos) {
            table.addCell(mov.getFechaRegistro().format(DateTimeFormatter.ofPattern("dd/MM/yy")));
            table.addCell(mov.getMaterial().getNombre());
            table.addCell(mov.getTipo().toString());
            table.addCell(String.valueOf(mov.getCantidad()));
            table.addCell(mov.getTrabajador().getNombreCompleto());
        }

        document.add(table);

        // 3. Sección de Firmas
        document.add(new Paragraph("\n\n\n"));
        PdfPTable firmaTable = new PdfPTable(2);
        firmaTable.setWidthPercentage(100);
        firmaTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        PdfPCell cellFirma = new PdfPCell(new Phrase("__________________________\nFirma Responsable Almacén"));
        cellFirma.setBorder(Rectangle.NO_BORDER);
        cellFirma.setHorizontalAlignment(Element.ALIGN_CENTER);

        firmaTable.addCell(cellFirma);
        firmaTable.addCell(cellFirma); // Firma duplicada o para el trabajador

        document.add(firmaTable);

        document.close();
    }

}

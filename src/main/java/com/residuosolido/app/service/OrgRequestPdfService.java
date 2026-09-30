package com.residuosolido.app.service;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.residuosolido.app.model.Request;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class OrgRequestPdfService {

    public byte[] generateRequestsPdf(List<Request> requests, String organizationName) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc);

        PdfFont bold = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
        PdfFont regular = PdfFontFactory.createFont(StandardFonts.HELVETICA);

        document.add(new Paragraph("Informe de Solicitudes de Acopio")
                .setFont(bold).setFontSize(18));
        document.add(new Paragraph("Organización: " + organizationName)
                .setFont(regular).setFontSize(11));
        document.add(new Paragraph("\n"));

        float[] columnWidths = {2, 3, 2, 2, 3, 2};
        Table table = new Table(columnWidths);

        addHeaderCell(table, "ID", bold);
        addHeaderCell(table, "Contacto", bold);
        addHeaderCell(table, "Ciudad", bold);
        addHeaderCell(table, "Estado", bold);
        addHeaderCell(table, "Dirección", bold);
        addHeaderCell(table, "Franja", bold);

        for (Request req : requests) {
            table.addCell(new Cell().add(new Paragraph(req.getId()).setFont(regular).setFontSize(10)));
            table.addCell(new Cell().add(new Paragraph(req.getContactName()).setFont(regular).setFontSize(10)));
            table.addCell(new Cell().add(new Paragraph(req.getCity().toString()).setFont(regular).setFontSize(10)));
            table.addCell(new Cell().add(new Paragraph(req.getStatus().name()).setFont(regular).setFontSize(10)));
            table.addCell(new Cell().add(new Paragraph(req.getAddress()).setFont(regular).setFontSize(10)));
            String slot = req.getConfirmedSlot() != null ? req.getConfirmedSlot().name() : "—";
            table.addCell(new Cell().add(new Paragraph(slot).setFont(regular).setFontSize(10)));
        }

        document.add(table);
        document.close();

        return baos.toByteArray();
    }

    private void addHeaderCell(Table table, String text, PdfFont font) {
        Cell cell = new Cell();
        cell.add(new Paragraph(text).setFont(font).setFontSize(11));
        cell.setBackgroundColor(com.itextpdf.kernel.colors.ColorConstants.LIGHT_GRAY);
        table.addCell(cell);
    }
}

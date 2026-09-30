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
import com.residuosolido.app.enums.RequestStatus;
import com.residuosolido.app.model.MonthlyReport;
import com.residuosolido.app.model.Request;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class OrgRequestPdfService {

    public byte[] generateMonthlyReportPdf(MonthlyReport report) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc);

        PdfFont bold = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
        PdfFont regular = PdfFontFactory.createFont(StandardFonts.HELVETICA);

        // Título
        document.add(new Paragraph("INFORME MENSUAL DE ACOPIO")
                .setFont(bold).setFontSize(18));
        document.add(new Paragraph(report.getPeriod().toString())
                .setFont(regular).setFontSize(12));
        document.add(new Paragraph("Organización: " + report.getOrganization().getName())
                .setFont(regular).setFontSize(11));
        document.add(new Paragraph("\n"));

        // 1. Total solicitudes
        document.add(new Paragraph("1. TOTAL SOLICITUDES")
                .setFont(bold).setFontSize(12));
        document.add(new Paragraph("Este mes: " + report.getTotalRequests())
                .setFont(regular));
        if (report.getPreviousMonth() != null) {
            double growth = report.getGrowthPercentage();
            String growthText = growth >= 0 ? "+" : "";
            document.add(new Paragraph(String.format("vs Mes anterior: %s%.1f%% (%d solicitudes)",
                    growthText, growth, report.getPreviousMonth().getTotal()))
                    .setFont(regular));
        }
        document.add(new Paragraph("\n"));

        // 2. Estado de solicitudes
        document.add(new Paragraph("2. ESTADO DE SOLICITUDES")
                .setFont(bold).setFontSize(12));
        addStatusTable(document, report, regular);
        document.add(new Paragraph("\n"));

        // 3. Solicitudes por ciudad
        document.add(new Paragraph("3. SOLICITUDES POR CIUDAD")
                .setFont(bold).setFontSize(12));
        addCityTable(document, report, regular);
        document.add(new Paragraph("\n"));

        // 4. Materiales más reciclados
        document.add(new Paragraph("4. MATERIALES MÁS RECICLADOS")
                .setFont(bold).setFontSize(12));
        addMaterialsTable(document, report, regular);
        document.add(new Paragraph("\n"));

        // 5. Comparativa últimos 3 meses (si aplica)
        if (report.getPreviousMonth() != null) {
            document.add(new Paragraph("5. TENDENCIA DE ÚLTIMOS MESES")
                    .setFont(bold).setFontSize(12));
            document.add(new Paragraph("Crecimiento sostenido: " +
                    String.format("%.1f%%", report.getGrowthPercentage()))
                    .setFont(regular));
            document.add(new Paragraph("Tasa de completación: " +
                    String.format("%.1f%%", report.getCompletionPercentage()))
                    .setFont(regular));
        }

        document.close();
        return baos.toByteArray();
    }

    private void addStatusTable(Document document, MonthlyReport report, PdfFont regular) throws IOException {
        float[] columnWidths = {2, 2, 1};
        Table table = new Table(columnWidths);

        addHeaderCell(table, "Estado", PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD));
        addHeaderCell(table, "Cantidad", PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD));
        addHeaderCell(table, "%", PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD));

        for (RequestStatus status : RequestStatus.values()) {
            int count = report.getCountByStatus().getOrDefault(status, 0);
            double pct = report.getTotalRequests() > 0 ? (count * 100.0) / report.getTotalRequests() : 0;
            table.addCell(new Cell().add(new Paragraph(status.name()).setFont(regular).setFontSize(10)));
            table.addCell(new Cell().add(new Paragraph(String.valueOf(count)).setFont(regular).setFontSize(10)));
            table.addCell(new Cell().add(new Paragraph(String.format("%.0f%%", pct)).setFont(regular).setFontSize(10)));
        }

        document.add(table);
    }

    private void addCityTable(Document document, MonthlyReport report, PdfFont regular) throws IOException {
        float[] columnWidths = {2, 2};
        Table table = new Table(columnWidths);

        addHeaderCell(table, "Ciudad", PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD));
        addHeaderCell(table, "Cantidad", PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD));

        report.getCountByCity().forEach((city, count) -> {
            table.addCell(new Cell().add(new Paragraph(city.name()).setFont(regular).setFontSize(10)));
            table.addCell(new Cell().add(new Paragraph(String.valueOf(count)).setFont(regular).setFontSize(10)));
        });

        document.add(table);
    }

    private void addMaterialsTable(Document document, MonthlyReport report, PdfFont regular) throws IOException {
        float[] columnWidths = {2, 2};
        Table table = new Table(columnWidths);

        addHeaderCell(table, "Material", PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD));
        addHeaderCell(table, "Cantidad", PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD));

        report.getCountByMaterial().entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .forEach(e -> {
                    table.addCell(new Cell().add(new Paragraph(e.getKey().name()).setFont(regular).setFontSize(10)));
                    table.addCell(new Cell().add(new Paragraph(String.valueOf(e.getValue())).setFont(regular).setFontSize(10)));
                });

        document.add(table);
    }

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

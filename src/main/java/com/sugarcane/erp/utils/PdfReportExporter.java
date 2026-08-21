package com.sugarcane.erp.utils;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.sugarcane.erp.model.DailyReportItem;

import java.io.File;
import java.io.FileOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PdfReportExporter {

    private static final String FONT_PATH = "C:/Windows/Fonts/mangal.ttf";

    public static void generateReportPdf(File file, List<DailyReportItem> items, String titleStr) throws Exception {
        Document document = new Document(PageSize.A4.rotate());
        PdfWriter.getInstance(document, new FileOutputStream(file));
        document.open();

        BaseFont bf = BaseFont.createFont(FONT_PATH, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        Font titleFont = new Font(bf, 20, Font.BOLD);
        Font headFont = new Font(bf, 12, Font.BOLD);
        Font cellFont = new Font(bf, 11, Font.NORMAL);

        Paragraph title = new Paragraph(titleStr, titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        
        document.add(new Paragraph(" ")); // blank line

        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10f);
        table.setSpacingAfter(10f);
        
        float[] columnWidths = {2f, 2f, 2f, 2f, 2f};
        table.setWidths(columnWidths);

        String[] headers = {"तारीख (Date)", "खरेदी वजन (टन)", "खरेदी रक्कम (₹)", "विक्री वजन (टन)", "विक्री रक्कम (₹)"};
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, headFont));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
            cell.setPadding(8f);
            table.addCell(cell);
        }

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        double totalPurWt = 0, totalPurAmt = 0, totalSaleWt = 0, totalSaleAmt = 0;

        for (DailyReportItem item : items) {
            addCell(table, item.getReportDate().format(dateFormatter), cellFont);
            addCell(table, String.format("%.3f", item.getTotalPurchaseWeight()), cellFont);
            addCell(table, String.format("%.2f", item.getTotalPurchaseAmount()), cellFont);
            addCell(table, String.format("%.3f", item.getTotalSaleWeight()), cellFont);
            addCell(table, String.format("%.2f", item.getTotalSaleAmount()), cellFont);
            
            totalPurWt += item.getTotalPurchaseWeight();
            totalPurAmt += item.getTotalPurchaseAmount();
            totalSaleWt += item.getTotalSaleWeight();
            totalSaleAmt += item.getTotalSaleAmount();
        }
        
        // Totals Row
        PdfPCell totalCell = new PdfPCell(new Phrase("एकूण (Total)", headFont));
        totalCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totalCell.setPadding(8f);
        table.addCell(totalCell);
        
        addCell(table, String.format("%.3f", totalPurWt), headFont);
        addCell(table, String.format("%.2f", totalPurAmt), headFont);
        addCell(table, String.format("%.3f", totalSaleWt), headFont);
        addCell(table, String.format("%.2f", totalSaleAmt), headFont);

        document.add(table);
        document.close();
    }

    private static void addCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(5f);
        table.addCell(cell);
    }
}

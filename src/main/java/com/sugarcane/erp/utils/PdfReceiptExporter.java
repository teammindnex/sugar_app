package com.sugarcane.erp.utils;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.sugarcane.erp.controller.FarmerController;
import javafx.stage.Window;
import javafx.scene.control.Alert;

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PdfReceiptExporter {

    private static final String FONT_PATH = "C:/Windows/Fonts/mangal.ttf";

    public static String generateReceiptPdf(String farmerName, String billNo, String transport, String vehicleNo,
                                          LocalDate date, List<FarmerController.BillItem> items,
                                          String emptyWeightStr, String loadedWeightStr,
                                          double advance, double previousBalance, double totalAmount, double finalBalance,
                                          Window ownerWindow) {

        String userHome = System.getProperty("user.home");
        File dir = new File(userHome, "SugarCaneBills");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File file = new File(dir, "Receipt_" + billNo + "_" + System.currentTimeMillis() + ".pdf");

        try {
            Document document = new Document(PageSize.A5, 20, 20, 20, 20); // A5 size is typical for such receipts
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(file));
            writer.setPageEvent(new WatermarkEvent());
            
            document.open();

            // Modern Colors
            BaseColor ACCENT_COLOR = new BaseColor(46, 125, 50); // Dark Green (#2E7D32)
            BaseColor TEXT_COLOR = new BaseColor(66, 66, 66); // Dark Gray (#424242)
            BaseColor BORDER_COLOR = new BaseColor(224, 224, 224); // Light Gray (#E0E0E0)
            BaseColor HEADER_BG_COLOR = new BaseColor(232, 245, 233); // Light Green (#E8F5E9)
            BaseColor VALUE_COLOR = new BaseColor(33, 33, 33); // Almost Black (#212121)

            // Setup Fonts
            BaseFont marathiBaseFont = BaseFont.createFont(FONT_PATH, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            Font titleFont = new Font(marathiBaseFont, 22, Font.BOLD, ACCENT_COLOR);
            Font subTitleFont = new Font(marathiBaseFont, 10, Font.NORMAL, TEXT_COLOR);
            Font propFont = new Font(marathiBaseFont, 10, Font.BOLD, TEXT_COLOR);
            Font labelFont = new Font(marathiBaseFont, 10, Font.NORMAL, TEXT_COLOR);
            Font valueFont = new Font(marathiBaseFont, 12, Font.BOLD, VALUE_COLOR);
            Font tableHeaderFont = new Font(marathiBaseFont, 11, Font.BOLD, ACCENT_COLOR);
            Font amountFont = new Font(marathiBaseFont, 12, Font.BOLD, ACCENT_COLOR);

            // Create Main Outer Table to act as border
            PdfPTable outerTable = new PdfPTable(1);
            outerTable.setWidthPercentage(100);
            PdfPCell outerCell = new PdfPCell();
            outerCell.setBorderColor(BORDER_COLOR);
            outerCell.setBorderWidth(1.5f);
            outerCell.setPadding(10);
            outerCell.setPaddingBottom(15);

            // ================= HEADER SECTION =================
            PdfPTable headerTable = new PdfPTable(1);
            headerTable.setWidthPercentage(100);
            
            try {
                PdfPTable godsTable = new PdfPTable(1);
                godsTable.setWidthPercentage(100);
                
                // Ganesh
                PdfPCell ganeshCell = new PdfPCell();
                ganeshCell.setBorder(Rectangle.NO_BORDER);
                Image ganeshImg = Image.getInstance(PdfReceiptExporter.class.getResource("/images/ganesh.png"));
                ganeshImg.scaleToFit(40, 40);
                ganeshImg.setAlignment(Element.ALIGN_CENTER);
                ganeshCell.addElement(ganeshImg);
                Paragraph ganeshText = new Paragraph("|| श्री गणेश प्रसन्न ||", propFont);
                ganeshText.setAlignment(Element.ALIGN_CENTER);
                ganeshCell.addElement(ganeshText);
                
                godsTable.addCell(ganeshCell);
                headerTable.addCell(godsTable);
            } catch (Exception e) {
                // Ignore missing images
            }
            
            PdfPCell titleCell = new PdfPCell(new Phrase("श्री गणेश कृपा ऊस सप्लायर्स", titleFont));
            titleCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            titleCell.setBorder(Rectangle.NO_BORDER);
            titleCell.setPaddingTop(5);
            headerTable.addCell(titleCell);

            PdfPCell subTitleCell = new PdfPCell(new Phrase("खास रसवंतीसाठी वापरल्या जाणाऱ्या खेडशिवापूर च्या ऊसाचे होलसेल विक्रेते", subTitleFont));
            subTitleCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            subTitleCell.setBorder(Rectangle.NO_BORDER);
            subTitleCell.setPaddingBottom(5);
            headerTable.addCell(subTitleCell);

            // Proprietors Info
            PdfPTable propTable = new PdfPTable(2);
            propTable.setWidthPercentage(100);
            
            PdfPCell prop1 = new PdfPCell(new Phrase("प्रोप्रा. दादासो कोंडे\n9763948154 / 7588237123", propFont));
            prop1.setHorizontalAlignment(Element.ALIGN_CENTER);
            prop1.setBorder(Rectangle.NO_BORDER);
            propTable.addCell(prop1);
            
            PdfPCell prop2 = new PdfPCell(new Phrase("प्रोप्रा. विराज कोंडे\n8999875886 / 7588237122", propFont));
            prop2.setHorizontalAlignment(Element.ALIGN_CENTER);
            prop2.setBorder(Rectangle.NO_BORDER);
            propTable.addCell(prop2);
            
            PdfPCell propContainer = new PdfPCell(propTable);
            propContainer.setBorder(Rectangle.BOTTOM);
            propContainer.setBorderColor(BORDER_COLOR);
            propContainer.setBorderWidthBottom(1f);
            propContainer.setPaddingBottom(10);
            headerTable.addCell(propContainer);
            
            outerCell.addElement(headerTable);

            // ================= INFO SECTION =================
            PdfPTable infoTable = new PdfPTable(4);
            infoTable.setWidthPercentage(100);
            infoTable.setWidths(new float[]{1.2f, 3.8f, 1f, 3f});
            infoTable.setSpacingBefore(10);
            infoTable.setSpacingAfter(10);
            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            String dateStr = date != null ? date.format(formatter) : "";

            // Row 1: Name and Bill No
            PdfPCell nameLabel = new PdfPCell(new Phrase("नाव ", labelFont));
            nameLabel.setBorder(Rectangle.NO_BORDER);
            nameLabel.setPaddingTop(5);
            infoTable.addCell(nameLabel);
            
            PdfPCell nameVal = new PdfPCell(new Phrase(farmerName != null ? farmerName : "", valueFont));
            nameVal.setBorder(Rectangle.BOTTOM);
            nameVal.setBorderColor(BORDER_COLOR);
            nameVal.setPaddingTop(5);
            infoTable.addCell(nameVal);
            
            PdfPCell noLabel = new PdfPCell(new Phrase("नं. ", labelFont));
            noLabel.setBorder(Rectangle.NO_BORDER);
            noLabel.setHorizontalAlignment(Element.ALIGN_RIGHT);
            noLabel.setPaddingTop(5);
            noLabel.setPaddingRight(5);
            infoTable.addCell(noLabel);
            
            PdfPCell noVal = new PdfPCell(new Phrase(billNo != null ? billNo : "", valueFont));
            noVal.setBorder(Rectangle.BOTTOM);
            noVal.setBorderColor(BORDER_COLOR);
            noVal.setPaddingTop(5);
            infoTable.addCell(noVal);

            // Row 2: Vehicle No and Date
            PdfPCell vhLabel = new PdfPCell(new Phrase("गाडी नं ", labelFont));
            vhLabel.setBorder(Rectangle.NO_BORDER);
            vhLabel.setPaddingTop(8);
            infoTable.addCell(vhLabel);
            
            PdfPCell vhVal = new PdfPCell(new Phrase(vehicleNo != null ? vehicleNo : "", valueFont));
            vhVal.setBorder(Rectangle.BOTTOM);
            vhVal.setBorderColor(BORDER_COLOR);
            vhVal.setPaddingTop(8);
            infoTable.addCell(vhVal);
            
            PdfPCell dtLabel = new PdfPCell(new Phrase("दिनांक ", labelFont));
            dtLabel.setBorder(Rectangle.NO_BORDER);
            dtLabel.setHorizontalAlignment(Element.ALIGN_RIGHT);
            dtLabel.setPaddingTop(8);
            dtLabel.setPaddingRight(5);
            infoTable.addCell(dtLabel);
            
            PdfPCell dtVal = new PdfPCell(new Phrase(dateStr, valueFont));
            dtVal.setBorder(Rectangle.BOTTOM);
            dtVal.setBorderColor(BORDER_COLOR);
            dtVal.setPaddingTop(8);
            infoTable.addCell(dtVal);

            // Row 3: Weight Info
            double nw = 0;
            if(loadedWeightStr != null && !loadedWeightStr.isEmpty() && emptyWeightStr != null && !emptyWeightStr.isEmpty()) {
                try {
                    nw = Double.parseDouble(loadedWeightStr) - Double.parseDouble(emptyWeightStr);
                } catch(Exception ignored) {}
            }
            String netWeightStr = nw > 0 ? String.format("%.2f", nw) : "";
            
            PdfPTable wtTable = new PdfPTable(6);
            wtTable.setWidthPercentage(100);
            wtTable.setWidths(new float[]{1.5f, 1.5f, 1.5f, 1.5f, 1.5f, 1.5f});
            
            PdfPCell w1 = new PdfPCell(new Phrase("भरलेले: ", labelFont));
            w1.setBorder(Rectangle.NO_BORDER);
            w1.setPaddingTop(8);
            wtTable.addCell(w1);
            
            PdfPCell w2 = new PdfPCell(new Phrase(loadedWeightStr != null ? loadedWeightStr : "", valueFont));
            w2.setBorder(Rectangle.BOTTOM);
            w2.setBorderColor(BORDER_COLOR);
            w2.setPaddingTop(8);
            wtTable.addCell(w2);
            
            PdfPCell w3 = new PdfPCell(new Phrase("रिकामे: ", labelFont));
            w3.setBorder(Rectangle.NO_BORDER);
            w3.setHorizontalAlignment(Element.ALIGN_RIGHT);
            w3.setPaddingRight(5);
            w3.setPaddingTop(8);
            wtTable.addCell(w3);
            
            PdfPCell w4 = new PdfPCell(new Phrase(emptyWeightStr != null ? emptyWeightStr : "", valueFont));
            w4.setBorder(Rectangle.BOTTOM);
            w4.setBorderColor(BORDER_COLOR);
            w4.setPaddingTop(8);
            wtTable.addCell(w4);
            
            PdfPCell w5 = new PdfPCell(new Phrase("निव्वळ: ", labelFont));
            w5.setBorder(Rectangle.NO_BORDER);
            w5.setHorizontalAlignment(Element.ALIGN_RIGHT);
            w5.setPaddingRight(5);
            w5.setPaddingTop(8);
            wtTable.addCell(w5);
            
            PdfPCell w6 = new PdfPCell(new Phrase(netWeightStr, valueFont));
            w6.setBorder(Rectangle.BOTTOM);
            w6.setBorderColor(BORDER_COLOR);
            w6.setPaddingTop(8);
            wtTable.addCell(w6);
            
            PdfPCell wtCell = new PdfPCell(wtTable);
            wtCell.setColspan(4);
            wtCell.setBorder(Rectangle.NO_BORDER);
            wtCell.setPaddingTop(5);
            wtCell.setPaddingBottom(5);
            infoTable.addCell(wtCell);

            outerCell.addElement(infoTable);

            // ================= ITEMS TABLE =================
            PdfPTable itemsTable = new PdfPTable(5);
            itemsTable.setWidthPercentage(100);
            itemsTable.setWidths(new float[]{1f, 4f, 1.5f, 1.5f, 2f});
            itemsTable.setSpacingAfter(10);
            
            String[] headers = {"अ.क्र.", "मालाचा प्रकार", "भारा", "दर", "रक्कम"};
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, tableHeaderFont));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setBackgroundColor(HEADER_BG_COLOR);
                cell.setBorderColor(BORDER_COLOR);
                cell.setPadding(6);
                itemsTable.addCell(cell);
            }

            int minRows = 8;
            for (int i = 0; i < Math.max(minRows, items.size()); i++) {
                if (i < items.size()) {
                    FarmerController.BillItem item = items.get(i);
                    itemsTable.addCell(createItemCell(String.valueOf(i + 1), valueFont, BORDER_COLOR));
                    itemsTable.addCell(createItemCell(item.getCaneType() != null ? item.getCaneType() : "", valueFont, BORDER_COLOR));
                    itemsTable.addCell(createItemCell(String.format("%.2f", item.getWeight()), valueFont, BORDER_COLOR));
                    itemsTable.addCell(createItemCell(String.format("%.2f", item.getRate()), valueFont, BORDER_COLOR));
                    itemsTable.addCell(createItemCell(String.format("%.2f", item.getAmount()), valueFont, BORDER_COLOR));
                } else {
                    itemsTable.addCell(createItemCell(" ", valueFont, BORDER_COLOR));
                    itemsTable.addCell(createItemCell(" ", valueFont, BORDER_COLOR));
                    itemsTable.addCell(createItemCell(" ", valueFont, BORDER_COLOR));
                    itemsTable.addCell(createItemCell(" ", valueFont, BORDER_COLOR));
                    itemsTable.addCell(createItemCell(" ", valueFont, BORDER_COLOR));
                }
            }
            
            outerCell.addElement(itemsTable);

            // ================= TOTALS SECTION =================
            PdfPTable totalsTable = new PdfPTable(3);
            totalsTable.setWidthPercentage(100);
            totalsTable.setWidths(new float[]{5.5f, 2.5f, 2f});

            // Ekun Rakkam
            PdfPCell tLabelEkun = new PdfPCell(new Phrase(" ", labelFont));
            tLabelEkun.setBorder(Rectangle.NO_BORDER);
            totalsTable.addCell(tLabelEkun);
            
            PdfPCell tLabel1 = new PdfPCell(new Phrase("एकुण रक्कम", labelFont));
            tLabel1.setBorder(Rectangle.BOTTOM | Rectangle.LEFT | Rectangle.TOP);
            tLabel1.setBorderColor(BORDER_COLOR);
            tLabel1.setPadding(5);
            totalsTable.addCell(tLabel1);
            
            PdfPCell tVal1 = new PdfPCell(new Phrase(String.format("%.0f", totalAmount), valueFont));
            tVal1.setBorder(Rectangle.BOTTOM | Rectangle.RIGHT | Rectangle.TOP);
            tVal1.setBorderColor(BORDER_COLOR);
            tVal1.setHorizontalAlignment(Element.ALIGN_RIGHT);
            tVal1.setPadding(5);
            totalsTable.addCell(tVal1);
            
            // Rokh Jama
            double totalExp = advance + previousBalance;
            PdfPCell emptyRow2 = new PdfPCell(new Phrase(" "));
            emptyRow2.setBorder(Rectangle.NO_BORDER);
            totalsTable.addCell(emptyRow2);
            
            PdfPCell tLabel2 = new PdfPCell(new Phrase("रोख जमा", labelFont));
            tLabel2.setBorder(Rectangle.BOTTOM | Rectangle.LEFT);
            tLabel2.setBorderColor(BORDER_COLOR);
            tLabel2.setPadding(5);
            totalsTable.addCell(tLabel2);
            
            PdfPCell tVal2 = new PdfPCell(new Phrase(totalExp > 0 ? String.format("%.0f", totalExp) : "0", valueFont));
            tVal2.setBorder(Rectangle.BOTTOM | Rectangle.RIGHT);
            tVal2.setBorderColor(BORDER_COLOR);
            tVal2.setHorizontalAlignment(Element.ALIGN_RIGHT);
            tVal2.setPadding(5);
            totalsTable.addCell(tVal2);
            
            // Baki
            PdfPCell emptyRow3 = new PdfPCell(new Phrase(" "));
            emptyRow3.setBorder(Rectangle.NO_BORDER);
            totalsTable.addCell(emptyRow3);
            
            PdfPCell tLabel3 = new PdfPCell(new Phrase("बाकी", tableHeaderFont)); // Make label stand out
            tLabel3.setBorder(Rectangle.BOTTOM | Rectangle.LEFT);
            tLabel3.setBorderColor(BORDER_COLOR);
            tLabel3.setBackgroundColor(HEADER_BG_COLOR);
            tLabel3.setPadding(5);
            totalsTable.addCell(tLabel3);
            
            PdfPCell tVal3 = new PdfPCell(new Phrase(String.format("%.0f", finalBalance), amountFont));
            tVal3.setBorder(Rectangle.BOTTOM | Rectangle.RIGHT);
            tVal3.setBorderColor(BORDER_COLOR);
            tVal3.setBackgroundColor(HEADER_BG_COLOR);
            tVal3.setHorizontalAlignment(Element.ALIGN_RIGHT);
            tVal3.setPadding(5);
            totalsTable.addCell(tVal3);

            outerCell.addElement(totalsTable);

            // ================= FOOTER SECTION =================
            PdfPTable footerTable = new PdfPTable(2);
            footerTable.setWidthPercentage(100);
            footerTable.setWidths(new float[]{6f, 4f});
            footerTable.setSpacingBefore(15);
            
            String words = NumberToMarathiWordsConverter.convert((long) finalBalance) + " रुपये फक्त";
            
            Phrase p1 = new Phrase("अक्षरी रुपये: ", labelFont);
            Phrase p2 = new Phrase(words, amountFont); // Highlight the words
            Paragraph wp = new Paragraph();
            wp.add(p1);
            wp.add(p2);
            
            PdfPCell wordsCell = new PdfPCell(wp);
            wordsCell.setBorder(Rectangle.NO_BORDER);
            wordsCell.setPadding(5);
            footerTable.addCell(wordsCell);
            
            PdfPCell sigCell = new PdfPCell(new Phrase("सही\n\n\n", propFont));
            sigCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            sigCell.setBorder(Rectangle.NO_BORDER);
            sigCell.setPaddingTop(10);
            sigCell.setPaddingRight(20);
            footerTable.addCell(sigCell);
            
            outerCell.addElement(footerTable);

            outerTable.addCell(outerCell);
            document.add(outerTable);
            
            document.close();

            // showAlert(Alert.AlertType.INFORMATION, "यशस्वी", "शेतकरी पावती PDF यशस्वीरित्या सेव्ह झाली आहे.");
            return file.getAbsolutePath();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Error generating Receipt PDF: " + e.getMessage());
            return null;
        }
    }
    
    private static PdfPCell createItemCell(String text, Font font, BaseColor borderColor) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setBorderColor(borderColor);
        cell.setPadding(5);
        return cell;
    }

    private static void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    static class WatermarkEvent extends PdfPageEventHelper {
        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            try {
                PdfContentByte canvas = writer.getDirectContentUnder();
                Image img = Image.getInstance(PdfReceiptExporter.class.getResource("/images/sugarcanephotoforreceipt.png"));
                
                float docW = document.getPageSize().getWidth();
                float docH = document.getPageSize().getHeight();
                
                img.scaleAbsolute(docW, docH); 
                img.setAbsolutePosition(0, 0);
                
                PdfGState state = new PdfGState();
                state.setFillOpacity(0.15f); 
                canvas.setGState(state);
                canvas.addImage(img);
                
                // Text Watermark
                canvas.beginText();
                BaseFont bf = BaseFont.createFont(FONT_PATH, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
                Font watermarkFont = new Font(bf, 35, Font.BOLD, new BaseColor(27, 94, 32, 20));
                ColumnText.showTextAligned(canvas, Element.ALIGN_CENTER, new Phrase("श्री गणेश ऊस सप्लायर", watermarkFont), docW / 2, docH / 2, 45);
                canvas.endText();
            } catch (Exception e) {
                // Ignore missing image
            }
        }
    }
}

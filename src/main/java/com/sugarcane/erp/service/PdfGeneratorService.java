package com.sugarcane.erp.service;

import com.itextpdf.text.Document;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfWriter;
import com.sugarcane.erp.controller.CustomerController.CustomerBillItem;
import com.sugarcane.erp.utils.NumberToMarathiWordsConverter;

import java.awt.*;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.AttributedString;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PdfGeneratorService {

    private static final Color MAROON = new Color(180, 0, 0);

    public static void generateCustomerBillPdf(
            String customerName, String mobile, String address, String month, String date, String billNo,
            List<CustomerBillItem> items, double totalAmt, double prevBal, double cashReceived, double finalBal,
            String outputPath) throws Exception {

        int width = 2480; // A4 at 300 DPI
        int height = 3508;
        
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = img.createGraphics();

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, width, height);

        // Load fonts
        Font boldFont = loadFont("NotoSansDevanagari-Bold.ttf", 45f);
        Font companyFont = loadFont("NotoSansDevanagari-Bold.ttf", 110f);
        Font smallFont = loadFont("NotoSansDevanagari-Regular.ttf", 35f);

        // Border
        g2d.setColor(MAROON);
        g2d.setStroke(new BasicStroke(8));
        g2d.drawRect(50, 50, width - 100, height - 100);
        g2d.drawRect(65, 65, width - 130, height - 130); // double border

        int margin = 150;
        int y = 150;

        // Header
        g2d.setFont(boldFont.deriveFont(40f));
        g2d.setColor(MAROON);
        drawCenteredString(g2d, "!! जय शारदा गजानन प्रसन्न !!", width, y);
        
        y += 70;
        g2d.setColor(MAROON);
        g2d.setFont(smallFont.deriveFont(Font.BOLD, 38f));
        drawMixedText(g2d, "मो.: ९४४४३९४८५१ / ९५५२२३७१२३", margin, y);
        drawRightAlignedText(g2d, "मो.: ९५५२२३७१२२ / ८९९९८७४८८६", width - margin, y);

        y += 40;
        drawMixedText(g2d, "प्रोप्रा. बाबासो कोंडे", margin, y);
        drawRightAlignedText(g2d, "प्रोप्रा. विराज कोंडे", width - margin, y);

        y += 120;
        g2d.setFont(companyFont);
        g2d.setColor(MAROON);
        drawCenteredString(g2d, "श्री गणेश कृपा ऊस सप्लायर्स", width, y);

        y += 80;
        g2d.setFont(boldFont.deriveFont(45f));
        drawCenteredString(g2d, "खास रसवंतीसाठी वापरल्या जाणाऱ्या खेडशिवापूर च्या ऊसाचे होलसेल विक्रेते", width, y);

        y += 60;
        g2d.setStroke(new BasicStroke(3));
        g2d.drawLine(margin, y, width - margin, y);

        // Customer Info
        y += 60;
        g2d.setColor(MAROON);
        g2d.setFont(boldFont.deriveFont(45f));
        drawMixedText(g2d, "नाव : " + (customerName.isEmpty() ? "_________________________" : customerName), margin, y);
        drawRightAlignedText(g2d, "नं. : " + billNo, width - margin, y);

        y += 70;
        drawMixedText(g2d, "पत्ता : " + (address.isEmpty() ? "_________________________" : address), margin, y);
        drawMixedText(g2d, "महिना : " + (month.isEmpty() ? "___________" : month), width / 2 + 100, y);
        drawRightAlignedText(g2d, "दिनांक : " + date, width - margin, y);

        y += 50;
        g2d.drawLine(margin, y, width - margin, y);

        // Table
        int tableTop = y + 20;
        y = tableTop;
        int tableWidth = width - 2 * margin;
        int[] colWidths = { 200, 700, 300, 400, 580 };
        int[] colEdges = new int[6];
        colEdges[0] = margin;
        for(int i=0; i<colWidths.length; i++) colEdges[i+1] = colEdges[i] + colWidths[i];

        g2d.setStroke(new BasicStroke(5));
        g2d.drawRect(margin, y, tableWidth, 1200);

        g2d.setFont(boldFont.deriveFont(40f));
        String[] headers = { "अ.क्र.", "मालाचा प्रकार", "भारा/वजन", "दर", "रक्कम" };
        for(int i=0; i<headers.length; i++) {
            drawCenteredStringInRect(g2d, headers[i], colEdges[i], y + 60, colWidths[i]);
        }
        
        y += 100;
        g2d.drawLine(margin, y, width - margin, y);

        // Vertical lines
        for(int i=1; i<colEdges.length; i++) {
            g2d.drawLine(colEdges[i], tableTop, colEdges[i], tableTop + 1200);
        }

        // Table Rows
        int rowY = y + 70;
        g2d.setColor(Color.BLACK); // Items in black
        for (CustomerBillItem item : items) {
            drawCenteredStringInRect(g2d, String.valueOf(item.getSrNo()), colEdges[0], rowY, colWidths[0]);
            
            // Append supply type to item type (e.g., ऊस (भारा))
            String typeText = item.getItemType() + " (" + item.getSupplyType() + ")";
            drawMixedText(g2d, typeText, colEdges[1] + 30, rowY);
            
            String qtyText = String.format("%.2f", item.getQuantity());
            drawCenteredStringInRect(g2d, qtyText, colEdges[2], rowY, colWidths[2]);
            drawCenteredStringInRect(g2d, String.format("%.2f", item.getRate()), colEdges[3], rowY, colWidths[3]);
            drawRightAlignedTextInRect(g2d, String.format("%.2f", item.getAmount()), colEdges[4], rowY, colWidths[4] - 30);
            rowY += 80;
        }

        // Totals Box
        y = tableTop + 1200;
        g2d.setColor(MAROON);
        g2d.drawRect(margin, y, tableWidth, 400);

        // Footer lines
        g2d.drawLine(colEdges[3], y, colEdges[3], y + 400); // Vertical line separating totals labels and values
        
        y += 80;
        drawRightAlignedTextInRect(g2d, "एकूण रक्कम : ", colEdges[2], y, colWidths[3] - 20);
        drawRightAlignedTextInRect(g2d, String.format("%.2f", totalAmt), colEdges[4], y, colWidths[4] - 30);
        
        y += 100;
        g2d.drawLine(colEdges[3], y - 60, width - margin, y - 60);
        drawRightAlignedTextInRect(g2d, "मागील बाकी : ", colEdges[2], y, colWidths[3] - 20);
        drawRightAlignedTextInRect(g2d, String.format("%.2f", prevBal), colEdges[4], y, colWidths[4] - 30);

        y += 100;
        g2d.drawLine(colEdges[3], y - 60, width - margin, y - 60);
        drawRightAlignedTextInRect(g2d, "रोख जमा : ", colEdges[2], y, colWidths[3] - 20);
        drawRightAlignedTextInRect(g2d, String.format("%.2f", cashReceived), colEdges[4], y, colWidths[4] - 30);

        y += 100;
        g2d.drawLine(colEdges[3], y - 60, width - margin, y - 60);
        drawRightAlignedTextInRect(g2d, "बाकी : ", colEdges[2], y, colWidths[3] - 20);
        drawRightAlignedTextInRect(g2d, String.format("%.2f", finalBal), colEdges[4], y, colWidths[4] - 30);

        // Amount in words
        y += 100;
        String amountInWords = NumberToMarathiWordsConverter.convert((long) finalBal);
        drawMixedText(g2d, "अक्षरी रुपये : " + amountInWords + " रुपये फक्त", margin + 20, y);

        // Signature
        y += 200;
        drawRightAlignedText(g2d, "श्री गणेश कृपा ऊस सप्लायर्स", width - margin - 50, y);
        y += 50;
        drawRightAlignedText(g2d, "प्रोप्रायटर", width - margin - 150, y);

        g2d.dispose();

        // Convert BufferedImage to PDF
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        javax.imageio.ImageIO.write(img, "jpg", baos);
        Image iTextImage = Image.getInstance(baos.toByteArray());
        
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, new FileOutputStream(new File(outputPath)));
        document.open();
        
        iTextImage.scaleToFit(PageSize.A4.getWidth(), PageSize.A4.getHeight());
        iTextImage.setAbsolutePosition(0, 0);
        document.add(iTextImage);
        document.close();
    }

    private static Font loadFont(String name, float size) {
        try {
            String path = name.startsWith("/") ? name : "/" + name;
            InputStream is = PdfGeneratorService.class.getResourceAsStream(path);
            
            if (is == null) {
                is = PdfGeneratorService.class.getClassLoader().getResourceAsStream(name);
            }
            if (is == null) {
                return new Font("Arial", Font.PLAIN, (int)size);
            }
            
            return Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(size);
        } catch (Exception e) {
            return new Font("Arial", Font.PLAIN, (int)size);
        }
    }

    private static void drawCenteredString(Graphics2D g, String text, int width, int y) {
        int w = getMixedTextWidth(g, text);
        drawMixedText(g, text, (width - w) / 2, y);
    }

    private static void drawCenteredStringInRect(Graphics2D g, String text, int x, int y, int width) {
        int w = getMixedTextWidth(g, text);
        drawMixedText(g, text, x + (width - w) / 2, y);
    }

    private static void drawRightAlignedText(Graphics2D g, String text, int rightX, int y) {
        int w = getMixedTextWidth(g, text);
        drawMixedText(g, text, rightX - w, y);
    }

    private static void drawRightAlignedTextInRect(Graphics2D g, String text, int x, int y, int width) {
        int w = getMixedTextWidth(g, text);
        drawMixedText(g, text, x + width - w, y);
    }

    private static void drawMixedText(Graphics2D g2d, String text, int x, int y) {
        if (text == null || text.trim().isEmpty()) return;
        Font curr = g2d.getFont();
        Font devFont = curr;
        Font engFont = new Font("Arial", curr.getStyle(), curr.getSize());
        
        AttributedString as = new AttributedString(text);
        as.addAttribute(TextAttribute.FONT, devFont);
        
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (Character.UnicodeBlock.of(c) != Character.UnicodeBlock.DEVANAGARI && c != '।') {
                as.addAttribute(TextAttribute.FONT, engFont, i, i + 1);
            }
        }
        g2d.drawString(as.getIterator(), x, y);
    }

    private static int getMixedTextWidth(Graphics2D g2d, String text) {
        if (text == null || text.trim().isEmpty()) return 0;
        Font curr = g2d.getFont();
        Font engFont = new Font("Arial", curr.getStyle(), curr.getSize());
        AttributedString as = new AttributedString(text);
        as.addAttribute(TextAttribute.FONT, curr);
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (Character.UnicodeBlock.of(c) != Character.UnicodeBlock.DEVANAGARI && c != '।') {
                as.addAttribute(TextAttribute.FONT, engFont, i, i + 1);
            }
        }
        try {
            return (int) new TextLayout(as.getIterator(), g2d.getFontRenderContext()).getAdvance();
        } catch (Exception e) {
            return text.length() * 20; // Fallback
        }
    }
}

package com.sugarcane.erp.utils;

import com.itextpdf.text.Document;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfWriter;
import com.sugarcane.erp.model.Farmer;
import com.sugarcane.erp.model.LedgerEntry;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Window;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class PdfLedgerExporter {

    public static String generateLedgerPdf(Farmer farmer, LocalDate startDate, LocalDate endDate, List<LedgerEntry> entries, Window ownerWindow) {
        String userHome = System.getProperty("user.home");
        File dir = new File(userHome, "SugarCaneBills");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File file = new File(dir, "FarmerLedger_" + farmer.getName() + "_" + System.currentTimeMillis() + ".pdf");
        
        final String[] resultPath = new String[1];
        final Exception[] error = new Exception[1];
        
        // We must run the JavaFX node creation and snapshot on the JavaFX Application Thread
        if (Platform.isFxApplicationThread()) {
            try {
                resultPath[0] = createPdfFromSnapshot(farmer, startDate, endDate, entries, file);
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Error", "Error generating Ledger PDF: " + e.getMessage());
            }
        } else {
            CountDownLatch latch = new CountDownLatch(1);
            Platform.runLater(() -> {
                try {
                    resultPath[0] = createPdfFromSnapshot(farmer, startDate, endDate, entries, file);
                } catch (Exception e) {
                    error[0] = e;
                } finally {
                    latch.countDown();
                }
            });
            try {
                latch.await();
                if (error[0] != null) {
                    error[0].printStackTrace();
                    Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Error", "Error generating Ledger PDF: " + error[0].getMessage()));
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        
        return resultPath[0];
    }
    
    private static String createPdfFromSnapshot(Farmer farmer, LocalDate startDate, LocalDate endDate, List<LedgerEntry> entries, File file) throws Exception {
        VBox mainLayout = new VBox(0);
        mainLayout.setStyle("-fx-background-color: white;");
        // Landscape A4 width is ~1123, we use a wide layout
        mainLayout.setPrefWidth(1150);
        mainLayout.setPadding(new Insets(30));

        // --- Header ---
        Label titleLabel = new Label("Sugarcane Supplier");
        titleLabel.setStyle("-fx-font-family: 'Arial'; -fx-font-weight: bold; -fx-font-size: 28px; -fx-text-fill: #6b1515;");
        titleLabel.setAlignment(Pos.CENTER);
        titleLabel.setMaxWidth(Double.MAX_VALUE);
        
        Label subTitle = new Label("शेतकरी खाते (FARMER LEDGER)");
        subTitle.setStyle("-fx-font-family: 'Mangal', 'Nirmala UI', sans-serif; -fx-font-size: 20px; -fx-text-fill: blue;");
        subTitle.setAlignment(Pos.CENTER);
        subTitle.setMaxWidth(Double.MAX_VALUE);
        
        VBox headerBox = new VBox(5, titleLabel, subTitle);
        headerBox.setPadding(new Insets(0, 0, 20, 0));
        mainLayout.getChildren().add(headerBox);

        // --- Info ---
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String startStr = startDate != null ? startDate.format(formatter) : "-";
        String endStr = endDate != null ? endDate.format(formatter) : "-";
        
        GridPane infoGrid = new GridPane();
        infoGrid.setHgap(20);
        infoGrid.setVgap(10);
        infoGrid.getColumnConstraints().addAll(
                new ColumnConstraints(300),
                new ColumnConstraints(300),
                new ColumnConstraints(300)
        );
        
        infoGrid.add(createBoldLabel("नाव : " + farmer.getName(), 14), 0, 0);
        infoGrid.add(createBoldLabel("कालावधी : " + startStr + " ते " + endStr, 14), 2, 0);
        infoGrid.add(createBoldLabel("मोबाईल : " + (farmer.getMobile() != null ? farmer.getMobile() : ""), 14), 0, 1);
        String cType = farmer.getLastCaneType() != null ? farmer.getLastCaneType() : "-";
        infoGrid.add(createBoldLabel("उसाचा प्रकार : " + cType, 14), 0, 2);
        
        VBox infoBox = new VBox(infoGrid);
        infoBox.setPadding(new Insets(0, 0, 20, 0));
        mainLayout.getChildren().add(infoBox);

        // --- Table ---
        GridPane table = new GridPane();
        table.setStyle("-fx-border-color: black; -fx-border-width: 1;");
        
        double[] colWidths = {120, 150, 330, 100, 130, 130, 150};
        for (double w : colWidths) {
            ColumnConstraints cc = new ColumnConstraints(w);
            table.getColumnConstraints().add(cc);
        }
        
        String[] headers = {"तारीख", "बिल नं.", "तपशील", "वजन (टन)", "रक्कम (रु)", "जमा (रु)", "बाकी (रु)"};
        
        for (int i = 0; i < headers.length; i++) {
            Label h = createBoldLabel(headers[i], 12);
            h.setAlignment(Pos.CENTER);
            h.setMaxWidth(Double.MAX_VALUE);
            StackPane cell = createCell(h, true);
            table.add(cell, i, 0);
        }
        
        int row = 1;
        for (LedgerEntry entry : entries) {
            String date = entry.getDate().format(formatter);
            String billNo = entry.getBillNo() != null ? entry.getBillNo() : "";
            String part = entry.getParticulars();
            String weight = entry.getWeight() > 0 ? String.format("%.3f", entry.getWeight()) : "";
            String debit = entry.getDebit() > 0 ? String.format("%.2f", entry.getDebit()) : "";
            String credit = entry.getCredit() > 0 ? String.format("%.2f", entry.getCredit()) : "";
            String bal = String.format("%.2f", entry.getBalance());
            
            table.add(createCell(createAlignLabel(date, Pos.CENTER), false), 0, row);
            table.add(createCell(createAlignLabel(billNo, Pos.CENTER), false), 1, row);
            table.add(createCell(createAlignLabel(part, Pos.CENTER_LEFT), false), 2, row);
            table.add(createCell(createAlignLabel(weight, Pos.CENTER_RIGHT), false), 3, row);
            table.add(createCell(createAlignLabel(debit, Pos.CENTER_RIGHT), false), 4, row);
            table.add(createCell(createAlignLabel(credit, Pos.CENTER_RIGHT), false), 5, row);
            table.add(createCell(createAlignLabel(bal, Pos.CENTER_RIGHT), false), 6, row);
            
            row++;
        }
        
        mainLayout.getChildren().add(table);

        // Force layout
        new Scene(mainLayout);
        mainLayout.applyCss();
        mainLayout.layout();

        // Snapshot
        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.WHITE);
        // Scale for high quality
        params.setTransform(javafx.scene.transform.Transform.scale(2, 2));
        WritableImage image = mainLayout.snapshot(params, null);
        
        BufferedImage bImage = SwingFXUtils.fromFXImage(image, null);

        // Create PDF
        Document document = new Document(PageSize.A4.rotate(), 20, 20, 20, 20); // Landscape A4
        PdfWriter.getInstance(document, new FileOutputStream(file));
        document.open();
        
        // A4 Landscape is 842 x 595 points. With 20pt margins -> 802 x 555
        float pdfPageWidth = 802f;
        float pdfPageHeight = 555f;
        
        // The image width might be larger. We will scale the image so its width fits pdfPageWidth.
        float scaleFactor = pdfPageWidth / bImage.getWidth();
        int chunkHeightPx = (int) (pdfPageHeight / scaleFactor); // The max height in image pixels that fits on one PDF page
        
        int totalHeight = bImage.getHeight();
        int y = 0;
        
        while (y < totalHeight) {
            int h = Math.min(chunkHeightPx, totalHeight - y);
            BufferedImage chunk = bImage.getSubimage(0, y, bImage.getWidth(), h);
            
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(chunk, "png", baos);
            Image pdfImg = Image.getInstance(baos.toByteArray());
            
            pdfImg.scaleToFit(pdfPageWidth, pdfPageHeight);
            pdfImg.setAlignment(Image.ALIGN_CENTER | Image.ALIGN_TOP);
            
            document.add(pdfImg);
            y += h;
            
            if (y < totalHeight) {
                document.newPage();
            }
        }

        document.close();
        return file.getAbsolutePath();
    }
    
    private static StackPane createCell(Label label, boolean isHeader) {
        StackPane pane = new StackPane(label);
        pane.setPadding(new Insets(8));
        pane.setStyle("-fx-border-color: black; -fx-border-width: 0.5; " + (isHeader ? "-fx-background-color: #f0f0f0;" : ""));
        return pane;
    }
    
    private static Label createBoldLabel(String text, int size) {
        Label label = new Label(text);
        label.setStyle("-fx-font-family: 'Mangal', 'Nirmala UI', sans-serif; -fx-font-size: " + size + "px; -fx-text-fill: black; -fx-font-weight: bold;");
        return label;
    }
    
    private static Label createAlignLabel(String text, Pos pos) {
        Label label = new Label(text);
        label.setStyle("-fx-font-family: 'Mangal', 'Nirmala UI', sans-serif; -fx-font-size: 12px; -fx-text-fill: black;");
        label.setAlignment(pos);
        label.setMaxWidth(Double.MAX_VALUE);
        return label;
    }

    private static void showAlert(Alert.AlertType type, String title, String msg) {
        if (Platform.isFxApplicationThread()) {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(msg);
            alert.showAndWait();
        } else {
            Platform.runLater(() -> {
                Alert alert = new Alert(type);
                alert.setTitle(title);
                alert.setHeaderText(null);
                alert.setContentText(msg);
                alert.showAndWait();
            });
        }
    }
}

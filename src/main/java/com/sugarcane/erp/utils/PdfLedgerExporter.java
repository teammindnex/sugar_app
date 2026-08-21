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
    
    public static String generateCustomerLedgerPdf(com.sugarcane.erp.model.Customer customer, LocalDate startDate, LocalDate endDate, List<LedgerEntry> entries, Window ownerWindow) {
        String userHome = System.getProperty("user.home");
        File dir = new File(userHome, "SugarCaneBills");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File file = new File(dir, "CustomerLedger_" + customer.getName() + "_" + System.currentTimeMillis() + ".pdf");
        
        final String[] resultPath = new String[1];
        final Exception[] error = new Exception[1];
        
        if (Platform.isFxApplicationThread()) {
            try {
                resultPath[0] = createCustomerPdfFromSnapshot(customer, startDate, endDate, entries, file);
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Error", "Error generating Customer Ledger PDF: " + e.getMessage());
            }
        } else {
            CountDownLatch latch = new CountDownLatch(1);
            Platform.runLater(() -> {
                try {
                    resultPath[0] = createCustomerPdfFromSnapshot(customer, startDate, endDate, entries, file);
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
                    Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Error", "Error generating Customer Ledger PDF: " + error[0].getMessage()));
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        
        return resultPath[0];
    }

    private static String createCustomerPdfFromSnapshot(com.sugarcane.erp.model.Customer customer, LocalDate startDate, LocalDate endDate, List<LedgerEntry> entries, File file) throws Exception {
        VBox mainLayout = new VBox(0);
        mainLayout.setStyle("-fx-background-color: white;");
        mainLayout.setPrefWidth(1150);
        mainLayout.setPadding(new Insets(30));

        // --- Header ---
        Label titleLabel = new Label("श्री गणेश कृपा ऊस सप्लायर्स");
        titleLabel.setStyle("-fx-font-family: 'Mangal', 'Nirmala UI', sans-serif; -fx-font-weight: bold; -fx-font-size: 34px; -fx-text-fill: #1B5E20;");
        titleLabel.setAlignment(Pos.CENTER);
        titleLabel.setMaxWidth(Double.MAX_VALUE);
        
        StackPane subtitlePill = new StackPane();
        subtitlePill.setStyle("-fx-background-color: #FBE470; -fx-background-radius: 20; -fx-padding: 6 25;");
        Label headerSubTitle = new Label("खास रसवंतीसाठी वापरल्या जाणाऱ्या खेडशिवापूर च्या ऊसाचे होलसेल विक्रेते");
        headerSubTitle.setStyle("-fx-font-family: 'Mangal', 'Nirmala UI', sans-serif; -fx-font-size: 14px; -fx-text-fill: #1565C0;");
        subtitlePill.getChildren().add(headerSubTitle);

        VBox titleBox = new VBox(5, titleLabel, subtitlePill);
        titleBox.setAlignment(Pos.CENTER);

        HBox headerWithImages = new HBox(50);
        headerWithImages.setAlignment(Pos.CENTER);
        try {
            javafx.scene.image.Image scImage = new javafx.scene.image.Image(PdfLedgerExporter.class.getResourceAsStream("/images/sugarcanephotoforreceipt.png"));
            javafx.scene.image.ImageView leftSugarcane = new javafx.scene.image.ImageView(scImage);
            leftSugarcane.setFitHeight(100);
            leftSugarcane.setPreserveRatio(true);
            
            javafx.scene.image.ImageView rightSugarcane = new javafx.scene.image.ImageView(scImage);
            rightSugarcane.setFitHeight(100);
            rightSugarcane.setPreserveRatio(true);
            rightSugarcane.setScaleX(-1);
            
            headerWithImages.getChildren().addAll(leftSugarcane, titleBox, rightSugarcane);
        } catch (Exception e) {
            headerWithImages.getChildren().add(titleBox);
        }
        
        Label subTitle = new Label("ग्राहक खतावणी");
        subTitle.setStyle("-fx-font-family: 'Mangal', 'Nirmala UI', sans-serif; -fx-font-weight: bold; -fx-font-size: 20px; -fx-text-fill: #b30000; -fx-padding: 10 0 0 0;");
        subTitle.setAlignment(Pos.CENTER);
        subTitle.setMaxWidth(Double.MAX_VALUE);
        
        HBox propBox = new HBox(40);
        propBox.setAlignment(Pos.CENTER);
        propBox.setPadding(new Insets(10, 0, 10, 0));
        
        Label p1 = new Label("👤 प्रोप्रा. दादासो कोंडे\n9763948154 / 7588237123");
        p1.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        p1.setStyle("-fx-font-family: 'Mangal', 'Nirmala UI', sans-serif; -fx-font-size: 16px; -fx-font-weight: bold;");
        
        javafx.scene.control.Separator sep = new javafx.scene.control.Separator(javafx.geometry.Orientation.VERTICAL);
        sep.setStyle("-fx-background-color: #81C784;");
        
        Label p2 = new Label("👤 प्रोप्रा. विराज कोंडे\n8999875886 / 7588237122");
        p2.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        p2.setStyle("-fx-font-family: 'Mangal', 'Nirmala UI', sans-serif; -fx-font-size: 16px; -fx-font-weight: bold;");
        
        propBox.getChildren().addAll(p1, sep, p2);
        
        VBox headerBox = new VBox(10, headerWithImages, propBox, subTitle);
        headerBox.setAlignment(Pos.CENTER);
        headerBox.setPadding(new Insets(0, 0, 20, 0));
        mainLayout.getChildren().add(headerBox);

        // --- Info ---
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
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
        
        infoGrid.add(createBoldLabel("नाव : " + customer.getName(), 14), 0, 0);
        infoGrid.add(createBoldLabel("कालावधी : " + startStr + " ते " + endStr, 14), 2, 0);
        infoGrid.add(createBoldLabel("मोबाईल : " + (customer.getMobile() != null ? customer.getMobile() : ""), 14), 0, 1);
        String addr = customer.getAddress() != null ? customer.getAddress() : "-";
        infoGrid.add(createBoldLabel("पत्ता : " + addr, 14), 0, 2);
        
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

        new Scene(mainLayout);
        mainLayout.applyCss();
        mainLayout.layout();

        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.WHITE);
        params.setTransform(javafx.scene.transform.Transform.scale(2, 2));
        WritableImage image = mainLayout.snapshot(params, null);
        
        BufferedImage bImage = SwingFXUtils.fromFXImage(image, null);

        Document document = new Document(PageSize.A4.rotate(), 20, 20, 20, 20);
        PdfWriter.getInstance(document, new FileOutputStream(file));
        document.open();
        
        float pdfPageWidth = 802f;
        float pdfPageHeight = 555f;
        
        float scaleFactor = pdfPageWidth / bImage.getWidth();
        int chunkHeightPx = (int) (pdfPageHeight / scaleFactor);
        
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
    
    private static String createPdfFromSnapshot(Farmer farmer, LocalDate startDate, LocalDate endDate, List<LedgerEntry> entries, File file) throws Exception {
        VBox mainLayout = new VBox(0);
        mainLayout.setStyle("-fx-background-color: white;");
        // Landscape A4 width is ~1123, we use a wide layout
        mainLayout.setPrefWidth(1150);
        mainLayout.setPadding(new Insets(30));

        // --- Header ---
        Label titleLabel = new Label("श्री गणेश कृपा ऊस सप्लायर्स");
        titleLabel.setStyle("-fx-font-family: 'Mangal', 'Nirmala UI', sans-serif; -fx-font-weight: bold; -fx-font-size: 34px; -fx-text-fill: #1B5E20;");
        titleLabel.setAlignment(Pos.CENTER);
        titleLabel.setMaxWidth(Double.MAX_VALUE);
        
        StackPane subtitlePill = new StackPane();
        subtitlePill.setStyle("-fx-background-color: #FBE470; -fx-background-radius: 20; -fx-padding: 6 25;");
        Label headerSubTitle = new Label("खास रसवंतीसाठी वापरल्या जाणाऱ्या खेडशिवापूर च्या ऊसाचे होलसेल विक्रेते");
        headerSubTitle.setStyle("-fx-font-family: 'Mangal', 'Nirmala UI', sans-serif; -fx-font-size: 14px; -fx-text-fill: #1565C0;");
        subtitlePill.getChildren().add(headerSubTitle);

        VBox titleBox = new VBox(5, titleLabel, subtitlePill);
        titleBox.setAlignment(Pos.CENTER);

        HBox headerWithImages = new HBox(50);
        headerWithImages.setAlignment(Pos.CENTER);
        try {
            javafx.scene.image.Image scImage = new javafx.scene.image.Image(PdfLedgerExporter.class.getResourceAsStream("/images/sugarcanephotoforreceipt.png"));
            javafx.scene.image.ImageView leftSugarcane = new javafx.scene.image.ImageView(scImage);
            leftSugarcane.setFitHeight(100);
            leftSugarcane.setPreserveRatio(true);
            
            javafx.scene.image.ImageView rightSugarcane = new javafx.scene.image.ImageView(scImage);
            rightSugarcane.setFitHeight(100);
            rightSugarcane.setPreserveRatio(true);
            rightSugarcane.setScaleX(-1);
            
            headerWithImages.getChildren().addAll(leftSugarcane, titleBox, rightSugarcane);
        } catch (Exception e) {
            headerWithImages.getChildren().add(titleBox);
        }
        
        Label subTitle = new Label("शेतकरी खतावणी");
        subTitle.setStyle("-fx-font-family: 'Mangal', 'Nirmala UI', sans-serif; -fx-font-weight: bold; -fx-font-size: 20px; -fx-text-fill: #b30000; -fx-padding: 10 0 0 0;");
        subTitle.setAlignment(Pos.CENTER);
        subTitle.setMaxWidth(Double.MAX_VALUE);
        
        HBox propBox = new HBox(40);
        propBox.setAlignment(Pos.CENTER);
        propBox.setPadding(new Insets(10, 0, 10, 0));
        
        Label p1 = new Label("👤 प्रोप्रा. दादासो कोंडे\n9763948154 / 7588237123");
        p1.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        p1.setStyle("-fx-font-family: 'Mangal', 'Nirmala UI', sans-serif; -fx-font-size: 16px; -fx-font-weight: bold;");
        
        javafx.scene.control.Separator sep = new javafx.scene.control.Separator(javafx.geometry.Orientation.VERTICAL);
        sep.setStyle("-fx-background-color: #81C784;");
        
        Label p2 = new Label("👤 प्रोप्रा. विराज कोंडे\n8999875886 / 7588237122");
        p2.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        p2.setStyle("-fx-font-family: 'Mangal', 'Nirmala UI', sans-serif; -fx-font-size: 16px; -fx-font-weight: bold;");
        
        propBox.getChildren().addAll(p1, sep, p2);
        
        VBox headerBox = new VBox(10, headerWithImages, propBox, subTitle);
        headerBox.setAlignment(Pos.CENTER);
        headerBox.setPadding(new Insets(0, 0, 20, 0));
        mainLayout.getChildren().add(headerBox);

        // --- Info ---
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
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

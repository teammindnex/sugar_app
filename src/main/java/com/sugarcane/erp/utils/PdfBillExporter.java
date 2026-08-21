package com.sugarcane.erp.utils;

import com.itextpdf.text.Document;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfWriter;
import com.sugarcane.erp.controller.FarmerController;
import com.sugarcane.erp.model.Farmer;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PdfBillExporter {

    private static final String FONT_FAMILY = "'Mangal', 'Nirmala UI', sans-serif";
    
    public static File generateBillPdf(Farmer farmer, String billNo, LocalDate date, List<FarmerController.BillItem> items, double totalAmount) throws Exception {
        VBox mainContainer = new VBox();
        mainContainer.setPadding(new Insets(10));
        mainContainer.setStyle("-fx-background-color: white; -fx-border-color: #9E9E9E; -fx-border-width: 2;");
        mainContainer.setPrefWidth(600);

        VBox innerContainer = new VBox(8);
        innerContainer.setPadding(new Insets(15));
        innerContainer.setStyle("-fx-border-color: #4CAF50; -fx-border-width: 2;");

        // 1. Top Gods Header
        HBox topGodsBox = new HBox();
        topGodsBox.setAlignment(Pos.CENTER);
        
        try {
            VBox ganeshBox = createGodBox("|| श्री गणेश प्रसन्न ||", "/images/ganesh.png");
            topGodsBox.getChildren().add(ganeshBox);
        } catch (Exception e) {}

        // 2. Main Title and Subtitle
        VBox titleBox = new VBox(5);
        titleBox.setAlignment(Pos.CENTER);
        
        Label titleLabel = new Label("श्री गणेश कृपा ऊस सप्लायर्स");
        titleLabel.setStyle("-fx-font-family: " + FONT_FAMILY + "; -fx-font-weight: bold; -fx-font-size: 34px; -fx-text-fill: #1B5E20;");
        
        StackPane subtitlePill = new StackPane();
        subtitlePill.setStyle("-fx-background-color: #FBE470; -fx-background-radius: 20; -fx-padding: 6 25;");
        Label subTitle = new Label("खास रसवंतीसाठी वापरल्या जाणाऱ्या खेडशिवापूर च्या ऊसाचे होलसेल विक्रेते");
        subTitle.setStyle("-fx-font-family: " + FONT_FAMILY + "; -fx-font-size: 14px; -fx-text-fill: #1565C0;");
        subtitlePill.getChildren().add(subTitle);
        
        titleBox.getChildren().addAll(titleLabel, subtitlePill);

        // 3. Receipt Type Pill
        HBox receiptTypeBox = new HBox(15);
        receiptTypeBox.setAlignment(Pos.CENTER);
        
        StackPane typePill = new StackPane();
        typePill.setStyle("-fx-background-color: #1B5E20; -fx-background-radius: 25; -fx-padding: 8 30;");
        Label typeLabel = new Label("ऊस खरेदी पावती");
        typeLabel.setStyle("-fx-font-family: " + FONT_FAMILY + "; -fx-font-weight: bold; -fx-font-size: 22px; -fx-text-fill: white;");
        typePill.getChildren().add(typeLabel);
        
        receiptTypeBox.getChildren().addAll(typePill);

        Label tagline = new Label("|| आमची सेवा, शेतकऱ्यांचा विश्वास ||");
        tagline.setStyle("-fx-font-family: " + FONT_FAMILY + "; -fx-font-size: 14px;");
        tagline.setAlignment(Pos.CENTER);
        tagline.setMaxWidth(Double.MAX_VALUE);

        // 4. Proprietors Section
        HBox propBox = new HBox(40);
        propBox.setAlignment(Pos.CENTER);
        propBox.setPadding(new Insets(10, 0, 10, 0));
        
        Label p1 = new Label("👤 प्रोप्रा. दादासो कोंडे\n9763948154 / 7588237123");
        p1.setTextAlignment(TextAlignment.CENTER);
        p1.setStyle("-fx-font-family: " + FONT_FAMILY + "; -fx-font-size: 16px; -fx-font-weight: bold;");
        
        Separator sep = new Separator(Orientation.VERTICAL);
        sep.setStyle("-fx-background-color: #81C784;");
        
        Label p2 = new Label("👤 प्रोप्रा. विराज कोंडे\n8999875886 / 7588237122");
        p2.setTextAlignment(TextAlignment.CENTER);
        p2.setStyle("-fx-font-family: " + FONT_FAMILY + "; -fx-font-size: 16px; -fx-font-weight: bold;");
        
        propBox.getChildren().addAll(p1, sep, p2);

        // Divider
        Separator horizSep = new Separator(Orientation.HORIZONTAL);
        horizSep.setStyle("-fx-background-color: #388E3C;");

        // 5. Info Box (Farmer Details)
        GridPane infoGrid = new GridPane();
        infoGrid.setHgap(15);
        infoGrid.setVgap(8);
        infoGrid.setPadding(new Insets(10));
        infoGrid.setStyle("-fx-border-color: #A5D6A7; -fx-border-width: 2; -fx-border-radius: 10; -fx-background-radius: 10; -fx-background-color: #FAFAFA;");
        
        ColumnConstraints c1 = new ColumnConstraints(); c1.setPercentWidth(20);
        ColumnConstraints c2 = new ColumnConstraints(); c2.setPercentWidth(33);
        ColumnConstraints c3 = new ColumnConstraints(); c3.setPercentWidth(17);
        ColumnConstraints c4 = new ColumnConstraints(); c4.setPercentWidth(30);
        infoGrid.getColumnConstraints().addAll(c1, c2, c3, c4);

        addInfoRow(infoGrid, 0, "👤 शेतकऱ्याचे नाव", farmer.getName(), "🧾 बिल क्रमांक", billNo);
        addInfoRow(infoGrid, 1, "🛺 मोबाईल नंबर", farmer.getMobile(), "📅 तारीख", date.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));

        // 6. Items Table
        VBox tableContainer = new VBox();
        tableContainer.setStyle("-fx-border-color: #81C784; -fx-border-width: 2; -fx-border-radius: 10; -fx-background-radius: 10; -fx-background-color: white;");
        
        GridPane table = new GridPane();
        table.setPadding(new Insets(0));
        
        ColumnConstraints tc1 = new ColumnConstraints(); tc1.setPercentWidth(15);
        ColumnConstraints tc2 = new ColumnConstraints(); tc2.setPercentWidth(25);
        ColumnConstraints tc3 = new ColumnConstraints(); tc3.setPercentWidth(20);
        ColumnConstraints tc4 = new ColumnConstraints(); tc4.setPercentWidth(20);
        ColumnConstraints tc5 = new ColumnConstraints(); tc5.setPercentWidth(20);
        table.getColumnConstraints().addAll(tc1, tc2, tc3, tc4, tc5);

        // Table Header
        String headStyle = "-fx-background-color: #1B5E20; -fx-padding: 10; -fx-border-color: #1B5E20; -fx-border-width: 0 1 1 0;";
        Label th1 = createStyledLabel("अ. क्र.", headStyle, true);
        Label th2 = createStyledLabel("उसाचा प्रकार", headStyle, true);
        Label th3 = createStyledLabel("वजन (टन)", headStyle, true);
        Label th4 = createStyledLabel("दर प्रति टन (₹)", headStyle, true);
        Label th5 = createStyledLabel("रक्कम (₹)", "-fx-background-color: #1B5E20; -fx-padding: 10; -fx-border-color: #1B5E20; -fx-border-width: 0 0 1 0;", true);
        th1.setStyle(th1.getStyle() + "-fx-background-radius: 8 0 0 0;");
        th5.setStyle(th5.getStyle() + "-fx-background-radius: 0 8 0 0;");
        
        table.addRow(0, th1, th2, th3, th4, th5);
        
        int row = 1;
        for (FarmerController.BillItem item : items) {
            String bg = (row % 2 == 0) ? "#F5F5F5" : "#FFFFFF";
            String cellStyle = "-fx-background-color: " + bg + "; -fx-padding: 12 10; -fx-border-color: #E0E0E0; -fx-border-width: 0 1 1 0;";
            String lastCellStyle = "-fx-background-color: " + bg + "; -fx-padding: 12 10; -fx-border-color: #E0E0E0; -fx-border-width: 0 0 1 0;";
            
            table.addRow(row, 
                createStyledLabel(String.valueOf(row), cellStyle, false),
                createStyledLabel(item.getCaneType(), cellStyle, false),
                createStyledLabel(String.format("%.3f", item.getWeight()), cellStyle, false),
                createStyledLabel(String.format("%.2f", item.getRate()), cellStyle, false),
                createStyledLabel(String.format("%.2f", item.getAmount()), lastCellStyle, false)
            );
            row++;
        }
        
        tableContainer.getChildren().add(table);

        // Total Box REMOVED as per user request

        // Note
        Label noteLabel = new Label("टीप: योग्य दरात ऊस खरेदी केली जाईल.");
        noteLabel.setStyle("-fx-font-family: " + FONT_FAMILY + "; -fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #D32F2F;");
        noteLabel.setAlignment(Pos.CENTER);
        noteLabel.setMaxWidth(Double.MAX_VALUE);

        // 7. Footer Signatures
        HBox thanksBox = new HBox(10);
        thanksBox.setAlignment(Pos.CENTER);
        Label tLeafLeft = new Label("🌿"); tLeafLeft.setStyle("-fx-font-size: 18px;");
        Label tLabel = new Label("धन्यवाद!");
        tLabel.setStyle("-fx-font-family: " + FONT_FAMILY + "; -fx-font-style: italic; -fx-font-weight: bold; -fx-font-size: 16px;");
        Label tLeafRight = new Label("🌿"); tLeafRight.setStyle("-fx-font-size: 18px;");
        
        Separator leftDash = new Separator(); leftDash.setPrefWidth(100);
        Separator rightDash = new Separator(); rightDash.setPrefWidth(100);
        thanksBox.getChildren().addAll(leftDash, tLeafLeft, tLabel, tLeafRight, rightDash);
        
        HBox sigBox = new HBox();
        sigBox.setAlignment(Pos.CENTER);
        
        VBox farmerSig = new VBox(5);
        farmerSig.setAlignment(Pos.CENTER);
        Label fSigLabel = new Label("✍️ शेतकरी सही");
        fSigLabel.setStyle("-fx-font-family: " + FONT_FAMILY + "; -fx-font-size: 12px; -fx-font-weight: bold;");
        Label fLine = new Label("------------------------");
        farmerSig.getChildren().addAll(fSigLabel, fLine);
        
        // Quality Badge / Logo
        StackPane badge = new StackPane();
        try {
            javafx.scene.image.ImageView logoView = new javafx.scene.image.ImageView(new javafx.scene.image.Image(PdfBillExporter.class.getResourceAsStream("/images/company_logo.png")));
            logoView.setFitWidth(50);
            logoView.setFitHeight(50);
            badge.setStyle("-fx-background-color: white; -fx-background-radius: 50; -fx-border-color: #4CAF50; -fx-border-width: 2; -fx-border-radius: 50; -fx-padding: 3;");
            badge.setMaxSize(60, 60);
            badge.getChildren().add(logoView);
        } catch (Exception e) {
            badge.setStyle("-fx-background-color: white; -fx-border-color: #1B5E20; -fx-border-width: 2; -fx-border-radius: 50; -fx-background-radius: 50; -fx-border-style: dashed;");
            badge.setPrefSize(60, 60);
            badge.setMaxSize(60, 60);
            Label badgeText = new Label("QUALITY CANE\n\n\nBETTER FUTURE");
            badgeText.setTextAlignment(TextAlignment.CENTER);
            badgeText.setStyle("-fx-font-size: 8px; -fx-font-weight: bold; -fx-text-fill: #1B5E20;");
            Label badgeIcon = new Label("*");
            badgeIcon.setStyle("-fx-font-size: 24px; -fx-text-fill: #1B5E20;");
            badge.getChildren().addAll(badgeText, badgeIcon);
        }
        
        VBox supplierSig = new VBox(5);
        supplierSig.setAlignment(Pos.CENTER);
        Label sSigLabel = new Label("✍️ सप्लायर सही");
        sSigLabel.setStyle("-fx-font-family: " + FONT_FAMILY + "; -fx-font-size: 12px; -fx-font-weight: bold;");
        Label sLine = new Label("------------------------");
        supplierSig.getChildren().addAll(sSigLabel, sLine);
        
        Region s1 = new Region(); HBox.setHgrow(s1, Priority.ALWAYS);
        Region s2 = new Region(); HBox.setHgrow(s2, Priority.ALWAYS);
        
        sigBox.getChildren().addAll(farmerSig, s1, badge, s2, supplierSig);

        // 8. Bottom Bar
        HBox bottomBar = new HBox();
        bottomBar.setStyle("-fx-background-color: #E0E0E0; -fx-padding: 10 20;");
        Label bLeft = new Label("📄 ही पावती संगणकाद्वारे तयार करण्यात आली आहे.");
        bLeft.setStyle("-fx-font-family: " + FONT_FAMILY + "; -fx-font-size: 12px;");
        Region bSpacer = new Region(); HBox.setHgrow(bSpacer, Priority.ALWAYS);
        Label bRight = new Label("धन्यवाद ! पुन्हा भेट द्या.");
        bRight.setStyle("-fx-font-family: " + FONT_FAMILY + "; -fx-font-size: 12px;");
        bottomBar.getChildren().addAll(bLeft, bSpacer, bRight);

        innerContainer.getChildren().addAll(
            topGodsBox, titleBox, receiptTypeBox, tagline, 
            propBox, horizSep, infoGrid, tableContainer, 
            noteLabel, thanksBox, sigBox, bottomBar
        );

        StackPane layeredContainer = new StackPane();
        layeredContainer.getChildren().add(innerContainer);

        try {
            javafx.scene.image.Image scImage = new javafx.scene.image.Image(PdfBillExporter.class.getResourceAsStream("/images/sugarcanephotoforreceipt.png"));
            
            javafx.scene.image.ImageView leftSugarcane = new javafx.scene.image.ImageView(scImage);
            leftSugarcane.setFitHeight(140);
            leftSugarcane.setPreserveRatio(true);
            StackPane.setAlignment(leftSugarcane, Pos.TOP_LEFT);
            StackPane.setMargin(leftSugarcane, new Insets(110, 0, 0, -20));
            
            javafx.scene.image.ImageView rightSugarcane = new javafx.scene.image.ImageView(scImage);
            rightSugarcane.setFitHeight(140);
            rightSugarcane.setPreserveRatio(true);
            rightSugarcane.setScaleX(-1); // flip horizontally
            StackPane.setAlignment(rightSugarcane, Pos.TOP_RIGHT);
            StackPane.setMargin(rightSugarcane, new Insets(110, -20, 0, 0));
            
            Label watermark = new Label("श्री गणेश ऊस सप्लायर");
            watermark.setStyle("-fx-font-family: " + FONT_FAMILY + "; -fx-font-size: 70px; -fx-font-weight: bold; -fx-text-fill: rgba(27, 94, 32, 0.08);");
            watermark.setRotate(-35);
            StackPane.setAlignment(watermark, Pos.CENTER);
            
            layeredContainer.getChildren().addAll(leftSugarcane, rightSugarcane, watermark);
        } catch (Exception e) {}

        mainContainer.getChildren().add(layeredContainer);

        // Render and Snapshot
        new Scene(mainContainer);
        mainContainer.applyCss();
        mainContainer.layout();

        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.WHITE);
        params.setTransform(javafx.scene.transform.Transform.scale(2, 2));
        WritableImage image = mainContainer.snapshot(params, null);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", baos);
        byte[] imageBytes = baos.toByteArray();

        File dir = new File(System.getProperty("user.home"), "SugarCaneBills");
        if (!dir.exists()) dir.mkdirs();
        File pdfFile = new File(dir, "Bill_" + billNo + ".pdf");

        Image pdfImg = Image.getInstance(imageBytes);
        float pdfWidth = 450f; // Slightly wider than A5 (420)
        float pdfHeight = pdfWidth * (pdfImg.getHeight() / pdfImg.getWidth());
        
        com.itextpdf.text.Rectangle pageSize = new com.itextpdf.text.Rectangle(pdfWidth, pdfHeight);
        Document document = new Document(pageSize, 0, 0, 0, 0);
        PdfWriter.getInstance(document, new FileOutputStream(pdfFile));
        document.open();

        pdfImg.scaleToFit(pdfWidth, pdfHeight);
        pdfImg.setAbsolutePosition(0, 0);
        document.add(pdfImg);
        document.close();

        return pdfFile;
    }

    private static VBox createGodBox(String topText, String imgPath) {
        VBox box = new VBox(8);
        box.setAlignment(Pos.CENTER);
        Label label = new Label(topText);
        label.setStyle("-fx-font-family: " + FONT_FAMILY + "; -fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #b30000;");
        try {
            javafx.scene.image.ImageView imgView = new javafx.scene.image.ImageView(new javafx.scene.image.Image(PdfBillExporter.class.getResourceAsStream(imgPath)));
            imgView.setFitWidth(55);
            imgView.setFitHeight(55);
            
            // Add rounded corner border effect to the image
            StackPane imgContainer = new StackPane(imgView);
            imgContainer.setStyle("-fx-border-color: #8D6E63; -fx-border-width: 4; -fx-border-radius: 10; -fx-background-color: #EFEBE9; -fx-background-radius: 10; -fx-padding: 5;");
            
            box.getChildren().addAll(label, imgContainer);
        } catch (Exception e) {}
        return box;
    }

    private static void addInfoRow(GridPane grid, int row, String l1, String v1, String l2, String v2) {
        Label lbl1 = new Label(l1);
        lbl1.setStyle("-fx-font-family: " + FONT_FAMILY + "; -fx-font-weight: bold; -fx-font-size: 14px;");
        
        Label val1 = new Label(" : " + v1);
        val1.setStyle("-fx-font-family: " + FONT_FAMILY + "; -fx-font-size: 14px;");
        
        Label lbl2 = new Label(l2);
        lbl2.setStyle("-fx-font-family: " + FONT_FAMILY + "; -fx-font-weight: bold; -fx-font-size: 14px;");
        
        Label val2 = new Label(" : " + v2);
        val2.setStyle("-fx-font-family: " + FONT_FAMILY + "; -fx-font-size: 14px;");
        
        grid.add(lbl1, 0, row);
        grid.add(val1, 1, row);
        grid.add(lbl2, 2, row);
        grid.add(val2, 3, row);
    }
    
    private static Label createStyledLabel(String text, String style, boolean isHeader) {
        Label label = new Label(text);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setAlignment(Pos.CENTER);
        
        String fontStyle = "-fx-font-family: " + FONT_FAMILY + "; -fx-font-size: 14px;";
        if (isHeader) {
            fontStyle += " -fx-font-weight: bold; -fx-text-fill: white;";
            style += " -fx-text-fill: white;"; // Force white color in inline style
        }
        
        label.setStyle(fontStyle + " " + style);
        return label;
    }
}

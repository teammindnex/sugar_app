package com.sugarcane.erp.controller;

import com.sugarcane.erp.model.Farmer;
import com.sugarcane.erp.service.FarmerService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.File;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.awt.Desktop;
import java.time.LocalDate;
import java.util.List;
import java.sql.SQLException;

import com.sugarcane.erp.dao.PurchaseDAO;
import com.sugarcane.erp.dao.FarmerPaymentDAO;
import com.sugarcane.erp.model.Purchase;
import com.sugarcane.erp.model.FarmerPayment;
import javafx.scene.layout.GridPane;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.stage.FileChooser;

public class FarmerController {

    // --- New Bill Tab Fields ---
    @FXML private TextField farmerNameField;
    @FXML private TextField mobileField;
    @FXML private DatePicker datePicker;
    @FXML private TextField billNoField;
    
    // State variables for Transport Dialog
    private String transportNameState = "";
    private String vehicleNoState = "";
    private String emptyWeightState = "";
    private String loadedWeightState = "";
    private String photoPathState = "";
    // Product Entry
    @FXML private ComboBox<String> caneTypeCombo;
    @FXML private TextField weightField;
    @FXML private TextField rateField;
    
    // Item Table
    @FXML private TableView<BillItem> itemTable;
    @FXML private TableColumn<BillItem, String> colCaneType;
    @FXML private TableColumn<BillItem, String> colWeight;
    @FXML private TableColumn<BillItem, String> colRate;
    @FXML private TableColumn<BillItem, String> colAmount;
    
    // Totals
    @FXML private Label totalAmtLabel;
    @FXML private TextField previousBalanceField;
    @FXML private TextField advanceField;
    @FXML private Label finalBalanceLabel;
    
    // --- Farmer History Tab Fields ---
    @FXML private TextField searchHistoryField;
    @FXML private TableView<Farmer> farmerHistoryTable;
    @FXML private TableColumn<Farmer, String> colHistName;
    @FXML private TableColumn<Farmer, String> colHistMobile;
    @FXML private TableColumn<Farmer, String> colHistCaneType;
    @FXML private TableColumn<Farmer, String> colHistTotalWeight;
    @FXML private TableColumn<Farmer, String> colHistDate;
    @FXML private TableColumn<Farmer, String> colHistDeneBaki;

    private FarmerService farmerService;
    private PurchaseDAO purchaseDAO = new PurchaseDAO();
    private FarmerPaymentDAO paymentDAO = new FarmerPaymentDAO();
    private ObservableList<BillItem> billItemsList = FXCollections.observableArrayList();
    private ObservableList<Farmer> farmerList = FXCollections.observableArrayList();

    public static class BillItem {
        private String caneType;
        private double emptyWeight;
        private double loadedWeight;
        private double weight;
        private double rate;
        private double amount;

        public BillItem(String caneType, double emptyWeight, double loadedWeight, double weight, double rate, double amount) {
            this.caneType = caneType;
            this.emptyWeight = emptyWeight;
            this.loadedWeight = loadedWeight;
            this.weight = weight;
            this.rate = rate;
            this.amount = amount;
        }

        public String getCaneType() { return caneType; }
        public double getEmptyWeight() { return emptyWeight; }
        public double getLoadedWeight() { return loadedWeight; }
        public double getWeight() { return weight; }
        public double getRate() { return rate; }
        public double getAmount() { return amount; }
    }

    @FXML
    public void initialize() {
        farmerService = new FarmerService();
        datePicker.setConverter(new javafx.util.StringConverter<LocalDate>() {
            private java.time.format.DateTimeFormatter dateFormatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");

            @Override
            public String toString(LocalDate date) {
                if (date != null) {
                    return dateFormatter.format(date);
                } else {
                    return "";
                }
            }

            @Override
            public LocalDate fromString(String string) {
                if (string != null && !string.isEmpty()) {
                    return LocalDate.parse(string, dateFormatter);
                } else {
                    return null;
                }
            }
        });
        datePicker.setValue(LocalDate.now());
        
        // Setup Combo Box (Sugarcane Types)
        caneTypeCombo.setItems(FXCollections.observableArrayList("419", "3102", "86032"));
        
        // Setup Item Table Columns
        colCaneType.setCellValueFactory(new PropertyValueFactory<>("caneType"));
        colWeight.setCellValueFactory(new PropertyValueFactory<>("weight"));
        colRate.setCellValueFactory(new PropertyValueFactory<>("rate"));
        colAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        itemTable.setItems(billItemsList);
        
        // Setup History Table Columns (Dummy mappings for now since model is just Farmer)
        colHistName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colHistMobile.setCellValueFactory(new PropertyValueFactory<>("mobile"));
        colHistCaneType.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getLastCaneType() != null ? cellData.getValue().getLastCaneType() : "-"));
        colHistTotalWeight.setCellValueFactory(cellData -> new SimpleStringProperty(String.format("%.2f", cellData.getValue().getTotalWeight())));
        colHistDate.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getLastDate() != null ? cellData.getValue().getLastDate() : "-"));
        colHistDeneBaki.setCellValueFactory(cellData -> new SimpleStringProperty(String.format("%.2f", cellData.getValue().getCurrentBalance())));
        
        // Remove Marathi Transliterator as per user request to use normal English
        // com.sugarcane.erp.utils.MarathiTransliterator.attach(farmerNameField);
        
        // Setup Auto-suggestion for Farmer Name
        ContextMenu autoSuggestMenu = new ContextMenu();
        farmerNameField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.trim().isEmpty()) {
                autoSuggestMenu.hide();
                return;
            }
            // Only show suggestions if the user is typing, not if they selected from the menu
            if (autoSuggestMenu.isShowing() && autoSuggestMenu.getItems().stream().anyMatch(item -> item.getText().equals(newValue))) {
                return;
            }
            
            String filter = newValue.toLowerCase();
            autoSuggestMenu.getItems().clear();
            int count = 0;
            for (Farmer f : farmerList) {
                if (f.getName() != null && f.getName().toLowerCase().contains(filter)) {
                    MenuItem item = new MenuItem(f.getName());
                    item.setOnAction(e -> {
                        farmerNameField.setText(f.getName());
                        mobileField.setText(f.getMobile() != null ? f.getMobile() : "");
                        autoSuggestMenu.hide();
                    });
                    autoSuggestMenu.getItems().add(item);
                    count++;
                    if (count >= 10) break; // Limit suggestions to 10
                }
            }
            
            if (!autoSuggestMenu.getItems().isEmpty()) {
                if (!autoSuggestMenu.isShowing()) {
                    autoSuggestMenu.show(farmerNameField, javafx.geometry.Side.BOTTOM, 0, 0);
                }
            } else {
                autoSuggestMenu.hide();
            }
        });
        
        // Hide menu when focus is lost
        farmerNameField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) autoSuggestMenu.hide();
        });

        // Add listeners for total calculation
        previousBalanceField.textProperty().addListener((obs, oldV, newV) -> calculateFinalBalance());
        advanceField.textProperty().addListener((obs, oldV, newV) -> calculateFinalBalance());

        // Restrict mobileField to exactly 10 digits
        mobileField.setTextFormatter(new TextFormatter<>(change -> {
            String text = change.getControlNewText();
            if (text.matches("\\d{0,10}")) {
                return change;
            }
            return null;
        }));
        
        // Restrict weightField and rateField to numbers and single decimal point
        java.util.function.UnaryOperator<TextFormatter.Change> doubleFilter = change -> {
            String text = change.getControlNewText();
            if (text.matches("\\d*\\.?\\d*")) {
                return change;
            }
            return null;
        };
        weightField.setTextFormatter(new TextFormatter<>(doubleFilter));
        rateField.setTextFormatter(new TextFormatter<>(doubleFilter));
        
        searchHistoryField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.trim().isEmpty()) {
                farmerHistoryTable.setItems(farmerList);
            } else {
                javafx.collections.ObservableList<Farmer> filteredList = javafx.collections.FXCollections.observableArrayList();
                String lowerCaseFilter = newValue.toLowerCase();
                for (Farmer farmer : farmerList) {
                    if ((farmer.getName() != null && farmer.getName().toLowerCase().contains(lowerCaseFilter)) ||
                        (farmer.getMobile() != null && farmer.getMobile().contains(lowerCaseFilter))) {
                        filteredList.add(farmer);
                    }
                }
                farmerHistoryTable.setItems(filteredList);
            }
        });

        loadFarmers();
        refreshBillNo();
    }

    private void refreshBillNo() {
        try {
            String nextBillNo = purchaseDAO.getNextBillNo();
            billNoField.setText(nextBillNo);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void loadFarmers() {
        try {
            farmerList.clear();
            java.util.List<Farmer> allFarmers = farmerService.getAllFarmers();
            com.sugarcane.erp.service.LedgerService ls = new com.sugarcane.erp.service.LedgerService();
            for (Farmer f : allFarmers) {
                java.util.List<com.sugarcane.erp.model.LedgerEntry> entries = ls.getFarmerLedger(f);
                if (!entries.isEmpty()) {
                    f.setCurrentBalance(entries.get(entries.size() - 1).getBalance());
                } else {
                    f.setCurrentBalance(f.getOpeningBalance());
                }
                
                double totalW = 0.0;
                String lastCane = "-";
                String lastDate = "-";
                
                for (com.sugarcane.erp.model.LedgerEntry entry : entries) {
                    if (entry.getWeight() > 0) {
                        totalW += entry.getWeight();
                        if (entry.getCaneType() != null && !entry.getCaneType().isEmpty()) {
                            lastCane = entry.getCaneType();
                        }
                        if (entry.getDate() != null) {
                            lastDate = entry.getDate().toString();
                        }
                    }
                }
                
                f.setTotalWeight(totalW);
                f.setLastCaneType(lastCane);
                f.setLastDate(lastDate);
            }
            farmerList.addAll(allFarmers);
            farmerHistoryTable.setItems(farmerList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAddItem() {
        if (farmerNameField.getText().trim().isEmpty() || mobileField.getText().trim().isEmpty()) {
            showAlert("कृपया शेतकऱ्याचे नाव आणि मोबाईल नंबर प्रविष्ट करा!");
            return;
        }
        
        if (mobileField.getText().trim().length() != 10) {
            showAlert("कृपया 10 अंकी मोबाईल नंबर प्रविष्ट करा!");
            return;
        }
        
        if (transportNameState.trim().isEmpty() || vehicleNoState.trim().isEmpty() || 
            emptyWeightState.trim().isEmpty() || loadedWeightState.trim().isEmpty() || 
            photoPathState.trim().isEmpty()) {
            showAlert("कृपया 'वजन तपशील' मधील सर्व माहिती (वाहतूक, गाडी नं, वजन आणि पावती फोटो) पूर्ण भरा!");
            return;
        }

        if (caneTypeCombo.getValue() == null || weightField.getText().trim().isEmpty() || rateField.getText().trim().isEmpty()) {
            showAlert("कृपया उसाचा प्रकार, वजन आणि भाव प्रविष्ट करा!");
            return;
        }
        
        try {
            String caneType = caneTypeCombo.getValue();
            double weight = Double.parseDouble(weightField.getText());
            double rate = Double.parseDouble(rateField.getText());
            double amount = weight * rate;
            double emptyW = emptyWeightState.trim().isEmpty() ? 0 : Double.parseDouble(emptyWeightState);
            double loadedW = loadedWeightState.trim().isEmpty() ? 0 : Double.parseDouble(loadedWeightState);
            
            BillItem item = new BillItem(caneType, emptyW, loadedW, weight, rate, amount);
            billItemsList.add(item);
            
            updateTotals();
            
            // Clear fields
            caneTypeCombo.getSelectionModel().clearSelection();
            weightField.clear();
            rateField.clear();
            emptyWeightState = "";
            loadedWeightState = "";
            
        } catch (NumberFormatException e) {
            showAlert("कृपया वैध संख्या प्रविष्ट करा!");
        }
    }

    private void updateTotals() {
        double totalAmt = billItemsList.stream().mapToDouble(BillItem::getAmount).sum();
        totalAmtLabel.setText(String.format("%.2f", totalAmt));
        calculateFinalBalance();
    }
    
    private void calculateFinalBalance() {
        try {
            double totalAmt = Double.parseDouble(totalAmtLabel.getText());
            double prevBal = previousBalanceField.getText().isEmpty() ? 0 : Double.parseDouble(previousBalanceField.getText());
            double advance = advanceField.getText().isEmpty() ? 0 : Double.parseDouble(advanceField.getText());
            
            // final = total + prevBal - advance
            double finalBal = totalAmt + prevBal - advance;
            finalBalanceLabel.setText(String.format("%.2f", finalBal));
        } catch (NumberFormatException e) {
            // Ignore format errors while typing
        }
    }

    @FXML
    private void handleRemoveItem() {
        BillItem selected = itemTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            billItemsList.remove(selected);
            updateTotals();
        }
    }

    @FXML
    private void handleClearForm() {
        farmerNameField.clear();
        mobileField.clear();
        transportNameState = "";
        vehicleNoState = "";
        emptyWeightState = "";
        loadedWeightState = "";
        photoPathState = "";
        billItemsList.clear();
        previousBalanceField.setText("0.00");
        advanceField.setText("0.00");
        updateTotals();
        refreshBillNo();
    }

    @FXML
    private void handleTransportWeightDetails() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("वजन तपशील (Weight Details)");
        dialog.setHeaderText("वाहतूक, गाडीचे वजन आणि पावती फोटो");

        ButtonType saveButtonType = new ButtonType("Save", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField transportNameDialogField = new TextField(transportNameState);
        transportNameDialogField.setPromptText("ट्रान्सपोर्टचे नाव");

        TextField vehicleNoDialogField = new TextField(vehicleNoState);
        vehicleNoDialogField.setPromptText("गाडी नं");

        TextField emptyWeight = new TextField(emptyWeightState);
        emptyWeight.setPromptText("रिकाम्या गाडीचे वजन");

        TextField loadedWeight = new TextField(loadedWeightState);
        loadedWeight.setPromptText("भरलेल्या गाडीचे वजन");
        
        java.util.function.UnaryOperator<TextFormatter.Change> dialogDoubleFilter = change -> {
            String text = change.getControlNewText();
            if (text.matches("\\d*\\.?\\d*")) {
                return change;
            }
            return null;
        };
        emptyWeight.setTextFormatter(new TextFormatter<>(dialogDoubleFilter));
        loadedWeight.setTextFormatter(new TextFormatter<>(dialogDoubleFilter));

        Button photoButton = new Button("फोटो निवडा (Select Photo)");
        Label photoLabel = new Label(photoPathState.isEmpty() ? "फोटो निवडला नाही" : new File(photoPathState).getName());
        
        final String[] photoPath = new String[]{photoPathState};
        
        Button viewPhotoButton = new Button("फोटो पहा (View Photo)");
        viewPhotoButton.setVisible(!photoPathState.isEmpty());
        viewPhotoButton.setOnAction(ev -> {
            try {
                if (photoPath[0] != null && !photoPath[0].isEmpty()) {
                    java.awt.Desktop.getDesktop().open(new File(photoPath[0]));
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                showAlert("फोटो उघडता आला नाही: " + ex.getMessage());
            }
        });

        photoButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("फोटो निवडा");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
            );
            fileChooser.setInitialDirectory(new File(System.getProperty("user.home"), "Desktop"));
            File selectedFile = fileChooser.showOpenDialog(dialog.getOwner());
            if (selectedFile != null) {
                photoPath[0] = selectedFile.getAbsolutePath();
                photoLabel.setText(selectedFile.getName());
                viewPhotoButton.setVisible(true);
            }
        });

        grid.add(new Label("ट्रान्सपोर्टचे नाव:"), 0, 0);
        grid.add(transportNameDialogField, 1, 0);
        grid.add(new Label("गाडी नं:"), 0, 1);
        grid.add(vehicleNoDialogField, 1, 1);
        grid.add(new Label("रिकाम्या गाडीचे वजन (Empty):"), 0, 2);
        grid.add(emptyWeight, 1, 2);
        grid.add(new Label("भरलेल्या गाडीचे वजन (Loaded):"), 0, 3);
        grid.add(loadedWeight, 1, 3);
        grid.add(new Label("वजन पावती फोटो:"), 0, 4);
        
        javafx.scene.layout.HBox photoBox = new javafx.scene.layout.HBox(10);
        photoBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        photoBox.getChildren().addAll(photoButton, viewPhotoButton, photoLabel);
        
        grid.add(photoBox, 1, 4);
        javafx.scene.layout.GridPane.setColumnSpan(photoBox, 2);

        dialog.getDialogPane().setContent(grid);
        
        javafx.scene.Node saveBtn = dialog.getDialogPane().lookupButton(saveButtonType);
        saveBtn.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            if (transportNameDialogField.getText().trim().isEmpty() ||
                vehicleNoDialogField.getText().trim().isEmpty() ||
                emptyWeight.getText().trim().isEmpty() ||
                loadedWeight.getText().trim().isEmpty() ||
                photoPath[0].trim().isEmpty()) {
                
                event.consume(); // Prevent dialog from closing
                showAlert("कृपया सर्व रकाने भरा आणि फोटो निवडा!");
            }
        });
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                transportNameState = transportNameDialogField.getText();
                vehicleNoState = vehicleNoDialogField.getText();
                emptyWeightState = emptyWeight.getText();
                loadedWeightState = loadedWeight.getText();
                photoPathState = photoPath[0];
                
                try {
                    double emptyW = emptyWeight.getText().isEmpty() ? 0 : Double.parseDouble(emptyWeight.getText());
                    double loadedW = loadedWeight.getText().isEmpty() ? 0 : Double.parseDouble(loadedWeight.getText());
                    double netWeightKg = loadedW - emptyW;
                    
                    if(netWeightKg > 0) {
                        // Assuming input is in KG, convert to Ton
                        double netWeightTon = netWeightKg;
                        if (loadedW >= 100) { // If loaded weight is >= 100, it's very likely in KG
                            netWeightTon = netWeightKg / 1000.0;
                        }
                        
                        weightField.setText(String.format("%.3f", netWeightTon));
                        showAlert("तपशील जतन केले! निव्वळ वजन (Net Weight): " + String.format("%.3f", netWeightTon) + " टन");
                    } else {
                        showAlert("तपशील जतन केले!");
                    }
                } catch(NumberFormatException ex) {
                    showAlert("तपशील जतन केले! पण वजन चुकीचे आहे.");
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    @FXML
    private void handlePrintBill() {
        if (farmerNameField.getText().trim().isEmpty() || mobileField.getText().trim().isEmpty()) {
            showAlert("शेतकऱ्याचे नाव आणि मोबाईल नंबर आवश्यक आहे.");
            return;
        }
        if (billItemsList.isEmpty()) {
            showAlert("बिलामध्ये कोणतेही आयटम नाहीत.");
            return;
        }
        
        try {
            // Check if farmer exists
            String mobile = mobileField.getText().trim();
            Farmer farmer = farmerList.stream()
                .filter(f -> mobile.equals(f.getMobile()))
                .findFirst()
                .orElse(null);
                
            int farmerId;
            if (farmer == null) {
                // Create new farmer
                farmer = new Farmer();
                farmer.setName(farmerNameField.getText().trim());
                farmer.setMobile(mobile);
                farmer.setVillage("");
                farmer.setTaluka("");
                farmer.setDistrict("");
                farmer.setAddress("");
                farmer.setAadharNumber("");
                farmer.setBankDetails("");
                farmer.setOpeningBalance(0);
                farmer.setRemarks("");
                farmer.setStatus("ACTIVE");
                
                farmerId = farmerService.addFarmer(farmer);
            } else {
                farmerId = farmer.getId();
            }
            
            // Save purchases
            double totalNet = 0;
            StringBuilder billText = new StringBuilder();
            billText.append("शेतकरी: ").append(farmerNameField.getText()).append("\n");
            billText.append("तारीख: ").append(datePicker.getValue().toString()).append("\n\n");
            
            for (BillItem item : billItemsList) {
                Purchase p = new Purchase();
                p.setFarmerId(farmerId);
                p.setBillNo(billNoField.getText());
                p.setPurchaseDate(datePicker.getValue());
                p.setCaneType(item.getCaneType());
                p.setVehicleNo(vehicleNoState);
                p.setEmptyWeight(item.getEmptyWeight());
                p.setLoadedWeight(item.getLoadedWeight());
                p.setWeight(item.getWeight());
                p.setRatePerTon(item.getRate());
                p.setTotalAmount(item.getAmount());
                p.setAdvance(0);
                p.setLoadingCharges(0);
                p.setCuttingCharges(0);
                p.setTransportCharges(0);
                p.setOtherCharges(0);
                p.setNetAmount(item.getAmount());
                p.setRemarks("");
                
                purchaseDAO.addPurchase(p);
                totalNet += item.getAmount();
                
                billText.append(item.getCaneType()).append(" | वजन: ").append(item.getWeight())
                        .append(" | दर: ").append(item.getRate()).append(" | रक्कम: ").append(item.getAmount()).append("\n");
            }
            
            billText.append("\nएकूण रक्कम: ").append(totalNet);
            
            // Generate PDF
            try {
                java.io.File pdfFile = com.sugarcane.erp.utils.PdfBillExporter.generateBillPdf(farmer, billNoField.getText(), datePicker.getValue(), billItemsList, totalNet);
                
                // Generate Ledger PDF
                com.sugarcane.erp.service.LedgerService ledgerService = new com.sugarcane.erp.service.LedgerService();
                java.time.LocalDate startDate = java.time.LocalDate.of(2000, 1, 1);
                java.time.LocalDate endDate = java.time.LocalDate.now();
                java.util.List<com.sugarcane.erp.model.LedgerEntry> ledgerEntries = ledgerService.getFarmerLedger(farmer, startDate, endDate);
                String ledgerPdfPath = com.sugarcane.erp.utils.PdfLedgerExporter.generateLedgerPdf(farmer, startDate, endDate, ledgerEntries, null);
                
                // Open the Bill PDF automatically in a new thread
                new Thread(() -> {
                    try {
                        java.awt.Desktop.getDesktop().open(pdfFile);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        javafx.application.Platform.runLater(() -> 
                            showAlert("PDF उघडताना त्रुटी आली. फाईल येथे सेव्ह केली आहे: " + pdfFile.getAbsolutePath())
                        );
                    }
                }).start();
                
                // Add WhatsApp Confirmation
                final double finalNet = totalNet;
                final Farmer finalFarmer = farmer;
                javafx.application.Platform.runLater(() -> {
                    javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION);
                    alert.setTitle("WhatsApp Message");
                    alert.setHeaderText(null);
                    alert.setContentText("शेतकऱ्याला WhatsApp वर मेसेज पाठवायचा आहे का?\n(WhatsApp Web मध्ये PDF फाईल्स आपोआप जोडल्या जात नाहीत. यासाठी तुमची फाईल्स असणारा फोल्डर ओपन होईल, तिथून तुम्ही PDF ड्रॅग करून WhatsApp मध्ये टाकू शकता.)");
                    java.util.Optional<javafx.scene.control.ButtonType> result = alert.showAndWait();
                    if (result.isPresent() && result.get() == javafx.scene.control.ButtonType.OK) {
                        try {
                            String farmerMobile = finalFarmer.getMobile();
                            if (farmerMobile != null && farmerMobile.trim().length() >= 10) {
                                farmerMobile = farmerMobile.trim();
                                if (!farmerMobile.startsWith("91") && !farmerMobile.startsWith("+91")) {
                                    farmerMobile = "91" + farmerMobile;
                                } else if (farmerMobile.startsWith("+91")) {
                                    farmerMobile = farmerMobile.substring(1);
                                }
                                String msg = "नमस्कार " + finalFarmer.getName() + ", तुमचे आजचे बिल तयार झाले आहे. एकूण रक्कम: ₹" + finalNet + ". सोबत बिल आणि खतावणी जोडली आहे. श्री गणेश कृपा ऊस सप्लायर्स.";
                                String encodedMsg = java.net.URLEncoder.encode(msg, "UTF-8");
                                String url = "https://wa.me/" + farmerMobile + "?text=" + encodedMsg;
                                java.awt.Desktop.getDesktop().browse(new java.net.URI(url));
                                
                                // Open folder
                                java.awt.Desktop.getDesktop().open(pdfFile.getParentFile());
                            } else {
                                showAlert("शेतकऱ्याचा मोबाईल नंबर बरोबर नाही: " + farmerMobile);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            showAlert("WhatsApp उघडताना त्रुटी आली: " + e.getMessage());
                        }
                    }
                });
            } catch (Exception ex) {
                ex.printStackTrace();
                showAlert("PDF बनवताना त्रुटी आली: " + ex.getMessage());
            }
            
            // Reset UI
            billItemsList.clear();
            calculateFinalBalance();
            loadFarmers(); // Refresh history
            refreshBillNo(); // Generate next bill number
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("बिल सेव्ह करताना त्रुटी आली: " + e.getMessage());
        }
    }

    @FXML
    private void handleWhatsApp() {
        if (farmerNameField.getText().trim().isEmpty() || mobileField.getText().trim().isEmpty()) {
            showAlert("शेतकऱ्याचे नाव आणि मोबाईल नंबर आवश्यक आहे.");
            return;
        }
        String mobile = mobileField.getText().trim();
        if (mobile.length() != 10) {
            showAlert("मोबाईल नंबर १० अंकी असावा.");
            return;
        }

        try {
            // If there are unsaved items, print/save the bill first!
            if (!billItemsList.isEmpty()) {
                handlePrintBill();
            }
            
            // Now the bill is saved and the image is on the clipboard.
            String msg = "नमस्कार " + farmerNameField.getText().trim() + ",\n\n" +
                         "तुमची उसाची पावती खालीलप्रमाणे.\n" +
                         "(टीप: कृपया 'Paste' (Ctrl+V) करून पावतीचा फोटो पाठवा)";
            
            String encodedMsg = java.net.URLEncoder.encode(msg, java.nio.charset.StandardCharsets.UTF_8.toString());
            String url = "https://wa.me/91" + mobile + "?text=" + encodedMsg;
            
            java.awt.Desktop.getDesktop().browse(new java.net.URI(url));
            
            showAlert("बिलाचा फोटो कॉपी झाला आहे! WhatsApp उघडल्यानंतर 'Paste (Ctrl+V)' करून फोटो पाठवा.");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("WhatsApp उघडताना त्रुटी आली: " + e.getMessage());
        }
    }

    private void showDateSelectionDialog(Farmer farmer, boolean isExcel) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("तारीख निवडा (Select Date)");
        dialog.setHeaderText(farmer.getName() + " चे लेजर काढण्यासाठी तारखेची श्रेणी निवडा");

        ButtonType downloadButtonType = new ButtonType("डाऊनलोड (Download)", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(downloadButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

        DatePicker fromDate = new DatePicker();
        fromDate.setPromptText("सुरुवात (From)");
        DatePicker toDate = new DatePicker();
        toDate.setPromptText("शेवट (End)");
        
        java.time.format.DateTimeFormatter dateFormatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
        javafx.util.StringConverter<java.time.LocalDate> dateConverter = new javafx.util.StringConverter<java.time.LocalDate>() {
            @Override public String toString(java.time.LocalDate date) { return date != null ? dateFormatter.format(date) : ""; }
            @Override public java.time.LocalDate fromString(String string) {
                try { return (string != null && !string.isEmpty()) ? java.time.LocalDate.parse(string, dateFormatter) : null; }
                catch (Exception e) { return null; }
            }
        };
        fromDate.setConverter(dateConverter);
        toDate.setConverter(dateConverter);
        toDate.setValue(java.time.LocalDate.now());

        grid.add(new Label("सुरुवात तारीख (Start Date):"), 0, 0);
        grid.add(fromDate, 1, 0);
        grid.add(new Label("शेवट तारीख (End Date):"), 0, 1);
        grid.add(toDate, 1, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == downloadButtonType) {
                java.time.LocalDate start = fromDate.getValue();
                java.time.LocalDate end = toDate.getValue();
                
                if (isExcel) {
                    try {
                        com.sugarcane.erp.service.LedgerService ledgerService = new com.sugarcane.erp.service.LedgerService();
                        java.util.List<com.sugarcane.erp.model.LedgerEntry> entries = ledgerService.getFarmerLedger(farmer, start, end);
                        
                        TableView<com.sugarcane.erp.model.LedgerEntry> dummyTable = new TableView<>();
                        
                        TableColumn<com.sugarcane.erp.model.LedgerEntry, String> colDate = new TableColumn<>("तारीख");
                        colDate.setCellValueFactory(cellData -> {
                            if (cellData.getValue().getDate().getYear() < 2000) return new SimpleStringProperty("");
                            return new SimpleStringProperty(dateFormatter.format(cellData.getValue().getDate()));
                        });
                        
                        TableColumn<com.sugarcane.erp.model.LedgerEntry, String> colBillNo = new TableColumn<>("बिल नं.");
                        colBillNo.setCellValueFactory(new PropertyValueFactory<>("billNo"));
                        
                        TableColumn<com.sugarcane.erp.model.LedgerEntry, String> colPart = new TableColumn<>("तपशील");
                        colPart.setCellValueFactory(new PropertyValueFactory<>("particulars"));
                        
                        TableColumn<com.sugarcane.erp.model.LedgerEntry, String> colCane = new TableColumn<>("उसाचा प्रकार");
                        colCane.setCellValueFactory(new PropertyValueFactory<>("caneType"));
                        
                        TableColumn<com.sugarcane.erp.model.LedgerEntry, String> colEmptyW = new TableColumn<>("खाली गाडी");
                        colEmptyW.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getEmptyWeight() > 0 ? String.format("%.0f", cellData.getValue().getEmptyWeight()) : "-"));
                        
                        TableColumn<com.sugarcane.erp.model.LedgerEntry, String> colLoadedW = new TableColumn<>("भरलेली गाडी");
                        colLoadedW.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getLoadedWeight() > 0 ? String.format("%.0f", cellData.getValue().getLoadedWeight()) : "-"));
                        
                        TableColumn<com.sugarcane.erp.model.LedgerEntry, String> colWeight = new TableColumn<>("उसाचे वजन (टन)");
                        colWeight.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getWeight() > 0 ? String.format("%.3f", cellData.getValue().getWeight()) : ""));
                        
                        TableColumn<com.sugarcane.erp.model.LedgerEntry, String> colDeb = new TableColumn<>("रक्कम (रु)");
                        colDeb.setCellValueFactory(cellData -> new SimpleStringProperty(String.format("%.2f", cellData.getValue().getDebit())));
                        
                        TableColumn<com.sugarcane.erp.model.LedgerEntry, String> colCred = new TableColumn<>("जमा (रु)");
                        colCred.setCellValueFactory(cellData -> new SimpleStringProperty(String.format("%.2f", cellData.getValue().getCredit())));
                        
                        TableColumn<com.sugarcane.erp.model.LedgerEntry, String> colBal = new TableColumn<>("बाकी (रु)");
                        colBal.setCellValueFactory(cellData -> new SimpleStringProperty(String.format("%.2f", cellData.getValue().getBalance())));
                        
                        dummyTable.getColumns().addAll(colDate, colBillNo, colPart, colCane, colEmptyW, colLoadedW, colWeight, colDeb, colCred, colBal);
                        dummyTable.getItems().setAll(entries);
                        
                        String name = "Ledger_Farmer_" + farmer.getName().replace(" ", "_");
                        com.sugarcane.erp.utils.ExcelExporter.exportTableToExcel(dummyTable, name, farmerHistoryTable.getScene().getWindow());
                    } catch (Exception e) {
                        e.printStackTrace();
                        showAlert("Excel तयार करताना त्रुटी: " + e.getMessage());
                    }
                } else {
                    // It's Khatawani (PDF)
                    try {
                        com.sugarcane.erp.service.LedgerService ls = new com.sugarcane.erp.service.LedgerService();
                        java.util.List<com.sugarcane.erp.model.LedgerEntry> entries = ls.getFarmerLedger(farmer, start, end);
                        
                        String pdfPath = com.sugarcane.erp.utils.PdfLedgerExporter.generateLedgerPdf(farmer, start, end, entries, farmerHistoryTable.getScene().getWindow());
                        
                        if (pdfPath != null) {
                            try {
                                java.awt.Desktop.getDesktop().open(new java.io.File(pdfPath));
                            } catch (Exception ex) {
                                ex.printStackTrace();
                                showAlert("PDF तयार झाली, पण उघडता आली नाही: " + pdfPath);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        showAlert("खतावणी PDF तयार करताना त्रुटी: " + e.getMessage());
                    }
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    @FXML
    private void handleExportExcel() {
        Farmer selectedFarmer = farmerHistoryTable.getSelectionModel().getSelectedItem();
        if (selectedFarmer != null) {
            showDateSelectionDialog(selectedFarmer, true);
        } else {
            if (farmerHistoryTable.getItems().isEmpty()) {
                showAlert("No data to export.");
                return;
            }
            com.sugarcane.erp.utils.ExcelExporter.exportTableToExcel(farmerHistoryTable, "Farmer_History", farmerHistoryTable.getScene().getWindow());
        }
    }

    @FXML
    private void handleOpenLedger() {
        Farmer selectedFarmer = farmerHistoryTable.getSelectionModel().getSelectedItem();
        if (selectedFarmer != null) {
            showDateSelectionDialog(selectedFarmer, false);
        } else {
            try {
                javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/view/ledger.fxml"));
                loader.setResources(java.util.ResourceBundle.getBundle("i18n.messages", com.sugarcane.erp.utils.LanguageManager.getLocale()));
                javafx.scene.Parent root = loader.load();
                javafx.stage.Stage stage = new javafx.stage.Stage();
                stage.setTitle("खतावणी");
                stage.setScene(new javafx.scene.Scene(root, 1000, 700));
                stage.show();
            } catch (Exception e) {
                e.printStackTrace();
                showAlert("खतावणी उघडण्यात त्रुटी: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handlePayment() {
        Farmer selectedFarmer = farmerHistoryTable.getSelectionModel().getSelectedItem();
        if (selectedFarmer == null) {
            showAlert("कृपया पेमेंट करण्यासाठी शेतकरी निवडा.");
            return;
        }

        double currentBalance = 0.0;
        try {
            com.sugarcane.erp.service.LedgerService ls = new com.sugarcane.erp.service.LedgerService();
            java.util.List<com.sugarcane.erp.model.LedgerEntry> entries = ls.getFarmerLedger(selectedFarmer);
            if (!entries.isEmpty()) {
                currentBalance = entries.get(entries.size() - 1).getBalance();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Dialog<com.sugarcane.erp.model.FarmerPayment> dialog = new Dialog<>();
        dialog.setTitle("पेमेंट करा");
        dialog.setHeaderText(selectedFarmer.getName() + " ची बाकी: ₹" + String.format("%.2f", currentBalance));

        ButtonType saveButtonType = new ButtonType("पेमेंट करा (Pay)", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField amountField = new TextField(String.format("%.2f", currentBalance));
        ComboBox<String> modeCombo = new ComboBox<>(FXCollections.observableArrayList("Google Pay", "Phone Pay", "Net Banking", "Cash", "Cheque"));
        modeCombo.getSelectionModel().select("Cash");

        grid.add(new Label("दिलेली रक्कम (Amount):"), 0, 0);
        grid.add(amountField, 1, 0);
        grid.add(new Label("कसे दिले (Payment Mode):"), 0, 1);
        grid.add(modeCombo, 1, 1);

        dialog.getDialogPane().setContent(grid);
        
        javafx.application.Platform.runLater(() -> {
            amountField.requestFocus();
            amountField.selectAll();
        });

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    double amt = Double.parseDouble(amountField.getText());
                    return new com.sugarcane.erp.model.FarmerPayment(selectedFarmer.getId(), java.time.LocalDate.now(), amt, modeCombo.getValue(), "");
                } catch (NumberFormatException e) {
                    showAlert("कृपया योग्य रक्कम टाका.");
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(payment -> {
            try {
                paymentDAO.addPayment(payment);
                loadFarmers();
                showAlert("पेमेंट यशस्वीरित्या जमा झाले.");
            } catch (Exception e) {
                e.printStackTrace();
                showAlert("पेमेंट सेव्ह करताना त्रुटी: " + e.getMessage());
            }
        });
    }

    @FXML
    private void handleCloseAccount() {
        Farmer selectedFarmer = farmerHistoryTable.getSelectionModel().getSelectedItem();
        if (selectedFarmer == null) {
            showAlert("कृपया हिशोब क्लोज करण्यासाठी शेतकरी निवडा.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("हिशोब क्लोज करा");
        confirm.setHeaderText("तुम्हाला खात्री आहे का की " + selectedFarmer.getName() + " यांचा हिशोब क्लोज करायचा आहे?");
        confirm.setContentText("यामुळे त्यांची बाकी ०.०० होईल.");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    selectedFarmer.setOpeningBalance(0);
                    farmerService.updateFarmer(selectedFarmer);
                    loadFarmers();
                    showAlert("हिशोब क्लोज झाला.");
                } catch (Exception e) {
                    e.printStackTrace();
                    showAlert("त्रुटी: " + e.getMessage());
                }
            }
        });
    }

    @FXML
    private void handleDeleteRecord() {
        Farmer selectedFarmer = farmerHistoryTable.getSelectionModel().getSelectedItem();
        if (selectedFarmer == null) {
            showAlert("कृपया डिलीट करण्यासाठी शेतकरी निवडा.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("नोंद काढा");
        confirm.setHeaderText("तुम्हाला खात्री आहे का की " + selectedFarmer.getName() + " यांना डिलीट करायचे आहे?");
        confirm.setContentText("हा बदल कायमस्वरूपी असेल.");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    farmerService.deleteFarmer(selectedFarmer.getId());
                    loadFarmers();
                    showAlert("नोंद डिलीट झाली.");
                } catch (Exception e) {
                    e.printStackTrace();
                    showAlert("शेतकरी डिलीट करताना त्रुटी (त्यांचे जुने बिल असू शकते): " + e.getMessage());
                }
            }
        });
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}

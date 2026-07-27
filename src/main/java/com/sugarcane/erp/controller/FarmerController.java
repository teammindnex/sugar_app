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

    private FarmerService farmerService;
    private PurchaseDAO purchaseDAO = new PurchaseDAO();
    private FarmerPaymentDAO paymentDAO = new FarmerPaymentDAO();
    private ObservableList<BillItem> billItemsList = FXCollections.observableArrayList();
    private ObservableList<Farmer> farmerList = FXCollections.observableArrayList();

    public static class BillItem {
        private String caneType;
        private double weight;
        private double rate;
        private double amount;

        public BillItem(String caneType, double weight, double rate, double amount) {
            this.caneType = caneType;
            this.weight = weight;
            this.rate = rate;
            this.amount = amount;
        }

        public String getCaneType() { return caneType; }
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
        colHistCaneType.setCellValueFactory(cellData -> new SimpleStringProperty("-"));
        colHistTotalWeight.setCellValueFactory(cellData -> new SimpleStringProperty("0.00"));
        colHistDate.setCellValueFactory(cellData -> new SimpleStringProperty(LocalDate.now().toString()));
        
        // Attach Marathi Transliterator
        com.sugarcane.erp.utils.MarathiTransliterator.attach(farmerNameField);
        
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
        
        // Restrict farmerNameField to no numbers
        farmerNameField.setTextFormatter(new TextFormatter<>(change -> {
            String text = change.getControlNewText();
            if (text.matches("[^\\d]*")) {
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
    }

    @FXML
    private void loadFarmers() {
        try {
            farmerList.clear();
            farmerList.addAll(farmerService.getAllFarmers());
            farmerHistoryTable.setItems(farmerList);
        } catch (SQLException e) {
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
            double amount = weight * rate; // Example calculation (Total weight * Rate per Ton)
            
            BillItem item = new BillItem(caneType, weight, rate, amount);
            billItemsList.add(item);
            
            updateTotals();
            
            // Clear fields
            caneTypeCombo.getSelectionModel().clearSelection();
            weightField.clear();
            rateField.clear();
            
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
        grid.add(photoButton, 1, 4);
        grid.add(photoLabel, 2, 4);

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
                    double netWeight = loadedW - emptyW;
                    
                    if(netWeight > 0) {
                        weightField.setText(String.valueOf(netWeight));
                        showAlert("तपशील जतन केले! निव्वळ वजन (Net Weight): " + netWeight + " टन");
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
                p.setPurchaseDate(datePicker.getValue());
                p.setCaneType(item.getCaneType());
                p.setVehicleNo(vehicleNoState);
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
            
            // Reset UI
            billItemsList.clear();
            calculateFinalBalance();
            loadFarmers(); // Refresh history
            
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("बिल प्रिंट");
            alert.setHeaderText("बिल यशस्वीरित्या सेव्ह झाले.");
            alert.setContentText(billText.toString());
            alert.showAndWait();
            
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
            StringBuilder msg = new StringBuilder();
            msg.append("नमस्कार ").append(farmerNameField.getText().trim()).append(",\n\n");
            msg.append("तुमची उसाची पावती खालीलप्रमाणे:\n");
            msg.append("तारीख: ").append(datePicker.getValue().toString()).append("\n\n");
            
            double total = 0;
            for (BillItem item : billItemsList) {
                msg.append(item.getCaneType()).append(" | वजन: ").append(item.getWeight())
                   .append(" | रक्कम: ").append(item.getAmount()).append("\n");
                total += item.getAmount();
            }
            msg.append("\nएकूण रक्कम: Rs ").append(total).append("\n\nधन्यवाद!");
            
            String encodedMsg = URLEncoder.encode(msg.toString(), StandardCharsets.UTF_8.toString());
            String url = "https://wa.me/91" + mobile + "?text=" + encodedMsg;
            
            Desktop.getDesktop().browse(new URI(url));
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("WhatsApp उघडताना त्रुटी आली: " + e.getMessage());
        }
    }

    @FXML
    private void handleExportExcel() {
        if (farmerHistoryTable.getItems().isEmpty()) {
            showAlert("No data to export.");
            return;
        }
        com.sugarcane.erp.utils.ExcelExporter.exportTableToExcel(farmerHistoryTable, "Farmer_History", farmerHistoryTable.getScene().getWindow());
    }

    @FXML
    private void handleOpenLedger() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/view/ledger.fxml"));
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

    @FXML
    private void handlePayment() {
        Farmer selectedFarmer = farmerHistoryTable.getSelectionModel().getSelectedItem();
        if (selectedFarmer == null) {
            showAlert("कृपया पेमेंट करण्यासाठी शेतकरी निवडा.");
            return;
        }

        Dialog<com.sugarcane.erp.model.FarmerPayment> dialog = new Dialog<>();
        dialog.setTitle("पेमेंट जमा करा");
        dialog.setHeaderText(selectedFarmer.getName() + " यांचे पेमेंट");

        ButtonType saveButtonType = new ButtonType("सेव्ह करा", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

        TextField amountField = new TextField();
        amountField.setPromptText("रक्कम");
        TextField modeField = new TextField("Cash");
        modeField.setPromptText("पेमेंट मोड (Cash/Online)");
        TextField refField = new TextField();
        refField.setPromptText("संदर्भ क्र.");

        grid.add(new Label("रक्कम:"), 0, 0);
        grid.add(amountField, 1, 0);
        grid.add(new Label("मोड:"), 0, 1);
        grid.add(modeField, 1, 1);
        grid.add(new Label("संदर्भ क्र.:"), 0, 2);
        grid.add(refField, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    double amt = Double.parseDouble(amountField.getText());
                    return new com.sugarcane.erp.model.FarmerPayment(selectedFarmer.getId(), java.time.LocalDate.now(), amt, modeField.getText(), refField.getText());
                } catch (NumberFormatException e) {
                    showAlert("कृपया योग्य रक्कम टाका.");
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(payment -> {
            try {
                paymentDAO.addPayment(payment);
                selectedFarmer.setOpeningBalance(selectedFarmer.getOpeningBalance() - payment.getAmount());
                farmerService.updateFarmer(selectedFarmer);
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

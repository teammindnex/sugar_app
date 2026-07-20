package com.sugarcane.erp.controller;

import com.sugarcane.erp.model.Farmer;
import com.sugarcane.erp.service.FarmerService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.SQLException;
import java.time.LocalDate;

public class FarmerController {

    // --- New Bill Tab Fields ---
    @FXML private TextField farmerNameField;
    @FXML private TextField mobileField;
    @FXML private DatePicker datePicker;
    @FXML private TextField billNoField;
    @FXML private TextField transportField;
    @FXML private TextField vehicleNoField;
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
    @FXML private TableColumn<Farmer, String> colHistTotalSales;
    @FXML private TableColumn<Farmer, String> colHistPayable;
    @FXML private TableColumn<Farmer, String> colHistDate;
    @FXML private TableColumn<Farmer, String> colHistItems;

    private FarmerService farmerService;
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
        colHistTotalSales.setCellValueFactory(cellData -> new SimpleStringProperty("0.00"));
        colHistPayable.setCellValueFactory(cellData -> new SimpleStringProperty(String.format("%.2f", cellData.getValue().getOpeningBalance())));
        colHistDate.setCellValueFactory(cellData -> new SimpleStringProperty(LocalDate.now().toString()));
        colHistItems.setCellValueFactory(cellData -> new SimpleStringProperty("-"));
        
        // Attach Marathi Transliterator
        com.sugarcane.erp.utils.MarathiTransliterator.attach(farmerNameField);
        
        // Add listeners for total calculation
        previousBalanceField.textProperty().addListener((obs, oldV, newV) -> calculateFinalBalance());
        advanceField.textProperty().addListener((obs, oldV, newV) -> calculateFinalBalance());

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
        if (caneTypeCombo.getValue() == null || weightField.getText().isEmpty() || rateField.getText().isEmpty()) {
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
        transportField.clear();
        vehicleNoField.clear();
        billItemsList.clear();
        previousBalanceField.setText("0.00");
        advanceField.setText("0.00");
        updateTotals();
    }

    @FXML
    private void handlePrintBill() {
        showAlert("Printing logic will be implemented here.");
    }

    @FXML
    private void handleWhatsApp() {
        showAlert("WhatsApp sending logic will be implemented here.");
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
            stage.setTitle("Khatavani");
            stage.setScene(new javafx.scene.Scene(root, 1000, 700));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Failed to open Khatavani: " + e.getMessage());
        }
    }

    @FXML
    private void handlePayment() {
        showAlert("Payment window will open here.");
    }

    @FXML
    private void handleCloseAccount() {
        showAlert("Account closed successfully.");
    }

    @FXML
    private void handleDeleteRecord() {
        Farmer selected = farmerHistoryTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            farmerList.remove(selected);
            showAlert("Record deleted successfully.");
        } else {
            showAlert("Please select a record to delete.");
        }
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}

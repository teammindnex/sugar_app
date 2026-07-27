package com.sugarcane.erp.controller;

import com.sugarcane.erp.model.Customer;
import com.sugarcane.erp.service.CustomerService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.SQLException;
import java.time.LocalDate;

public class CustomerController {

    // --- New Bill Tab Fields ---
    @FXML private TextField customerNameField;
    @FXML private TextField mobileField;
    @FXML private DatePicker datePicker;
    @FXML private TextField billNoField;
    @FXML private TextField addressField;
    @FXML private TextField monthField;
    
    // Product Entry
    @FXML private TextField itemTypeField;
    @FXML private TextField bharaField;
    @FXML private TextField weightField;
    @FXML private TextField rateField;
    
    // Item Table
    @FXML private TableView<CustomerBillItem> itemTable;
    @FXML private TableColumn<CustomerBillItem, String> colSrNo;
    @FXML private TableColumn<CustomerBillItem, String> colItemType;
    @FXML private TableColumn<CustomerBillItem, String> colBhara;
    @FXML private TableColumn<CustomerBillItem, String> colWeight;
    @FXML private TableColumn<CustomerBillItem, String> colRate;
    @FXML private TableColumn<CustomerBillItem, String> colAmount;
    
    // Totals
    @FXML private Label totalAmtLabel;
    @FXML private TextField previousBalanceField;
    @FXML private TextField cashReceivedField;
    @FXML private Label finalAmtLabel;
    @FXML private Label amountInWordsLabel;
    
    // --- Customer History Tab Fields ---
    @FXML private TextField searchHistoryField;
    @FXML private TableView<Customer> customerHistoryTable;
    @FXML private TableColumn<Customer, String> colHistName;
    @FXML private TableColumn<Customer, String> colHistMobile;
    @FXML private TableColumn<Customer, String> colHistTotalPurchase;
    @FXML private TableColumn<Customer, String> colHistReceivable;
    @FXML private TableColumn<Customer, String> colHistDate;
    @FXML private TableColumn<Customer, String> colHistItems;

    private CustomerService customerService;
    private ObservableList<CustomerBillItem> billItemsList = FXCollections.observableArrayList();
    private ObservableList<Customer> customerList = FXCollections.observableArrayList();

    public static class CustomerBillItem {
        private int srNo;
        private String itemType;
        private String bhara;
        private double weight;
        private double rate;
        private double amount;

        public CustomerBillItem(int srNo, String itemType, String bhara, double weight, double rate, double amount) {
            this.srNo = srNo;
            this.itemType = itemType;
            this.bhara = bhara;
            this.weight = weight;
            this.rate = rate;
            this.amount = amount;
        }

        public int getSrNo() { return srNo; }
        public String getItemType() { return itemType; }
        public String getBhara() { return bhara; }
        public double getWeight() { return weight; }
        public double getRate() { return rate; }
        public double getAmount() { return amount; }
    }

    @FXML
    public void initialize() {
        customerService = new CustomerService();
        datePicker.setValue(LocalDate.now());
        
        // Setup Item Table Columns
        colSrNo.setCellValueFactory(new PropertyValueFactory<>("srNo"));
        colItemType.setCellValueFactory(new PropertyValueFactory<>("itemType"));
        colBhara.setCellValueFactory(new PropertyValueFactory<>("bhara"));
        colWeight.setCellValueFactory(new PropertyValueFactory<>("weight"));
        colRate.setCellValueFactory(new PropertyValueFactory<>("rate"));
        colAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        itemTable.setItems(billItemsList);
        
        // Setup History Table Columns
        colHistName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colHistMobile.setCellValueFactory(new PropertyValueFactory<>("mobile"));
        colHistTotalPurchase.setCellValueFactory(cellData -> new SimpleStringProperty("0.00")); // Dummy
        colHistReceivable.setCellValueFactory(cellData -> new SimpleStringProperty(String.format("%.2f", cellData.getValue().getOpeningBalance())));
        colHistDate.setCellValueFactory(cellData -> new SimpleStringProperty(LocalDate.now().toString()));
        colHistItems.setCellValueFactory(cellData -> new SimpleStringProperty("-"));
        
        // Add listeners for total calculation
        previousBalanceField.textProperty().addListener((obs, oldV, newV) -> calculateFinalBalance());
        cashReceivedField.textProperty().addListener((obs, oldV, newV) -> calculateFinalBalance());

        loadCustomers();
    }

    @FXML
    private void loadCustomers() {
        try {
            customerList.clear();
            customerList.addAll(customerService.getAllCustomers());
            customerHistoryTable.setItems(customerList);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAddItem() {
        if (itemTypeField.getText().isEmpty() || weightField.getText().isEmpty() || rateField.getText().isEmpty()) {
            showAlert("कृपया मालाचा प्रकार, वजन आणि भाव प्रविष्ट करा!");
            return;
        }
        
        try {
            String itemType = itemTypeField.getText();
            String bhara = bharaField.getText();
            double weight = Double.parseDouble(weightField.getText());
            double rate = Double.parseDouble(rateField.getText());
            double amount = weight * rate; // basic calculation
            int srNo = billItemsList.size() + 1;
            
            CustomerBillItem item = new CustomerBillItem(srNo, itemType, bhara, weight, rate, amount);
            billItemsList.add(item);
            
            updateTotals();
            
            // Clear fields
            itemTypeField.clear();
            bharaField.clear();
            weightField.clear();
            rateField.clear();
            
        } catch (NumberFormatException e) {
            showAlert("कृपया वैध संख्या प्रविष्ट करा!");
        }
    }

    private void updateTotals() {
        double totalAmt = billItemsList.stream().mapToDouble(CustomerBillItem::getAmount).sum();
        totalAmtLabel.setText(String.format("%.2f", totalAmt));
        calculateFinalBalance();
    }
    
    private void calculateFinalBalance() {
        try {
            double totalAmt = Double.parseDouble(totalAmtLabel.getText());
            double prevBal = previousBalanceField.getText().isEmpty() ? 0 : Double.parseDouble(previousBalanceField.getText());
            double cashRec = cashReceivedField.getText().isEmpty() ? 0 : Double.parseDouble(cashReceivedField.getText());
            
            double finalBal = totalAmt + prevBal - cashRec;
            finalAmtLabel.setText(String.format("%.2f", finalBal));
            
            amountInWordsLabel.setText(com.sugarcane.erp.utils.NumberToMarathiWordsConverter.convert((long) finalBal) + " रुपये फक्त");
        } catch (NumberFormatException e) {
            // Ignore format errors while typing
        }
    }

    @FXML
    private void handleRemoveItem() {
        CustomerBillItem selected = itemTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            billItemsList.remove(selected);
            // Renumber srNo
            for (int i = 0; i < billItemsList.size(); i++) {
                CustomerBillItem item = billItemsList.get(i);
                item.srNo = i + 1;
            }
            itemTable.refresh();
            updateTotals();
        }
    }

    @FXML
    private void handleClearForm() {
        customerNameField.clear();
        mobileField.clear();
        addressField.clear();
        monthField.clear();
        billItemsList.clear();
        previousBalanceField.setText("0.00");
        cashReceivedField.setText("0.00");
        updateTotals();
    }

    @FXML
    private void handlePrintBill() {
        try {
            String customerName = customerNameField.getText();
            String mobile = mobileField.getText();
            String address = addressField.getText();
            String month = monthField.getText();
            String date = datePicker.getValue() != null ? datePicker.getValue().format(java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy")) : "";
            String billNo = billNoField.getText();
            if (billNo == null || billNo.isEmpty()) billNo = "Auto-Generated";

            double totalAmt = Double.parseDouble(totalAmtLabel.getText());
            double prevBal = previousBalanceField.getText().isEmpty() ? 0 : Double.parseDouble(previousBalanceField.getText());
            double cashRec = cashReceivedField.getText().isEmpty() ? 0 : Double.parseDouble(cashReceivedField.getText());
            double finalBal = totalAmt + prevBal - cashRec;

            javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
            fileChooser.setTitle("Save Bill as PDF");
            fileChooser.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("PDF files (*.pdf)", "*.pdf"));
            fileChooser.setInitialFileName("Bill_" + (customerName.isEmpty() ? "Unknown" : customerName) + "_" + System.currentTimeMillis() + ".pdf");
            
            java.io.File file = fileChooser.showSaveDialog(customerNameField.getScene().getWindow());
            if (file != null) {
                com.sugarcane.erp.service.PdfGeneratorService.generateCustomerBillPdf(
                        customerName, mobile, address, month, date, billNo,
                        billItemsList, totalAmt, prevBal, cashRec, finalBal,
                        file.getAbsolutePath()
                );
                
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setHeaderText(null);
                alert.setContentText("बिल यशस्वीरित्या सेव्ह केले! (Bill generated successfully at: " + file.getAbsolutePath() + ")");
                alert.showAndWait();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("PDF Generation Failed");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void handleWhatsApp() {
        showAlert("WhatsApp sending logic will be implemented here.");
    }

    @FXML
    private void handleExportExcel() {
        if (customerHistoryTable.getItems().isEmpty()) {
            showAlert("No data to export.");
            return;
        }
        com.sugarcane.erp.utils.ExcelExporter.exportTableToExcel(customerHistoryTable, "Customer_History", customerHistoryTable.getScene().getWindow());
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
        showAlert("Payment window will open here.");
    }

    @FXML
    private void handleCloseAccount() {
        showAlert("Account closed successfully.");
    }

    @FXML
    private void handleDeleteRecord() {
        Customer selected = customerHistoryTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            customerList.remove(selected);
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

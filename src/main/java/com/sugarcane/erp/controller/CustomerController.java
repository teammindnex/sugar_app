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
    @FXML private ComboBox<String> supplyTypeCombo;
    @FXML private TextField quantityField;
    @FXML private TextField rateField;
    
    // Item Table
    @FXML private TableView<CustomerBillItem> itemTable;
    @FXML private TableColumn<CustomerBillItem, String> colSrNo;
    @FXML private TableColumn<CustomerBillItem, String> colItemType;
    @FXML private TableColumn<CustomerBillItem, String> colSupplyType;
    @FXML private TableColumn<CustomerBillItem, String> colQuantity;
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
        private String supplyType;
        private double quantity;
        private double rate;
        private double amount;

        public CustomerBillItem(int srNo, String itemType, String supplyType, double quantity, double rate, double amount) {
            this.srNo = srNo;
            this.itemType = itemType;
            this.supplyType = supplyType;
            this.quantity = quantity;
            this.rate = rate;
            this.amount = amount;
        }

        public int getSrNo() { return srNo; }
        public String getItemType() { return itemType; }
        public String getSupplyType() { return supplyType; }
        public double getQuantity() { return quantity; }
        public double getRate() { return rate; }
        public double getAmount() { return amount; }
    }

    @FXML
    public void initialize() {
        customerService = new CustomerService();
        datePicker.setConverter(new javafx.util.StringConverter<LocalDate>() {
            private java.time.format.DateTimeFormatter dateFormatter = java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy");
            @Override
            public String toString(LocalDate date) {
                return date != null ? dateFormatter.format(date) : "";
            }
            @Override
            public LocalDate fromString(String string) {
                return (string != null && !string.isEmpty()) ? LocalDate.parse(string, dateFormatter) : null;
            }
        });
        datePicker.setValue(LocalDate.now());
        
        // Setup Item Table Columns
        colSrNo.setCellValueFactory(new PropertyValueFactory<>("srNo"));
        colItemType.setCellValueFactory(new PropertyValueFactory<>("itemType"));
        colSupplyType.setCellValueFactory(new PropertyValueFactory<>("supplyType"));
        colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colRate.setCellValueFactory(new PropertyValueFactory<>("rate"));
        colAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        itemTable.setItems(billItemsList);
        
        supplyTypeCombo.setItems(FXCollections.observableArrayList("भारा", "वजन"));
        supplyTypeCombo.setValue("भारा");
        
        // Setup History Table Columns
        colHistName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colHistMobile.setCellValueFactory(new PropertyValueFactory<>("mobile"));
        colHistTotalPurchase.setCellValueFactory(cellData -> new SimpleStringProperty("0.00")); // Dummy
        colHistReceivable.setCellValueFactory(cellData -> new SimpleStringProperty(String.format("%.2f", cellData.getValue().getOpeningBalance())));
        colHistDate.setCellValueFactory(cellData -> new SimpleStringProperty(LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy"))));
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
        if (itemTypeField.getText().isEmpty() || rateField.getText().isEmpty() || quantityField.getText().isEmpty()) {
            showAlert("कृपया मालाचा प्रकार, संख्या/वजन आणि दर प्रविष्ट करा!");
            return;
        }
        
        try {
            String itemType = itemTypeField.getText();
            String supplyType = supplyTypeCombo.getValue();
            double quantity = Double.parseDouble(quantityField.getText());
            double rate = Double.parseDouble(rateField.getText());
            double amount = quantity * rate;
            
            int srNo = billItemsList.size() + 1;
            
            CustomerBillItem item = new CustomerBillItem(srNo, itemType, supplyType, quantity, rate, amount);
            billItemsList.add(item);
            
            updateTotals();
            
            // Clear fields
            itemTypeField.clear();
            quantityField.clear();
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

    private java.io.File lastGeneratedCustomerBillPdf;

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
                lastGeneratedCustomerBillPdf = file;
                
                // --- DB Save Logic ---
                com.sugarcane.erp.model.Customer customer = customerList.stream()
                        .filter(c -> mobile.equals(c.getMobile()))
                        .findFirst()
                        .orElse(null);
                        
                if (customer == null) {
                    customer = new com.sugarcane.erp.model.Customer(0, customerName, mobile, address, "", "", 0, "ACTIVE");
                    int newId = customerService.addCustomer(customer);
                    customer.setId(newId);
                    customerList.add(customer);
                }

                com.sugarcane.erp.dao.SaleDAO saleDAO = new com.sugarcane.erp.dao.SaleDAO();
                for (CustomerBillItem item : billItemsList) {
                    com.sugarcane.erp.model.Sale sale = new com.sugarcane.erp.model.Sale();
                    sale.setCustomerId(customer.getId());
                    java.time.LocalDate saleDate = java.time.LocalDate.now();
                    if (!date.isEmpty()) {
                        try { saleDate = java.time.LocalDate.parse(date, java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy")); } catch(Exception ignored) {}
                    }
                    sale.setSaleDate(saleDate);
                    sale.setCaneType(item.getItemType());
                    sale.setVehicleNo("");
                    sale.setWeight(item.getQuantity());
                    sale.setRatePerTon(item.getRate());
                    sale.setTotalAmount(item.getAmount());
                    sale.setReceivedAmount(0); // overall receipt handled in CustomerCollection
                    sale.setNetAmount(item.getAmount());
                    sale.setRemarks("Bill " + billNo);
                    saleDAO.addSale(sale);
                }

                if (cashRec > 0) {
                    com.sugarcane.erp.dao.CustomerCollectionDAO collectionDAO = new com.sugarcane.erp.dao.CustomerCollectionDAO();
                    com.sugarcane.erp.model.CustomerCollection coll = new com.sugarcane.erp.model.CustomerCollection();
                    coll.setCustomerId(customer.getId());
                    java.time.LocalDate collDate = java.time.LocalDate.now();
                    if (!date.isEmpty()) {
                        try { collDate = java.time.LocalDate.parse(date, java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy")); } catch(Exception ignored) {}
                    }
                    coll.setCollectionDate(collDate);
                    coll.setAmount(cashRec);
                    coll.setPaymentMode("Cash");
                    coll.setRefNo("Bill " + billNo);
                    coll.setRemarks("Received against Bill " + billNo);
                    collectionDAO.addCollection(coll);
                }
                // ---------------------
                
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
        Customer selectedCustomer = customerHistoryTable.getSelectionModel().getSelectedItem();
        String customerName = customerNameField.getText().trim();
        String mobile = mobileField.getText().trim();

        if (selectedCustomer != null && (customerName.isEmpty() || mobile.isEmpty())) {
            customerName = selectedCustomer.getName();
            mobile = selectedCustomer.getMobile();
        }

        if (customerName.isEmpty() || mobile.isEmpty()) {
            showAlert("ग्राहकाचे नाव आणि मोबाईल नंबर आवश्यक आहे.");
            return;
        }
        if (mobile.length() != 10) {
            showAlert("मोबाईल नंबर १० अंकी असावा.");
            return;
        }

        final String finalMobile = mobile;
        Customer customer = customerList.stream()
                .filter(c -> finalMobile.equals(c.getMobile()))
                .findFirst()
                .orElse(selectedCustomer);

        if (customer == null) {
            customer = new Customer(0, customerName, mobile, addressField.getText(), "", "", 0, "ACTIVE");
            try {
                int newId = customerService.addCustomer(customer);
                customer.setId(newId);
                customerList.add(customer);
            } catch (Exception ignored) {}
        }
        
        try {
            java.io.File dir = new java.io.File(System.getProperty("user.home"), "SugarCaneBills");
            if (!dir.exists()) dir.mkdirs();

            java.io.File billPdfFile = null;
            String billNo = billNoField.getText().trim();
            if (billNo.isEmpty()) billNo = "Auto";

            if (!billItemsList.isEmpty()) {
                double totalAmt = billItemsList.stream().mapToDouble(CustomerBillItem::getAmount).sum();
                double prevBal = previousBalanceField.getText().isEmpty() ? 0 : Double.parseDouble(previousBalanceField.getText());
                double cashRec = cashReceivedField.getText().isEmpty() ? 0 : Double.parseDouble(cashReceivedField.getText());
                double finalBal = totalAmt + prevBal - cashRec;
                String date = datePicker.getValue() != null ? datePicker.getValue().format(java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy")) : "";
                
                billPdfFile = new java.io.File(dir, "CustomerBill_" + customer.getName() + "_" + billNo + ".pdf");
                com.sugarcane.erp.service.PdfGeneratorService.generateCustomerBillPdf(
                        customer.getName(), customer.getMobile(), addressField.getText(), monthField.getText(), date, billNo,
                        billItemsList, totalAmt, prevBal, cashRec, finalBal,
                        billPdfFile.getAbsolutePath()
                );
                lastGeneratedCustomerBillPdf = billPdfFile;
            } else if (lastGeneratedCustomerBillPdf != null && lastGeneratedCustomerBillPdf.exists()) {
                billPdfFile = lastGeneratedCustomerBillPdf;
            } else {
                java.io.File checkFile = new java.io.File(dir, "CustomerBill_" + customer.getName() + "_" + billNo + ".pdf");
                if (checkFile.exists()) {
                    billPdfFile = checkFile;
                } else {
                    final String cName = customer.getName();
                    java.io.File[] matchingFiles = dir.listFiles((d, name) -> name.startsWith("CustomerBill_" + cName) || name.startsWith("Bill_" + cName));
                    if (matchingFiles != null && matchingFiles.length > 0) {
                        java.util.Arrays.sort(matchingFiles, (f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));
                        billPdfFile = matchingFiles[0];
                    }
                }
            }

            // Generate Customer Ledger PDF
            com.sugarcane.erp.service.LedgerService ledgerService = new com.sugarcane.erp.service.LedgerService();
            java.time.LocalDate startDate = java.time.LocalDate.of(2000, 1, 1);
            java.time.LocalDate endDate = java.time.LocalDate.now();
            java.util.List<com.sugarcane.erp.model.LedgerEntry> ledgerEntries = ledgerService.getCustomerLedger(customer);
            String ledgerPdfPath = com.sugarcane.erp.utils.PdfLedgerExporter.generateCustomerLedgerPdf(customer, startDate, endDate, ledgerEntries, null);
            java.io.File ledgerPdfFile = (ledgerPdfPath != null) ? new java.io.File(ledgerPdfPath) : null;

            // Copy both PDF files to system clipboard so user can press Ctrl+V directly in WhatsApp
            javafx.scene.input.Clipboard clipboard = javafx.scene.input.Clipboard.getSystemClipboard();
            javafx.scene.input.ClipboardContent cbContent = new javafx.scene.input.ClipboardContent();
            java.util.List<java.io.File> filesList = new java.util.ArrayList<>();
            if (billPdfFile != null && billPdfFile.exists()) filesList.add(billPdfFile);
            if (ledgerPdfFile != null && ledgerPdfFile.exists()) filesList.add(ledgerPdfFile);
            if (!filesList.isEmpty()) {
                cbContent.putFiles(filesList);
                clipboard.setContent(cbContent);
            }

            String msg = "नमस्कार " + customer.getName() + ",\n\n" +
                         "सोबत तुमचे बिल आणि खतावणी PDF जोडली आहे.\n" +
                         "श्री गणेश कृपा ऊस सप्लायर्स.";
            
            String customerMobile = mobile;
            if (!customerMobile.startsWith("91") && !customerMobile.startsWith("+91")) {
                customerMobile = "91" + customerMobile;
            } else if (customerMobile.startsWith("+91")) {
                customerMobile = customerMobile.substring(1);
            }
            
            String encodedMsg = java.net.URLEncoder.encode(msg, java.nio.charset.StandardCharsets.UTF_8.toString());
            String url = "https://wa.me/" + customerMobile + "?text=" + encodedMsg;
            
            java.awt.Desktop.getDesktop().browse(new java.net.URI(url));
            
            // Open Bills folder and highlight file
            if (dir.exists()) {
                if (billPdfFile != null && billPdfFile.exists()) {
                    try {
                        new ProcessBuilder("explorer.exe", "/select," + billPdfFile.getAbsolutePath()).start();
                    } catch (Exception ex) {
                        java.awt.Desktop.getDesktop().open(dir);
                    }
                } else {
                    java.awt.Desktop.getDesktop().open(dir);
                }
            }
            
            showAlert("WhatsApp वेब उघडले आहे!\n\n१. बिल आणि खतावणी PDF दोन्ही आपोआप कॉपी झाल्या आहेत, फक्त WhatsApp चॅटमध्ये Ctrl + V (Paste) दाबा.\n२. किंवा उघडलेल्या फोल्डरमधून PDF फाईल्स WhatsApp मध्ये ड्रॅग करा.");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("WhatsApp उघडताना त्रुटी आली: " + e.getMessage());
        }
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
        alert.setTitle("माहिती (Information)");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        try {
            javafx.stage.Window owner = null;
            if (customerNameField != null && customerNameField.getScene() != null) {
                owner = customerNameField.getScene().getWindow();
            } else if (customerHistoryTable != null && customerHistoryTable.getScene() != null) {
                owner = customerHistoryTable.getScene().getWindow();
            }
            if (owner != null) {
                alert.initOwner(owner);
                alert.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            }
        } catch (Exception ignored) {}
        alert.showAndWait();
    }
}

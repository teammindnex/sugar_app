package com.sugarcane.erp.controller;

import com.sugarcane.erp.model.Customer;
import com.sugarcane.erp.model.Sale;
import com.sugarcane.erp.service.CustomerService;
import com.sugarcane.erp.service.SaleService;
import com.sugarcane.erp.utils.LanguageManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Optional;

public class SaleController {

    @FXML private Label titleLabel;
    
    // Table components
    @FXML private TableView<Sale> saleTable;
    @FXML private TableColumn<Sale, String> colDate;
    @FXML private TableColumn<Sale, String> colCustomer;
    @FXML private TableColumn<Sale, String> colWeight;
    @FXML private TableColumn<Sale, String> colRate;
    @FXML private TableColumn<Sale, String> colNet;
    
    // Form fields
    @FXML private ComboBox<Customer> customerComboBox;
    @FXML private DatePicker saleDatePicker;
    @FXML private TextField caneTypeField;
    @FXML private TextField vehicleNoField;
    @FXML private TextField weightField;
    @FXML private TextField rateField;
    @FXML private TextField totalAmountField;
    @FXML private TextField receivedAmountField;
    @FXML private TextField netAmountField;
    @FXML private TextArea remarksArea;
    
    // Buttons
    @FXML private Button btnSave;
    @FXML private Button btnDelete;
    @FXML private Button btnClear;

    private SaleService saleService;
    private CustomerService customerService;
    private ObservableList<Sale> saleList = FXCollections.observableArrayList();
    private ObservableList<Customer> customerList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        saleService = new SaleService();
        customerService = new CustomerService();
        
        // i18n Bindings
        titleLabel.textProperty().bind(LanguageManager.createStringBinding("sale.title"));
        colDate.textProperty().bind(LanguageManager.createStringBinding("field.date"));
        colCustomer.textProperty().bind(LanguageManager.createStringBinding("menu.customers"));
        colWeight.textProperty().bind(LanguageManager.createStringBinding("purchase.weight"));
        colRate.textProperty().bind(LanguageManager.createStringBinding("purchase.rate"));
        colNet.textProperty().bind(LanguageManager.createStringBinding("purchase.netAmount"));
        
        btnSave.textProperty().bind(LanguageManager.createStringBinding("btn.save"));
        btnDelete.textProperty().bind(LanguageManager.createStringBinding("btn.delete"));
        btnClear.textProperty().bind(LanguageManager.createStringBinding("btn.clear"));

        // Configure Table Columns
        colDate.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getSaleDate().toString()));
        colCustomer.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        colWeight.setCellValueFactory(cellData -> new SimpleStringProperty(String.format("%.2f", cellData.getValue().getWeight())));
        colRate.setCellValueFactory(cellData -> new SimpleStringProperty(String.format("%.2f", cellData.getValue().getRatePerTon())));
        colNet.setCellValueFactory(cellData -> new SimpleStringProperty(String.format("%.2f", cellData.getValue().getNetAmount())));

        // ComboBox setup
        customerComboBox.setConverter(new StringConverter<Customer>() {
            @Override
            public String toString(Customer c) {
                return c != null ? c.getName() : "";
            }
            @Override
            public Customer fromString(String string) { return null; }
        });

        // Add calculation listeners
        weightField.textProperty().addListener((obs, oldV, newV) -> calculate());
        rateField.textProperty().addListener((obs, oldV, newV) -> calculate());
        receivedAmountField.textProperty().addListener((obs, oldV, newV) -> calculate());

        loadCustomers();
        loadSales();
        handleClear();
    }

    private void loadCustomers() {
        try {
            customerList.clear();
            customerList.addAll(customerService.getAllCustomers());
            customerComboBox.setItems(customerList);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadSales() {
        try {
            saleList.clear();
            saleList.addAll(saleService.getAllSales());
            saleTable.setItems(saleList);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void calculate() {
        try {
            double weight = parseDouble(weightField.getText());
            double rate = parseDouble(rateField.getText());
            double received = parseDouble(receivedAmountField.getText());

            double total = weight * rate;
            totalAmountField.setText(String.format("%.2f", total));

            double net = total - received;
            netAmountField.setText(String.format("%.2f", net));
        } catch (Exception e) {
            // Ignore format errors while typing
        }
    }

    private double parseDouble(String str) {
        if (str == null || str.trim().isEmpty()) return 0.0;
        try {
            return Double.parseDouble(str);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    @FXML
    private void handleSave() {
        if (!validateForm()) return;
        
        try {
            Sale s = new Sale();
            s.setCustomerId(customerComboBox.getValue().getId());
            s.setSaleDate(saleDatePicker.getValue());
            s.setCaneType(caneTypeField.getText());
            s.setVehicleNo(vehicleNoField.getText());
            s.setWeight(parseDouble(weightField.getText()));
            s.setRatePerTon(parseDouble(rateField.getText()));
            s.setTotalAmount(parseDouble(totalAmountField.getText()));
            s.setReceivedAmount(parseDouble(receivedAmountField.getText()));
            s.setNetAmount(parseDouble(netAmountField.getText()));
            s.setRemarks(remarksArea.getText());

            saleService.addSale(s);
            showAlert(LanguageManager.get("msg.save.success"));
            handleClear();
            loadSales();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error saving sale: " + e.getMessage());
        }
    }

    @FXML
    private void handleDelete() {
        Sale selected = saleTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(LanguageManager.get("btn.delete"));
        alert.setHeaderText(null);
        alert.setContentText(LanguageManager.get("msg.delete.confirm"));

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                saleService.deleteSale(selected.getId());
                showAlert(LanguageManager.get("msg.delete.success"));
                loadSales();
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert("Cannot delete sale.");
            }
        }
    }

    @FXML
    private void handleClear() {
        customerComboBox.getSelectionModel().clearSelection();
        saleDatePicker.setValue(LocalDate.now());
        caneTypeField.clear();
        vehicleNoField.clear();
        weightField.setText("0.0");
        rateField.setText("0.0");
        totalAmountField.setText("0.0");
        receivedAmountField.setText("0.0");
        netAmountField.setText("0.0");
        remarksArea.clear();
        saleTable.getSelectionModel().clearSelection();
    }

    private boolean validateForm() {
        if (customerComboBox.getValue() == null) {
            showAlert("Please select a Customer.");
            return false;
        }
        if (parseDouble(weightField.getText()) <= 0) {
            showAlert("Weight must be greater than 0.");
            return false;
        }
        return true;
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

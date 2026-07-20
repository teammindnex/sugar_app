package com.sugarcane.erp.controller;

import com.sugarcane.erp.model.Customer;
import com.sugarcane.erp.model.Farmer;
import com.sugarcane.erp.model.LedgerEntry;
import com.sugarcane.erp.service.CustomerService;
import com.sugarcane.erp.service.FarmerService;
import com.sugarcane.erp.service.LedgerService;
import com.sugarcane.erp.utils.ExcelExporter;
import com.sugarcane.erp.utils.LanguageManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;

import java.sql.SQLException;

public class LedgerController {

    // Farmer Tab
    @FXML private ComboBox<Farmer> farmerComboBox;
    @FXML private Button btnExportFarmer;
    @FXML private TableView<LedgerEntry> farmerTable;
    @FXML private TableColumn<LedgerEntry, String> colFDate;
    @FXML private TableColumn<LedgerEntry, String> colFParticulars;
    @FXML private TableColumn<LedgerEntry, String> colFDebit;
    @FXML private TableColumn<LedgerEntry, String> colFCredit;
    @FXML private TableColumn<LedgerEntry, String> colFBalance;
    
    // Customer Tab
    @FXML private ComboBox<Customer> customerComboBox;
    @FXML private Button btnExportCustomer;
    @FXML private TableView<LedgerEntry> customerTable;
    @FXML private TableColumn<LedgerEntry, String> colCDate;
    @FXML private TableColumn<LedgerEntry, String> colCParticulars;
    @FXML private TableColumn<LedgerEntry, String> colCDebit;
    @FXML private TableColumn<LedgerEntry, String> colCCredit;
    @FXML private TableColumn<LedgerEntry, String> colCBalance;

    private LedgerService ledgerService;
    private FarmerService farmerService;
    private CustomerService customerService;
    
    private ObservableList<LedgerEntry> farmerEntries = FXCollections.observableArrayList();
    private ObservableList<LedgerEntry> customerEntries = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        ledgerService = new LedgerService();
        farmerService = new FarmerService();
        customerService = new CustomerService();
        
        setupTable(farmerTable, colFDate, colFParticulars, colFDebit, colFCredit, colFBalance);
        setupTable(customerTable, colCDate, colCParticulars, colCDebit, colCCredit, colCBalance);
        
        // ComboBox conversions
        farmerComboBox.setConverter(new StringConverter<Farmer>() {
            @Override public String toString(Farmer f) { return f != null ? f.getName() : ""; }
            @Override public Farmer fromString(String s) { return null; }
        });
        customerComboBox.setConverter(new StringConverter<Customer>() {
            @Override public String toString(Customer c) { return c != null ? c.getName() : ""; }
            @Override public Customer fromString(String s) { return null; }
        });
        
        // Listeners
        farmerComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) loadFarmerLedger(newVal);
        });
        
        customerComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) loadCustomerLedger(newVal);
        });

        loadMasters();
    }
    
    private void setupTable(TableView<LedgerEntry> table, TableColumn<LedgerEntry, String> colDate,
                            TableColumn<LedgerEntry, String> colPart, TableColumn<LedgerEntry, String> colDeb,
                            TableColumn<LedgerEntry, String> colCred, TableColumn<LedgerEntry, String> colBal) {
        
        colDate.setCellValueFactory(cellData -> {
            if (cellData.getValue().getDate().getYear() < 2000) return new SimpleStringProperty(""); // Opening balance date hidden
            return new SimpleStringProperty(cellData.getValue().getDate().toString());
        });
        colPart.setCellValueFactory(new PropertyValueFactory<>("particulars"));
        colDeb.setCellValueFactory(cellData -> new SimpleStringProperty(String.format("%.2f", cellData.getValue().getDebit())));
        colCred.setCellValueFactory(cellData -> new SimpleStringProperty(String.format("%.2f", cellData.getValue().getCredit())));
        colBal.setCellValueFactory(cellData -> new SimpleStringProperty(String.format("%.2f", cellData.getValue().getBalance())));
    }

    private void loadMasters() {
        try {
            farmerComboBox.getItems().addAll(farmerService.getAllFarmers());
            customerComboBox.getItems().addAll(customerService.getAllCustomers());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadFarmerLedger(Farmer farmer) {
        try {
            farmerEntries.setAll(ledgerService.getFarmerLedger(farmer));
            farmerTable.setItems(farmerEntries);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadCustomerLedger(Customer customer) {
        try {
            customerEntries.setAll(ledgerService.getCustomerLedger(customer));
            customerTable.setItems(customerEntries);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleExportFarmer() {
        if (farmerComboBox.getValue() == null) return;
        String name = "Ledger_Farmer_" + farmerComboBox.getValue().getName().replace(" ", "_");
        ExcelExporter.exportTableToExcel(farmerTable, name, btnExportFarmer.getScene().getWindow());
    }

    @FXML
    private void handleExportCustomer() {
        if (customerComboBox.getValue() == null) return;
        String name = "Ledger_Customer_" + customerComboBox.getValue().getName().replace(" ", "_");
        ExcelExporter.exportTableToExcel(customerTable, name, btnExportCustomer.getScene().getWindow());
    }
}

package com.sugarcane.erp.controller;

import com.sugarcane.erp.model.Customer;
import com.sugarcane.erp.model.Farmer;
import com.sugarcane.erp.model.LedgerEntry;
import com.sugarcane.erp.service.CustomerService;
import com.sugarcane.erp.service.FarmerService;
import com.sugarcane.erp.service.LedgerService;
import com.sugarcane.erp.utils.ExcelExporter;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class LedgerController {

    // Global Filters
    @FXML private DatePicker fromDate;
    @FXML private DatePicker toDate;

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
        
        // Date Converters (DD/MM/YYYY)
        StringConverter<LocalDate> dateConverter = new StringConverter<LocalDate>() {
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            @Override
            public String toString(LocalDate date) {
                return date != null ? dateFormatter.format(date) : "";
            }
            @Override
            public LocalDate fromString(String string) {
                try {
                    return (string != null && !string.isEmpty()) ? LocalDate.parse(string, dateFormatter) : null;
                } catch (Exception e) { return null; }
            }
        };
        fromDate.setConverter(dateConverter);
        toDate.setConverter(dateConverter);
        
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
        
        DateTimeFormatter outFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        colDate.setCellValueFactory(cellData -> {
            if (cellData.getValue().getDate().getYear() < 2000) return new SimpleStringProperty(""); // Opening balance date hidden
            return new SimpleStringProperty(outFormatter.format(cellData.getValue().getDate()));
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

    public void setInitialFarmerFilter(Farmer farmer, LocalDate start, LocalDate end) {
        if (start != null) fromDate.setValue(start);
        if (end != null) toDate.setValue(end);
        
        if (farmer != null) {
            for (Farmer f : farmerComboBox.getItems()) {
                if (f.getId() == farmer.getId()) {
                    farmerComboBox.getSelectionModel().select(f);
                    break;
                }
            }
        }
    }

    @FXML
    private void handleDateFilter() {
        if (farmerComboBox.getValue() != null) {
            loadFarmerLedger(farmerComboBox.getValue());
        }
        if (customerComboBox.getValue() != null) {
            loadCustomerLedger(customerComboBox.getValue());
        }
    }

    private void loadFarmerLedger(Farmer farmer) {
        try {
            List<LedgerEntry> allEntries = ledgerService.getFarmerLedger(farmer);
            farmerEntries.setAll(filterEntriesByDate(allEntries));
            farmerTable.setItems(farmerEntries);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadCustomerLedger(Customer customer) {
        try {
            List<LedgerEntry> allEntries = ledgerService.getCustomerLedger(customer);
            customerEntries.setAll(filterEntriesByDate(allEntries));
            customerTable.setItems(customerEntries);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private List<LedgerEntry> filterEntriesByDate(List<LedgerEntry> allEntries) {
        LocalDate start = fromDate.getValue();
        LocalDate end = toDate.getValue();
        
        if (start == null && end == null) {
            return allEntries; // No filter applied
        }

        List<LedgerEntry> filtered = new ArrayList<>();
        double carryForwardBalance = 0;
        
        for (LedgerEntry entry : allEntries) {
            LocalDate d = entry.getDate();
            if (d.getYear() < 2000) {
                // Original opening balance
                carryForwardBalance += entry.getDebit() - entry.getCredit();
                continue;
            }
            
            boolean isBeforeStart = (start != null && d.isBefore(start));
            boolean isAfterEnd = (end != null && d.isAfter(end));
            
            if (isBeforeStart) {
                carryForwardBalance += entry.getDebit() - entry.getCredit();
            } else if (isAfterEnd) {
                // Ignore entirely
            } else {
                filtered.add(entry);
            }
        }
        
        // Build new filtered list with combined opening balance
        LedgerEntry openingEntry = new LedgerEntry(
            LocalDate.now().minusYears(10), 
            "मागील बाकी (Opening Balance)", 
            "",
            0.0,
            carryForwardBalance > 0 ? carryForwardBalance : 0,
            carryForwardBalance < 0 ? Math.abs(carryForwardBalance) : 0
        );
        
        List<LedgerEntry> finalList = new ArrayList<>();
        finalList.add(openingEntry);
        finalList.addAll(filtered);
        
        // Recalculate running balance
        double running = 0;
        for (LedgerEntry e : finalList) {
            running = running + e.getDebit() - e.getCredit();
            e.setBalance(running);
        }
        
        return finalList;
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

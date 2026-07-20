package com.sugarcane.erp.controller;

import com.sugarcane.erp.model.Customer;
import com.sugarcane.erp.model.Farmer;
import com.sugarcane.erp.model.Transport;
import com.sugarcane.erp.model.TransportTrip;
import com.sugarcane.erp.service.CustomerService;
import com.sugarcane.erp.service.FarmerService;
import com.sugarcane.erp.service.TransportService;
import com.sugarcane.erp.service.TransportTripService;
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

public class TransportTripController {

    @FXML private Label titleLabel;
    
    // Table components
    @FXML private TableView<TransportTrip> tripTable;
    @FXML private TableColumn<TransportTrip, String> colDate;
    @FXML private TableColumn<TransportTrip, String> colTransport;
    @FXML private TableColumn<TransportTrip, String> colPickup;
    @FXML private TableColumn<TransportTrip, String> colDest;
    @FXML private TableColumn<TransportTrip, String> colBalance;
    
    // Form fields
    @FXML private ComboBox<Transport> transportComboBox;
    @FXML private DatePicker tripDatePicker;
    @FXML private ComboBox<Farmer> farmerComboBox;
    @FXML private ComboBox<Customer> customerComboBox;
    @FXML private TextField pickupLocationField;
    @FXML private TextField destinationField;
    
    @FXML private TextField weightField;
    @FXML private TextField tripChargeField;
    @FXML private TextField dieselField;
    @FXML private TextField tollField;
    @FXML private TextField advanceField;
    
    @FXML private TextField balanceField;
    @FXML private ComboBox<String> statusComboBox;
    
    // Buttons
    @FXML private Button btnSave;
    @FXML private Button btnDelete;
    @FXML private Button btnClear;

    private TransportTripService tripService;
    private TransportService transportService;
    private FarmerService farmerService;
    private CustomerService customerService;
    
    private ObservableList<TransportTrip> tripList = FXCollections.observableArrayList();
    private ObservableList<Transport> transportList = FXCollections.observableArrayList();
    private ObservableList<Farmer> farmerList = FXCollections.observableArrayList();
    private ObservableList<Customer> customerList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        tripService = new TransportTripService();
        transportService = new TransportService();
        farmerService = new FarmerService();
        customerService = new CustomerService();
        
        // i18n Bindings
        titleLabel.textProperty().bind(LanguageManager.createStringBinding("transportTrip.title"));
        colDate.textProperty().bind(LanguageManager.createStringBinding("field.date"));
        colTransport.textProperty().bind(LanguageManager.createStringBinding("menu.transports"));
        colPickup.textProperty().bind(LanguageManager.createStringBinding("transportTrip.pickup"));
        colDest.textProperty().bind(LanguageManager.createStringBinding("transportTrip.dest"));
        colBalance.textProperty().bind(LanguageManager.createStringBinding("transportTrip.balance"));
        
        btnSave.textProperty().bind(LanguageManager.createStringBinding("btn.save"));
        btnDelete.textProperty().bind(LanguageManager.createStringBinding("btn.delete"));
        btnClear.textProperty().bind(LanguageManager.createStringBinding("btn.clear"));

        // Configure Table Columns
        colDate.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTripDate().toString()));
        colTransport.setCellValueFactory(new PropertyValueFactory<>("transportName"));
        colPickup.setCellValueFactory(new PropertyValueFactory<>("pickupLocation"));
        colDest.setCellValueFactory(new PropertyValueFactory<>("destination"));
        colBalance.setCellValueFactory(cellData -> new SimpleStringProperty(String.format("%.2f", cellData.getValue().getBalance())));

        // ComboBox setups
        transportComboBox.setConverter(new StringConverter<Transport>() {
            @Override public String toString(Transport t) { return t != null ? t.getVehicleNo() : ""; }
            @Override public Transport fromString(String s) { return null; }
        });
        farmerComboBox.setConverter(new StringConverter<Farmer>() {
            @Override public String toString(Farmer f) { return f != null ? f.getName() : ""; }
            @Override public Farmer fromString(String s) { return null; }
        });
        customerComboBox.setConverter(new StringConverter<Customer>() {
            @Override public String toString(Customer c) { return c != null ? c.getName() : ""; }
            @Override public Customer fromString(String s) { return null; }
        });
        
        statusComboBox.getItems().addAll("COMPLETED", "IN_TRANSIT", "CANCELLED");
        statusComboBox.setValue("COMPLETED");

        // Add calculation listeners
        tripChargeField.textProperty().addListener((obs, oldV, newV) -> calculate());
        dieselField.textProperty().addListener((obs, oldV, newV) -> calculate());
        tollField.textProperty().addListener((obs, oldV, newV) -> calculate());
        advanceField.textProperty().addListener((obs, oldV, newV) -> calculate());

        loadMasters();
        loadTrips();
        handleClear();
    }

    private void loadMasters() {
        try {
            transportList.setAll(transportService.getAllTransports());
            transportComboBox.setItems(transportList);
            
            farmerList.setAll(farmerService.getAllFarmers());
            farmerComboBox.setItems(farmerList);
            
            customerList.setAll(customerService.getAllCustomers());
            customerComboBox.setItems(customerList);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadTrips() {
        try {
            tripList.setAll(tripService.getAllTrips());
            tripTable.setItems(tripList);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void calculate() {
        try {
            double charge = parseDouble(tripChargeField.getText());
            double diesel = parseDouble(dieselField.getText());
            double toll = parseDouble(tollField.getText());
            double advance = parseDouble(advanceField.getText());

            double balance = charge - diesel - toll - advance;
            balanceField.setText(String.format("%.2f", balance));
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
        if (transportComboBox.getValue() == null) {
            showAlert("Please select a Transport Vehicle.");
            return;
        }
        
        try {
            TransportTrip t = new TransportTrip();
            t.setTransportId(transportComboBox.getValue().getId());
            t.setTripDate(tripDatePicker.getValue());
            
            if (farmerComboBox.getValue() != null) t.setFarmerId(farmerComboBox.getValue().getId());
            if (customerComboBox.getValue() != null) t.setCustomerId(customerComboBox.getValue().getId());
            
            t.setPickupLocation(pickupLocationField.getText());
            t.setDestination(destinationField.getText());
            t.setWeight(parseDouble(weightField.getText()));
            t.setTripCharge(parseDouble(tripChargeField.getText()));
            t.setDiesel(parseDouble(dieselField.getText()));
            t.setToll(parseDouble(tollField.getText()));
            t.setAdvance(parseDouble(advanceField.getText()));
            t.setBalance(parseDouble(balanceField.getText()));
            t.setTripStatus(statusComboBox.getValue());

            tripService.addTrip(t);
            showAlert(LanguageManager.get("msg.save.success"));
            handleClear();
            loadTrips();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error saving trip: " + e.getMessage());
        }
    }

    @FXML
    private void handleDelete() {
        TransportTrip selected = tripTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(LanguageManager.get("btn.delete"));
        alert.setHeaderText(null);
        alert.setContentText(LanguageManager.get("msg.delete.confirm"));

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                tripService.deleteTrip(selected.getId());
                showAlert(LanguageManager.get("msg.delete.success"));
                loadTrips();
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert("Cannot delete trip.");
            }
        }
    }

    @FXML
    private void handleClear() {
        transportComboBox.getSelectionModel().clearSelection();
        tripDatePicker.setValue(LocalDate.now());
        farmerComboBox.getSelectionModel().clearSelection();
        customerComboBox.getSelectionModel().clearSelection();
        pickupLocationField.clear();
        destinationField.clear();
        weightField.setText("0.0");
        tripChargeField.setText("0.0");
        dieselField.setText("0.0");
        tollField.setText("0.0");
        advanceField.setText("0.0");
        balanceField.setText("0.0");
        statusComboBox.setValue("COMPLETED");
        tripTable.getSelectionModel().clearSelection();
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

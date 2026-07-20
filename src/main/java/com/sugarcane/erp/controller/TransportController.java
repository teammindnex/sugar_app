package com.sugarcane.erp.controller;

import com.sugarcane.erp.model.Transport;
import com.sugarcane.erp.service.TransportService;
import com.sugarcane.erp.utils.LanguageManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.SQLException;
import java.util.Optional;

public class TransportController {

    @FXML private Label titleLabel;
    
    // Table components
    @FXML private TableView<Transport> transportTable;
    @FXML private TableColumn<Transport, String> colTransportName;
    @FXML private TableColumn<Transport, String> colVehicleNo;
    @FXML private TableColumn<Transport, String> colDriverName;
    @FXML private TableColumn<Transport, String> colDriverMobile;
    @FXML private TableColumn<Transport, String> colStatus;
    
    // Form fields
    @FXML private TextField transportNameField;
    @FXML private TextField vehicleNoField;
    @FXML private TextField driverNameField;
    @FXML private TextField driverMobileField;
    
    // Buttons
    @FXML private Button btnSave;
    @FXML private Button btnUpdate;
    @FXML private Button btnDelete;
    @FXML private Button btnClear;

    private TransportService transportService;
    private ObservableList<Transport> transportList = FXCollections.observableArrayList();
    private Transport selectedTransport;

    @FXML
    public void initialize() {
        transportService = new TransportService();
        
        // i18n Bindings
        titleLabel.textProperty().bind(LanguageManager.createStringBinding("transport.title"));
        colTransportName.textProperty().bind(LanguageManager.createStringBinding("transport.name"));
        colVehicleNo.textProperty().bind(LanguageManager.createStringBinding("transport.vehicleNo"));
        colDriverName.textProperty().bind(LanguageManager.createStringBinding("transport.driverName"));
        colDriverMobile.textProperty().bind(LanguageManager.createStringBinding("transport.driverMobile"));
        colStatus.textProperty().bind(LanguageManager.createStringBinding("field.status"));
        
        btnSave.textProperty().bind(LanguageManager.createStringBinding("btn.save"));
        btnUpdate.textProperty().bind(LanguageManager.createStringBinding("btn.update"));
        btnDelete.textProperty().bind(LanguageManager.createStringBinding("btn.delete"));
        btnClear.textProperty().bind(LanguageManager.createStringBinding("btn.clear"));

        // Configure Table Columns
        colTransportName.setCellValueFactory(new PropertyValueFactory<>("transportName"));
        colVehicleNo.setCellValueFactory(new PropertyValueFactory<>("vehicleNo"));
        colDriverName.setCellValueFactory(new PropertyValueFactory<>("driverName"));
        colDriverMobile.setCellValueFactory(new PropertyValueFactory<>("driverMobile"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Table Selection Listener
        transportTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                populateForm(newSelection);
            }
        });

        loadTransports();
        setUpdateMode(false);
    }

    private void loadTransports() {
        try {
            transportList.clear();
            transportList.addAll(transportService.getAllTransports());
            transportTable.setItems(transportList);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error loading transports: " + e.getMessage());
        }
    }

    @FXML
    private void handleSave() {
        if (!validateForm()) return;
        
        try {
            Transport transport = createTransportFromForm();
            transport.setStatus("ACTIVE");
            transportService.addTransport(transport);
            showAlert(LanguageManager.get("msg.save.success"));
            handleClear();
            loadTransports();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error saving transport: " + e.getMessage());
        }
    }

    @FXML
    private void handleUpdate() {
        if (selectedTransport == null || !validateForm()) return;

        try {
            Transport transport = createTransportFromForm();
            transport.setId(selectedTransport.getId());
            transport.setStatus(selectedTransport.getStatus());
            transportService.updateTransport(transport);
            showAlert(LanguageManager.get("msg.update.success"));
            handleClear();
            loadTransports();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error updating transport: " + e.getMessage());
        }
    }

    @FXML
    private void handleDelete() {
        if (selectedTransport == null) return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(LanguageManager.get("btn.delete"));
        alert.setHeaderText(null);
        alert.setContentText(LanguageManager.get("msg.delete.confirm"));

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                transportService.deleteTransport(selectedTransport.getId());
                showAlert(LanguageManager.get("msg.delete.success"));
                handleClear();
                loadTransports();
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert("Cannot delete transport. It might have active trips.");
            }
        }
    }

    @FXML
    private void handleClear() {
        transportNameField.clear();
        vehicleNoField.clear();
        driverNameField.clear();
        driverMobileField.clear();
        
        transportTable.getSelectionModel().clearSelection();
        selectedTransport = null;
        setUpdateMode(false);
    }

    private void populateForm(Transport transport) {
        this.selectedTransport = transport;
        transportNameField.setText(transport.getTransportName());
        vehicleNoField.setText(transport.getVehicleNo());
        driverNameField.setText(transport.getDriverName());
        driverMobileField.setText(transport.getDriverMobile());
        
        setUpdateMode(true);
    }

    private Transport createTransportFromForm() {
        Transport transport = new Transport();
        transport.setTransportName(transportNameField.getText());
        transport.setVehicleNo(vehicleNoField.getText());
        transport.setDriverName(driverNameField.getText());
        transport.setDriverMobile(driverMobileField.getText());
        
        return transport;
    }

    private boolean validateForm() {
        if (vehicleNoField.getText().trim().isEmpty()) {
            showAlert("Vehicle Number is required!");
            return false;
        }
        return true;
    }

    private void setUpdateMode(boolean isUpdate) {
        btnSave.setDisable(isUpdate);
        btnUpdate.setDisable(!isUpdate);
        btnDelete.setDisable(!isUpdate);
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

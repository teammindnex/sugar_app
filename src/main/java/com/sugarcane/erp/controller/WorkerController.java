package com.sugarcane.erp.controller;

import com.sugarcane.erp.model.Worker;
import com.sugarcane.erp.service.WorkerService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.SQLException;
import java.util.Optional;

public class WorkerController {

    // Form fields
    @FXML private TextField nameField;
    @FXML private TextField mobileField;
    
    // Search
    @FXML private TextField searchField;
    
    // Table components
    @FXML private TableView<Worker> workerTable;
    @FXML private TableColumn<Worker, Integer> colId;
    @FXML private TableColumn<Worker, String> colName;
    @FXML private TableColumn<Worker, String> colMobile;
    @FXML private TableColumn<Worker, String> colBalance;

    private WorkerService workerService;
    private ObservableList<Worker> workerList = FXCollections.observableArrayList();
    private Worker selectedWorker;

    @FXML
    public void initialize() {
        workerService = new WorkerService();
        
        // Configure Table Columns
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colMobile.setCellValueFactory(new PropertyValueFactory<>("mobile"));
        colBalance.setCellValueFactory(cellData -> new SimpleStringProperty("0.00"));

        // Table Selection Listener
        workerTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                populateForm(newSelection);
            }
        });

        loadWorkers();
    }

    @FXML
    private void loadWorkers() {
        try {
            workerList.clear();
            workerList.addAll(workerService.getAllWorkers());
            workerTable.setItems(workerList);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error loading workers: " + e.getMessage());
        }
    }

    @FXML
    private void handleSave() {
        if (!validateForm()) return;
        
        try {
            Worker worker = createWorkerFromForm();
            worker.setStatus("ACTIVE");
            workerService.addWorker(worker);
            showAlert("कामगार यशस्वीरित्या जोडला!");
            handleClear();
            loadWorkers();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error saving worker: " + e.getMessage());
        }
    }

    @FXML
    private void handleUpdate() {
        if (selectedWorker == null || !validateForm()) return;

        try {
            Worker worker = createWorkerFromForm();
            worker.setId(selectedWorker.getId());
            worker.setStatus(selectedWorker.getStatus());
            
            // Keep old values for missing fields to avoid overriding them
            worker.setVillage(selectedWorker.getVillage());
            worker.setWorkType(selectedWorker.getWorkType());
            worker.setJoiningDate(selectedWorker.getJoiningDate());
            
            workerService.updateWorker(worker);
            showAlert("बदल यशस्वीरित्या जतन केले!");
            handleClear();
            loadWorkers();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error updating worker: " + e.getMessage());
        }
    }

    @FXML
    private void handleDelete() {
        if (selectedWorker == null) return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("नोंद काढा");
        alert.setHeaderText(null);
        alert.setContentText("तुम्हाला खात्री आहे का की तुम्ही हा कामगार हटवू इच्छिता?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                workerService.deleteWorker(selectedWorker.getId());
                showAlert("कामगार यशस्वीरित्या हटवला!");
                handleClear();
                loadWorkers();
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert("कामगार हटवता आला नाही. त्यांच्याशी संबंधित नोंदी असू शकतात.");
            }
        }
    }

    @FXML
    private void handleClear() {
        nameField.clear();
        mobileField.clear();
        workerTable.getSelectionModel().clearSelection();
        selectedWorker = null;
    }

    private void populateForm(Worker worker) {
        this.selectedWorker = worker;
        nameField.setText(worker.getName());
        mobileField.setText(worker.getMobile());
    }

    private Worker createWorkerFromForm() {
        Worker worker = new Worker();
        worker.setName(nameField.getText());
        worker.setMobile(mobileField.getText());
        return worker;
    }

    private boolean validateForm() {
        if (nameField.getText() == null || nameField.getText().trim().isEmpty()) {
            showAlert("कामगाराचे नाव आवश्यक आहे!");
            return false;
        }
        return true;
    }
    
    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}

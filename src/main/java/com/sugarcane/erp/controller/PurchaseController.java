package com.sugarcane.erp.controller;

import com.sugarcane.erp.model.Farmer;
import com.sugarcane.erp.model.Purchase;
import com.sugarcane.erp.service.FarmerService;
import com.sugarcane.erp.service.PurchaseService;
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

public class PurchaseController {

    @FXML private Label titleLabel;
    
    // Table components
    @FXML private TableView<Purchase> purchaseTable;
    @FXML private TableColumn<Purchase, String> colDate;
    @FXML private TableColumn<Purchase, String> colFarmer;
    @FXML private TableColumn<Purchase, String> colWeight;
    @FXML private TableColumn<Purchase, String> colRate;
    @FXML private TableColumn<Purchase, String> colNet;
    
    // Form fields
    @FXML private ComboBox<Farmer> farmerComboBox;
    @FXML private DatePicker purchaseDatePicker;
    @FXML private TextField caneTypeField;
    @FXML private TextField vehicleNoField;
    @FXML private TextField weightField;
    @FXML private TextField rateField;
    @FXML private TextField totalAmountField;
    @FXML private TextField advanceField;
    @FXML private TextField loadingChargesField;
    @FXML private TextField cuttingChargesField;
    @FXML private TextField transportChargesField;
    @FXML private TextField otherChargesField;
    @FXML private TextField netAmountField;
    @FXML private TextArea remarksArea;
    
    // Buttons
    @FXML private Button btnSave;
    @FXML private Button btnDelete;
    @FXML private Button btnClear;

    private PurchaseService purchaseService;
    private FarmerService farmerService;
    private ObservableList<Purchase> purchaseList = FXCollections.observableArrayList();
    private ObservableList<Farmer> farmerList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        purchaseService = new PurchaseService();
        farmerService = new FarmerService();
        
        // i18n Bindings
        titleLabel.textProperty().bind(LanguageManager.createStringBinding("purchase.title"));
        colDate.textProperty().bind(LanguageManager.createStringBinding("field.date"));
        colFarmer.textProperty().bind(LanguageManager.createStringBinding("menu.farmers"));
        colWeight.textProperty().bind(LanguageManager.createStringBinding("purchase.weight"));
        colRate.textProperty().bind(LanguageManager.createStringBinding("purchase.rate"));
        colNet.textProperty().bind(LanguageManager.createStringBinding("purchase.netAmount"));
        
        btnSave.textProperty().bind(LanguageManager.createStringBinding("btn.save"));
        btnDelete.textProperty().bind(LanguageManager.createStringBinding("btn.delete"));
        btnClear.textProperty().bind(LanguageManager.createStringBinding("btn.clear"));

        // Configure Table Columns
        colDate.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPurchaseDate().toString()));
        colFarmer.setCellValueFactory(new PropertyValueFactory<>("farmerName"));
        colWeight.setCellValueFactory(cellData -> new SimpleStringProperty(String.format("%.2f", cellData.getValue().getWeight())));
        colRate.setCellValueFactory(cellData -> new SimpleStringProperty(String.format("%.2f", cellData.getValue().getRatePerTon())));
        colNet.setCellValueFactory(cellData -> new SimpleStringProperty(String.format("%.2f", cellData.getValue().getNetAmount())));

        // ComboBox setup
        farmerComboBox.setConverter(new StringConverter<Farmer>() {
            @Override
            public String toString(Farmer f) {
                return f != null ? f.getName() : "";
            }
            @Override
            public Farmer fromString(String string) { return null; }
        });

        // Add calculation listeners
        weightField.textProperty().addListener((obs, oldV, newV) -> calculate());
        rateField.textProperty().addListener((obs, oldV, newV) -> calculate());
        advanceField.textProperty().addListener((obs, oldV, newV) -> calculate());
        loadingChargesField.textProperty().addListener((obs, oldV, newV) -> calculate());
        cuttingChargesField.textProperty().addListener((obs, oldV, newV) -> calculate());
        transportChargesField.textProperty().addListener((obs, oldV, newV) -> calculate());
        otherChargesField.textProperty().addListener((obs, oldV, newV) -> calculate());

        loadFarmers();
        loadPurchases();
        handleClear();
    }

    private void loadFarmers() {
        try {
            farmerList.clear();
            farmerList.addAll(farmerService.getAllFarmers());
            farmerComboBox.setItems(farmerList);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadPurchases() {
        try {
            purchaseList.clear();
            purchaseList.addAll(purchaseService.getAllPurchases());
            purchaseTable.setItems(purchaseList);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void calculate() {
        try {
            double weight = parseDouble(weightField.getText());
            double rate = parseDouble(rateField.getText());
            double advance = parseDouble(advanceField.getText());
            double loading = parseDouble(loadingChargesField.getText());
            double cutting = parseDouble(cuttingChargesField.getText());
            double transport = parseDouble(transportChargesField.getText());
            double other = parseDouble(otherChargesField.getText());

            double total = weight * rate;
            totalAmountField.setText(String.format("%.2f", total));

            double net = total - advance - loading - cutting - transport - other;
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
            Purchase p = new Purchase();
            p.setFarmerId(farmerComboBox.getValue().getId());
            p.setPurchaseDate(purchaseDatePicker.getValue());
            p.setCaneType(caneTypeField.getText());
            p.setVehicleNo(vehicleNoField.getText());
            p.setWeight(parseDouble(weightField.getText()));
            p.setRatePerTon(parseDouble(rateField.getText()));
            p.setTotalAmount(parseDouble(totalAmountField.getText()));
            p.setAdvance(parseDouble(advanceField.getText()));
            p.setLoadingCharges(parseDouble(loadingChargesField.getText()));
            p.setCuttingCharges(parseDouble(cuttingChargesField.getText()));
            p.setTransportCharges(parseDouble(transportChargesField.getText()));
            p.setOtherCharges(parseDouble(otherChargesField.getText()));
            p.setNetAmount(parseDouble(netAmountField.getText()));
            p.setRemarks(remarksArea.getText());

            purchaseService.addPurchase(p);
            showAlert(LanguageManager.get("msg.save.success"));
            handleClear();
            loadPurchases();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error saving purchase: " + e.getMessage());
        }
    }

    @FXML
    private void handleDelete() {
        Purchase selected = purchaseTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(LanguageManager.get("btn.delete"));
        alert.setHeaderText(null);
        alert.setContentText(LanguageManager.get("msg.delete.confirm"));

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                purchaseService.deletePurchase(selected.getId());
                showAlert(LanguageManager.get("msg.delete.success"));
                loadPurchases();
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert("Cannot delete purchase.");
            }
        }
    }

    @FXML
    private void handleClear() {
        farmerComboBox.getSelectionModel().clearSelection();
        purchaseDatePicker.setValue(LocalDate.now());
        caneTypeField.clear();
        vehicleNoField.clear();
        weightField.setText("0.0");
        rateField.setText("0.0");
        totalAmountField.setText("0.0");
        advanceField.setText("0.0");
        loadingChargesField.setText("0.0");
        cuttingChargesField.setText("0.0");
        transportChargesField.setText("0.0");
        otherChargesField.setText("0.0");
        netAmountField.setText("0.0");
        remarksArea.clear();
        purchaseTable.getSelectionModel().clearSelection();
    }

    private boolean validateForm() {
        if (farmerComboBox.getValue() == null) {
            showAlert("Please select a Farmer.");
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

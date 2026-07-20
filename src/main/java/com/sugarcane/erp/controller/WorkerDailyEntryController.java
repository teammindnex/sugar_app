package com.sugarcane.erp.controller;

import com.sugarcane.erp.model.Worker;
import com.sugarcane.erp.model.WorkerDailyEntry;
import com.sugarcane.erp.service.WorkerService;
import com.sugarcane.erp.service.WorkerDailyEntryService;
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

public class WorkerDailyEntryController {

    @FXML private Label titleLabel;
    
    // Table components
    @FXML private TableView<WorkerDailyEntry> entryTable;
    @FXML private TableColumn<WorkerDailyEntry, String> colDate;
    @FXML private TableColumn<WorkerDailyEntry, String> colWorker;
    @FXML private TableColumn<WorkerDailyEntry, String> colAttendance;
    @FXML private TableColumn<WorkerDailyEntry, String> colBundles;
    @FXML private TableColumn<WorkerDailyEntry, String> colNetSalary;
    
    // Form fields
    @FXML private ComboBox<Worker> workerComboBox;
    @FXML private DatePicker entryDatePicker;
    @FXML private ComboBox<String> attendanceComboBox;
    
    @FXML private TextField bundlesField;
    @FXML private TextField rateField;
    @FXML private TextField totalSalaryField;
    @FXML private TextField bonusField;
    @FXML private TextField advanceField;
    @FXML private TextField penaltyField;
    
    @FXML private TextField teaExpenseField;
    @FXML private TextField foodExpenseField;
    @FXML private TextField otherExpenseField;
    
    @FXML private TextField netSalaryField;
    
    // Buttons
    @FXML private Button btnSave;
    @FXML private Button btnDelete;
    @FXML private Button btnClear;

    private WorkerDailyEntryService entryService;
    private WorkerService workerService;
    private ObservableList<WorkerDailyEntry> entryList = FXCollections.observableArrayList();
    private ObservableList<Worker> workerList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        entryService = new WorkerDailyEntryService();
        workerService = new WorkerService();
        
        // i18n Bindings
        titleLabel.textProperty().bind(LanguageManager.createStringBinding("workerEntry.title"));
        colDate.textProperty().bind(LanguageManager.createStringBinding("field.date"));
        colWorker.textProperty().bind(LanguageManager.createStringBinding("menu.workers"));
        colAttendance.textProperty().bind(LanguageManager.createStringBinding("workerEntry.attendance"));
        colBundles.textProperty().bind(LanguageManager.createStringBinding("workerEntry.bundles"));
        colNetSalary.textProperty().bind(LanguageManager.createStringBinding("workerEntry.netSalary"));
        
        btnSave.textProperty().bind(LanguageManager.createStringBinding("btn.save"));
        btnDelete.textProperty().bind(LanguageManager.createStringBinding("btn.delete"));
        btnClear.textProperty().bind(LanguageManager.createStringBinding("btn.clear"));

        // Configure Table Columns
        colDate.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getEntryDate().toString()));
        colWorker.setCellValueFactory(new PropertyValueFactory<>("workerName"));
        colAttendance.setCellValueFactory(new PropertyValueFactory<>("attendance"));
        colBundles.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getBundles())));
        colNetSalary.setCellValueFactory(cellData -> new SimpleStringProperty(String.format("%.2f", cellData.getValue().getNetSalary())));

        // ComboBox setup
        workerComboBox.setConverter(new StringConverter<Worker>() {
            @Override
            public String toString(Worker w) {
                return w != null ? w.getName() : "";
            }
            @Override
            public Worker fromString(String string) { return null; }
        });
        
        attendanceComboBox.getItems().addAll("PRESENT", "ABSENT", "HALF_DAY");
        attendanceComboBox.setValue("PRESENT");

        // Add calculation listeners
        bundlesField.textProperty().addListener((obs, oldV, newV) -> calculate());
        rateField.textProperty().addListener((obs, oldV, newV) -> calculate());
        bonusField.textProperty().addListener((obs, oldV, newV) -> calculate());
        advanceField.textProperty().addListener((obs, oldV, newV) -> calculate());
        penaltyField.textProperty().addListener((obs, oldV, newV) -> calculate());
        teaExpenseField.textProperty().addListener((obs, oldV, newV) -> calculate());
        foodExpenseField.textProperty().addListener((obs, oldV, newV) -> calculate());
        otherExpenseField.textProperty().addListener((obs, oldV, newV) -> calculate());
        attendanceComboBox.valueProperty().addListener((obs, oldV, newV) -> calculate());

        loadWorkers();
        loadEntries();
        handleClear();
    }

    private void loadWorkers() {
        try {
            workerList.clear();
            workerList.addAll(workerService.getAllWorkers());
            workerComboBox.setItems(workerList);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadEntries() {
        try {
            entryList.clear();
            entryList.addAll(entryService.getAllEntries());
            entryTable.setItems(entryList);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void calculate() {
        try {
            if ("ABSENT".equals(attendanceComboBox.getValue())) {
                bundlesField.setText("0");
                totalSalaryField.setText("0.0");
                netSalaryField.setText("0.0");
                return;
            }

            int bundles = parseInteger(bundlesField.getText());
            double rate = parseDouble(rateField.getText());
            double total = bundles * rate;
            
            if ("HALF_DAY".equals(attendanceComboBox.getValue())) {
                total = total / 2.0; // Assuming half day halves the calculated or fixed salary
            }
            
            totalSalaryField.setText(String.format("%.2f", total));

            double bonus = parseDouble(bonusField.getText());
            double advance = parseDouble(advanceField.getText());
            double penalty = parseDouble(penaltyField.getText());
            double tea = parseDouble(teaExpenseField.getText());
            double food = parseDouble(foodExpenseField.getText());
            double other = parseDouble(otherExpenseField.getText());

            double net = total + bonus - advance - penalty - tea - food - other;
            netSalaryField.setText(String.format("%.2f", net));
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
    
    private int parseInteger(String str) {
        if (str == null || str.trim().isEmpty()) return 0;
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    @FXML
    private void handleSave() {
        if (!validateForm()) return;
        
        try {
            WorkerDailyEntry e = new WorkerDailyEntry();
            e.setWorkerId(workerComboBox.getValue().getId());
            e.setEntryDate(entryDatePicker.getValue());
            e.setAttendance(attendanceComboBox.getValue());
            e.setBundles(parseInteger(bundlesField.getText()));
            e.setRatePerBundle(parseDouble(rateField.getText()));
            e.setTotalSalary(parseDouble(totalSalaryField.getText()));
            e.setBonus(parseDouble(bonusField.getText()));
            e.setAdvance(parseDouble(advanceField.getText()));
            e.setPenalty(parseDouble(penaltyField.getText()));
            e.setTeaExpense(parseDouble(teaExpenseField.getText()));
            e.setFoodExpense(parseDouble(foodExpenseField.getText()));
            e.setOtherExpense(parseDouble(otherExpenseField.getText()));
            e.setNetSalary(parseDouble(netSalaryField.getText()));

            entryService.addEntry(e);
            showAlert(LanguageManager.get("msg.save.success"));
            handleClear();
            loadEntries();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error saving entry: " + e.getMessage());
        }
    }

    @FXML
    private void handleDelete() {
        WorkerDailyEntry selected = entryTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(LanguageManager.get("btn.delete"));
        alert.setHeaderText(null);
        alert.setContentText(LanguageManager.get("msg.delete.confirm"));

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                entryService.deleteEntry(selected.getId());
                showAlert(LanguageManager.get("msg.delete.success"));
                loadEntries();
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert("Cannot delete entry.");
            }
        }
    }

    @FXML
    private void handleClear() {
        workerComboBox.getSelectionModel().clearSelection();
        entryDatePicker.setValue(LocalDate.now());
        attendanceComboBox.setValue("PRESENT");
        bundlesField.setText("0");
        rateField.setText("0.0");
        totalSalaryField.setText("0.0");
        bonusField.setText("0.0");
        advanceField.setText("0.0");
        penaltyField.setText("0.0");
        teaExpenseField.setText("0.0");
        foodExpenseField.setText("0.0");
        otherExpenseField.setText("0.0");
        netSalaryField.setText("0.0");
        entryTable.getSelectionModel().clearSelection();
    }

    private boolean validateForm() {
        if (workerComboBox.getValue() == null) {
            showAlert("Please select a Worker.");
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

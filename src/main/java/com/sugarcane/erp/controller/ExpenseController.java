package com.sugarcane.erp.controller;

import com.sugarcane.erp.model.Expense;
import com.sugarcane.erp.service.ExpenseService;
import com.sugarcane.erp.utils.LanguageManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Optional;

public class ExpenseController {

    @FXML private Label titleLabel;
    
    // Table components
    @FXML private TableView<Expense> expenseTable;
    @FXML private TableColumn<Expense, String> colDate;
    @FXML private TableColumn<Expense, String> colCategory;
    @FXML private TableColumn<Expense, String> colAmount;
    @FXML private TableColumn<Expense, String> colMode;
    
    // Form fields
    @FXML private DatePicker expenseDatePicker;
    @FXML private ComboBox<String> categoryComboBox;
    @FXML private TextField amountField;
    @FXML private ComboBox<String> paymentModeComboBox;
    @FXML private TextArea descriptionArea;
    
    // Buttons
    @FXML private Button btnSave;
    @FXML private Button btnDelete;
    @FXML private Button btnClear;

    private ExpenseService expenseService;
    private ObservableList<Expense> expenseList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        expenseService = new ExpenseService();
        
        // i18n Bindings
        titleLabel.textProperty().bind(LanguageManager.createStringBinding("expense.title"));
        colDate.textProperty().bind(LanguageManager.createStringBinding("field.date"));
        colCategory.textProperty().bind(LanguageManager.createStringBinding("expense.category"));
        colAmount.textProperty().bind(LanguageManager.createStringBinding("expense.amount"));
        colMode.textProperty().bind(LanguageManager.createStringBinding("expense.mode"));
        
        btnSave.textProperty().bind(LanguageManager.createStringBinding("btn.save"));
        btnDelete.textProperty().bind(LanguageManager.createStringBinding("btn.delete"));
        btnClear.textProperty().bind(LanguageManager.createStringBinding("btn.clear"));

        // Configure Table Columns
        colDate.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getExpenseDate().toString()));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colAmount.setCellValueFactory(cellData -> new SimpleStringProperty(String.format("%.2f", cellData.getValue().getAmount())));
        colMode.setCellValueFactory(new PropertyValueFactory<>("paymentMode"));

        // ComboBox setup
        categoryComboBox.getItems().addAll("Diesel", "Tea/Coffee", "Food", "Office Supplies", "Repair & Maintenance", "Other");
        paymentModeComboBox.getItems().addAll("Cash", "Bank Transfer", "UPI");

        loadExpenses();
        handleClear();
    }

    private void loadExpenses() {
        try {
            expenseList.clear();
            expenseList.addAll(expenseService.getAllExpenses());
            expenseTable.setItems(expenseList);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSave() {
        if (!validateForm()) return;
        
        try {
            Expense e = new Expense();
            e.setExpenseDate(expenseDatePicker.getValue());
            e.setCategory(categoryComboBox.getValue());
            e.setAmount(Double.parseDouble(amountField.getText()));
            e.setPaymentMode(paymentModeComboBox.getValue());
            e.setDescription(descriptionArea.getText());

            expenseService.addExpense(e);
            showAlert(LanguageManager.get("msg.save.success"));
            handleClear();
            loadExpenses();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error saving expense: " + e.getMessage());
        }
    }

    @FXML
    private void handleDelete() {
        Expense selected = expenseTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(LanguageManager.get("btn.delete"));
        alert.setHeaderText(null);
        alert.setContentText(LanguageManager.get("msg.delete.confirm"));

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                expenseService.deleteExpense(selected.getId());
                showAlert(LanguageManager.get("msg.delete.success"));
                loadExpenses();
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert("Cannot delete expense.");
            }
        }
    }

    @FXML
    private void handleClear() {
        expenseDatePicker.setValue(LocalDate.now());
        categoryComboBox.getSelectionModel().clearSelection();
        paymentModeComboBox.getSelectionModel().clearSelection();
        amountField.setText("0.0");
        descriptionArea.clear();
        expenseTable.getSelectionModel().clearSelection();
    }

    private boolean validateForm() {
        if (categoryComboBox.getValue() == null) {
            showAlert("Please select a Category.");
            return false;
        }
        try {
            double amt = Double.parseDouble(amountField.getText());
            if (amt <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            showAlert("Please enter a valid amount greater than 0.");
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

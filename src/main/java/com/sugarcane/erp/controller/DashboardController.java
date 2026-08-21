package com.sugarcane.erp.controller;

import com.sugarcane.erp.MainApplication;
import com.sugarcane.erp.service.AuthService;
import com.sugarcane.erp.utils.LanguageManager;
import com.sugarcane.erp.utils.ThemeManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.Locale;

public class DashboardController {

    @FXML private BorderPane root;
    @FXML private StackPane contentArea;
    @FXML private VBox sidebarVBox;

    @FXML private Label todayPurchaseLabel;
    @FXML private Label todaySalesLabel;
    @FXML private Label todayExpensesLabel;
    @FXML private Label todayPurchaseWeightLabel;
    @FXML private Label todaySalesWeightLabel;
    @FXML private Label todayProfitLabel;
    @FXML private VBox profitCard;
    @FXML private Label profitTitleLabel;
    @FXML private Label profitSubTitleLabel;

    @FXML private Label monthlyPurchaseWeightLabel;
    @FXML private Label monthlySalesWeightLabel;
    @FXML private Label yearlyPurchaseWeightLabel;
    @FXML private Label yearlySalesWeightLabel;
    @FXML private javafx.scene.control.ComboBox<String> sugarcaneMonthPicker;
    private java.util.List<java.time.LocalDate> monthDates = new java.util.ArrayList<>();

    private Node homeNode;
    private com.sugarcane.erp.service.DashboardService dashboardService;

    @FXML
    public void initialize() {
        dashboardService = new com.sugarcane.erp.service.DashboardService();
        
        // Save the original home content
        if (!contentArea.getChildren().isEmpty()) {
            homeNode = contentArea.getChildren().get(0);
        }
        
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("MMM yyyy");
        for (int i = 0; i < 12; i++) {
            java.time.LocalDate d = java.time.LocalDate.now().minusMonths(i);
            monthDates.add(d);
            sugarcaneMonthPicker.getItems().add(d.format(formatter));
        }
        sugarcaneMonthPicker.getSelectionModel().select(0);
        
        loadDashboardStats();
    }
    
    @FXML
    private void loadDashboardStats() {
        try {
            java.time.LocalDate selectedDate = java.time.LocalDate.now();
            if (sugarcaneMonthPicker != null && sugarcaneMonthPicker.getSelectionModel().getSelectedIndex() >= 0) {
                selectedDate = monthDates.get(sugarcaneMonthPicker.getSelectionModel().getSelectedIndex());
            }
            com.sugarcane.erp.model.DashboardMetrics metrics = dashboardService.getMetrics(selectedDate);
            
            todayPurchaseLabel.setText(String.format("₹ %.2f", metrics.getTodayPurchase()));
            todaySalesLabel.setText(String.format("₹ %.2f", metrics.getTodaySales()));
            todayExpensesLabel.setText(String.format("₹ %.2f", metrics.getTodayExpenses()));
            if (todayPurchaseWeightLabel != null) {
                todayPurchaseWeightLabel.setText(String.format("%.2f टन", metrics.getTodayPurchaseWeight()));
            }
            if (todaySalesWeightLabel != null) {
                todaySalesWeightLabel.setText(String.format("%.2f टन", metrics.getTodaySalesWeight()));
            }
            
            double profit = metrics.getTodaySales() - metrics.getTodayPurchase() - metrics.getTodayExpenses();
            
            if (profitCard != null && profitTitleLabel != null && profitSubTitleLabel != null) {
                profitCard.getStyleClass().remove("card-red");
                if (!profitCard.getStyleClass().contains("card-green")) profitCard.getStyleClass().add("card-green");
                profitTitleLabel.setText("आजचा नफा");
                profitSubTitleLabel.setText("Today's Profit");
                todayProfitLabel.setText(String.format("₹ %.2f", profit));
            } else {
                todayProfitLabel.setText(String.format("₹ %.2f", profit));
            }
            
            if (monthlyPurchaseWeightLabel != null) {
                monthlyPurchaseWeightLabel.setText(String.format("%.2f टन", metrics.getMonthlyPurchaseWeight()));
            }
            if (monthlySalesWeightLabel != null) {
                monthlySalesWeightLabel.setText(String.format("%.2f टन", metrics.getMonthlySalesWeight()));
            }
            if (yearlyPurchaseWeightLabel != null) {
                yearlyPurchaseWeightLabel.setText(String.format("%.2f टन", metrics.getYearlyPurchaseWeight()));
            }
            if (yearlySalesWeightLabel != null) {
                yearlySalesWeightLabel.setText(String.format("%.2f टन", metrics.getYearlySalesWeight()));
            }
            
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
    }

    private void setActiveButton(javafx.event.ActionEvent event) {
        if (sidebarVBox == null || event == null) return;
        for (Node node : sidebarVBox.getChildren()) {
            if (node instanceof javafx.scene.control.Button) {
                node.getStyleClass().remove("active");
            }
        }
        if (event.getSource() instanceof javafx.scene.control.Button) {
            ((javafx.scene.control.Button) event.getSource()).getStyleClass().add("active");
        }
    }

    @FXML
    private void showDashboardHome(javafx.event.ActionEvent event) {
        setActiveButton(event);
        contentArea.getChildren().clear();
        if (homeNode != null) {
            contentArea.getChildren().add(homeNode);
        }
        loadDashboardStats();
    }

    @FXML
    private void loadDashboardStatsAction() {
        loadDashboardStats();
    }

    @FXML
    private void showFarmer(javafx.event.ActionEvent event) {
        setActiveButton(event);
        loadView("/view/farmer.fxml");
    }

    @FXML
    private void showCustomer(javafx.event.ActionEvent event) {
        setActiveButton(event);
        loadView("/view/customer.fxml");
    }

    @FXML
    private void showWorker(javafx.event.ActionEvent event) {
        setActiveButton(event);
        loadView("/view/worker.fxml");
    }

    @FXML
    private void showReports(javafx.event.ActionEvent event) {
        setActiveButton(event);
        loadView("/view/reports.fxml");
    }

    @FXML
    private void showTransport(javafx.event.ActionEvent event) {
        setActiveButton(event);
        loadView("/view/transport.fxml");
    }

    @FXML
    private void showSettings(javafx.event.ActionEvent event) {
        setActiveButton(event);
        loadView("/view/settings.fxml");
    }

    private void loadView(String fxml) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(fxml));
            fxmlLoader.setResources(java.util.ResourceBundle.getBundle("i18n.messages", LanguageManager.getLocale()));
            Node view = fxmlLoader.load();
            contentArea.getChildren().clear();
            contentArea.getChildren().add(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void switchEnglish() {
        LanguageManager.setLocale(new Locale("en"));
        // Need to reload current view to apply language if not fully bound, 
        // but for now bindings will handle some, and we can force reload.
        try {
            MainApplication.setRoot("/view/dashboard.fxml", root.getWidth(), root.getHeight());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void switchMarathi() {
        LanguageManager.setLocale(new Locale("mr"));
        try {
            MainApplication.setRoot("/view/dashboard.fxml", root.getWidth(), root.getHeight());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void toggleTheme() {
        String newTheme = ThemeManager.getTheme().equals(ThemeManager.LIGHT_THEME) ? 
                          ThemeManager.DARK_THEME : ThemeManager.LIGHT_THEME;
        ThemeManager.setTheme(newTheme);
        ThemeManager.applyTheme(root.getScene());
    }

    @FXML
    private void handleLogout() {
        AuthService.logout();
        try {
            MainApplication.setRoot("/view/login.fxml", 400, 500);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

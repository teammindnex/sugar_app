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

    @FXML private Label todayPurchaseLabel;
    @FXML private Label todaySalesLabel;
    @FXML private Label todayExpensesLabel;
    @FXML private Label todayCollectionLabel;
    @FXML private Label todayPaymentsLabel;
    @FXML private Label todayProfitLabel;
    @FXML private Label totalFarmersLabel;
    @FXML private Label totalCustomersLabel;
    @FXML private Label totalVehiclesLabel;

    private Node homeNode;
    private com.sugarcane.erp.service.DashboardService dashboardService;

    @FXML
    public void initialize() {
        dashboardService = new com.sugarcane.erp.service.DashboardService();
        
        // Save the original home content
        if (!contentArea.getChildren().isEmpty()) {
            homeNode = contentArea.getChildren().get(0);
        }
        
        loadDashboardStats();
    }
    
    private void loadDashboardStats() {
        try {
            com.sugarcane.erp.model.DashboardMetrics metrics = dashboardService.getMetrics();
            
            todayPurchaseLabel.setText(String.format("₹ %.2f", metrics.getTodayPurchase()));
            todaySalesLabel.setText(String.format("₹ %.2f", metrics.getTodaySales()));
            todayExpensesLabel.setText(String.format("₹ %.2f", metrics.getTodayExpenses()));
            todayCollectionLabel.setText(String.format("₹ %.2f", metrics.getTodayCollection()));
            todayPaymentsLabel.setText(String.format("₹ %.2f", metrics.getTodayPayments()));
            
            double profit = metrics.getTodaySales() - metrics.getTodayPurchase() - metrics.getTodayExpenses();
            todayProfitLabel.setText(String.format("₹ %.2f", profit));
            
            totalFarmersLabel.setText(String.valueOf(metrics.getTotalFarmers()));
            totalCustomersLabel.setText(String.valueOf(metrics.getTotalCustomers()));
            totalVehiclesLabel.setText(String.valueOf(metrics.getTotalVehicles()));
            
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void showDashboardHome() {
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
    private void showFarmer() {
        loadView("/view/farmer.fxml");
    }

    @FXML
    private void showCustomer() {
        loadView("/view/customer.fxml");
    }

    @FXML
    private void showWorker() {
        loadView("/view/worker.fxml");
    }

    @FXML
    private void showTransport() {
        loadView("/view/transport.fxml");
    }

    @FXML
    private void showSettings() {
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

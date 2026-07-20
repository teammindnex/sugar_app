package com.sugarcane.erp;

import com.sugarcane.erp.utils.DatabaseManager;
import com.sugarcane.erp.utils.LanguageManager;
import com.sugarcane.erp.utils.ThemeManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApplication extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;
        
        // Initialize DB
        DatabaseManager.getInstance();
        
        // Load settings
        try {
            com.sugarcane.erp.service.SettingService settingService = new com.sugarcane.erp.service.SettingService();
            String lang = settingService.getSetting("LANGUAGE");
            if (lang != null && !lang.isEmpty()) {
                LanguageManager.setLocale(new java.util.Locale(lang));
            }
            // Theme is fixed to Light Theme
            ThemeManager.setTheme(ThemeManager.LIGHT_THEME);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Load Login Screen
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("/view/login.fxml"));
        // Set ResourceBundle for initial load
        fxmlLoader.setResources(java.util.ResourceBundle.getBundle("i18n.messages", LanguageManager.getLocale()));
        
        Parent root = fxmlLoader.load();
        Scene scene = new Scene(root, 400, 500);
        
        ThemeManager.applyTheme(scene);
        
        stage.setTitle("Shree Ganesh Krupa Uss Suppliers ERP");
        stage.setScene(scene);
        stage.setResizable(true);
        stage.show();
    }

    public static void setRoot(String fxml, double width, double height) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource(fxml));
        fxmlLoader.setResources(java.util.ResourceBundle.getBundle("i18n.messages", LanguageManager.getLocale()));
        Parent root = fxmlLoader.load();
        
        Scene scene = new Scene(root, width, height);
        ThemeManager.applyTheme(scene);
        
        primaryStage.setScene(scene);
        
        if (fxml.contains("dashboard")) {
            primaryStage.setMaximized(true);
        } else {
            primaryStage.setMaximized(false);
            primaryStage.centerOnScreen();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}

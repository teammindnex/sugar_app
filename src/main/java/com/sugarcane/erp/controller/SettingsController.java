package com.sugarcane.erp.controller;

import com.sugarcane.erp.service.SettingService;
import com.sugarcane.erp.utils.DatabaseManager;
import com.sugarcane.erp.utils.LanguageManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;

public class SettingsController {

    @FXML private TextField companyNameField;
    @FXML private ComboBox<String> languageComboBox;
    @FXML private ComboBox<String> themeComboBox;

    private SettingService settingService;

    @FXML
    public void initialize() {
        settingService = new SettingService();
        
        languageComboBox.getItems().addAll("English", "Marathi");
        themeComboBox.getItems().addAll("Light", "Dark");
        
        loadSettings();
    }

    private void loadSettings() {
        try {
            String company = settingService.getSetting("COMPANY_NAME");
            String lang = settingService.getSetting("LANGUAGE");
            String theme = settingService.getSetting("THEME");
            
            companyNameField.setText(company != null ? company : "श्री गणेश कृपा ऊस सप्लायर्स");
            languageComboBox.setValue("mr".equals(lang) ? "Marathi" : "English");
            themeComboBox.setValue("dark".equals(theme) ? "Dark" : "Light");
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSave() {
        try {
            settingService.updateSetting("COMPANY_NAME", companyNameField.getText());
            settingService.updateSetting("LANGUAGE", "Marathi".equals(languageComboBox.getValue()) ? "mr" : "en");
            settingService.updateSetting("THEME", "Dark".equals(themeComboBox.getValue()) ? "dark" : "light");
            
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setHeaderText(null);
            alert.setContentText("Settings saved successfully! Please restart the application for language and theme changes to take effect.");
            alert.showAndWait();
            
        } catch (SQLException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText(null);
            alert.setContentText("Failed to save settings: " + e.getMessage());
            alert.showAndWait();
        }
    }
    
    @FXML
    private void handleClearData() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("चेतावणी (Warning)");
        alert.setHeaderText("तुम्ही सर्व डेटा डिलीट करत आहात!");
        alert.setContentText("तुम्हाला खात्री आहे का की तुम्हाला संपूर्ण डेटा (शेतकरी, ग्राहक, खरेदी, विक्री इ.) कायमचा डिलीट करायचा आहे? ही कृती परत घेता येणार नाही.");
        
        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                DatabaseManager.getInstance().clearAllData();
                Alert success = new Alert(Alert.AlertType.INFORMATION);
                success.setHeaderText(null);
                success.setContentText("सर्व डेटा यशस्वीरित्या डिलीट झाला आहे.");
                success.showAndWait();
            } catch (SQLException e) {
                e.printStackTrace();
                Alert error = new Alert(Alert.AlertType.ERROR);
                error.setHeaderText(null);
                error.setContentText("डेटा डिलीट करताना त्रुटी आली: " + e.getMessage());
                error.showAndWait();
            }
        }
    }

    @FXML
    private void handleBackup() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Database Backup");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("SQLite Database", "*.db"));
        fileChooser.setInitialFileName("sugarcane_erp_backup.db");
        
        File dest = fileChooser.showSaveDialog(companyNameField.getScene().getWindow());
        if (dest != null) {
            try {
                // The actual DB file is usually in the "db" directory relative to execution
                File source = new File("db/sugarcane_erp.db");
                if (source.exists()) {
                    Files.copy(source.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setHeaderText(null);
                    alert.setContentText("Backup saved successfully to " + dest.getAbsolutePath());
                    alert.showAndWait();
                } else {
                    throw new IOException("Source database file not found at " + source.getAbsolutePath());
                }
            } catch (IOException e) {
                e.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setHeaderText("Backup Failed");
                alert.setContentText(e.getMessage());
                alert.showAndWait();
            }
        }
    }

    @FXML
    private void handleRestore() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Database Backup to Restore");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("SQLite Database", "*.db"));
        
        File source = fileChooser.showOpenDialog(companyNameField.getScene().getWindow());
        if (source != null) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Restore Database");
            confirm.setHeaderText("Warning: This will overwrite your current data!");
            confirm.setContentText("Are you sure you want to restore from " + source.getName() + "? You must restart the application after this.");
            
            if (confirm.showAndWait().get() == ButtonType.OK) {
                try {
                    File dest = new File("db/sugarcane_erp.db");
                    
                    // The SQLite connections are auto-closed in DAOs, so we can just copy.
                    Files.copy(source.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setHeaderText(null);
                    alert.setContentText("Database restored successfully! The application will now exit. Please start it again.");
                    alert.showAndWait();
                    
                    System.exit(0);
                    
                } catch (Exception e) {
                    e.printStackTrace();
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setHeaderText("Restore Failed");
                    alert.setContentText(e.getMessage());
                    alert.showAndWait();
                }
            }
        }
    }
}

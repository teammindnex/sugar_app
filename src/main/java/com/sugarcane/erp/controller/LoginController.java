package com.sugarcane.erp.controller;

import com.sugarcane.erp.MainApplication;
import com.sugarcane.erp.service.AuthService;
import com.sugarcane.erp.utils.LanguageManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.io.IOException;

import javafx.scene.layout.StackPane;

public class LoginController {

    @FXML private StackPane rootPane;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    @FXML private Label titleLabel;

    private AuthService authService;

    @FXML
    public void initialize() {
        authService = new AuthService();
        // Dynamic language binding
        titleLabel.textProperty().bind(LanguageManager.createStringBinding("admin.login"));
        usernameField.promptTextProperty().bind(LanguageManager.createStringBinding("username"));
        passwordField.promptTextProperty().bind(LanguageManager.createStringBinding("password"));
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (authService.login(username, password) || authService.loginWithPin(username, password)) {
            errorLabel.setVisible(false);
            try {
                // Navigate to dashboard
                MainApplication.setRoot("/view/dashboard.fxml", 1024, 768);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            errorLabel.textProperty().bind(LanguageManager.createStringBinding("login.error"));
            errorLabel.setVisible(true);
        }
    }
}

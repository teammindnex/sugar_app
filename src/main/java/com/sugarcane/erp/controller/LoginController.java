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

    @FXML private javafx.scene.control.Hyperlink forgotPasswordLink;

    private AuthService authService;

    @FXML
    public void initialize() {
        authService = new AuthService();
        // Dynamic language binding
        titleLabel.textProperty().bind(LanguageManager.createStringBinding("admin.login"));
        usernameField.promptTextProperty().bind(LanguageManager.createStringBinding("username"));
        passwordField.promptTextProperty().bind(LanguageManager.createStringBinding("password"));
        if (forgotPasswordLink != null) {
            forgotPasswordLink.textProperty().bind(LanguageManager.createStringBinding("forgot.password"));
        }
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

    @FXML
    private void handleForgotPassword() {
        javafx.scene.control.Dialog<javafx.scene.control.ButtonType> dialog = new javafx.scene.control.Dialog<>();
        dialog.setTitle("पासवर्ड रीसेट करा (Forgot Password)");
        dialog.setHeaderText("नवीन पासवर्ड सेट करण्यासाठी वापरकर्तानाव आणि सुरक्षा पिन टाका");
        
        if (rootPane != null && rootPane.getScene() != null && rootPane.getScene().getWindow() != null) {
            dialog.initOwner(rootPane.getScene().getWindow());
            dialog.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        }

        javafx.scene.control.ButtonType resetBtnType = new javafx.scene.control.ButtonType("पासवर्ड बदला (Reset Password)", javafx.scene.control.ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(resetBtnType, javafx.scene.control.ButtonType.CANCEL);

        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new javafx.geometry.Insets(20, 20, 20, 20));

        TextField userField = new TextField();
        String currentU = usernameField.getText() != null && !usernameField.getText().trim().isEmpty() ? usernameField.getText().trim() : "admin";
        userField.setText(currentU);

        PasswordField pinField = new PasswordField();
        pinField.setPromptText("सुरक्षा पिन (उदा. 1234)");

        PasswordField newPassField = new PasswordField();
        newPassField.setPromptText("नवीन पासवर्ड");

        PasswordField confirmPassField = new PasswordField();
        confirmPassField.setPromptText("पासवर्डची पुष्टी करा");

        grid.add(new Label("वापरकर्ता नाव (Username):"), 0, 0);
        grid.add(userField, 1, 0);

        grid.add(new Label("सुरक्षा पिन (Security PIN):"), 0, 1);
        grid.add(pinField, 1, 1);

        grid.add(new Label("नवीन पासवर्ड (New Password):"), 0, 2);
        grid.add(newPassField, 1, 2);

        grid.add(new Label("पुष्टी करा (Confirm Password):"), 0, 3);
        grid.add(confirmPassField, 1, 3);

        Label hintLabel = new Label("💡 टीप: डीफॉल्ट सुरक्षा पिन '1234' आहे.");
        hintLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #666666; -fx-font-style: italic;");
        grid.add(hintLabel, 1, 4);

        dialog.getDialogPane().setContent(grid);

        javafx.scene.Node resetBtn = dialog.getDialogPane().lookupButton(resetBtnType);
        resetBtn.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            String u = userField.getText().trim();
            String pin = pinField.getText().trim();
            String newP = newPassField.getText();
            String confP = confirmPassField.getText();

            if (u.isEmpty()) {
                event.consume();
                showError("कृपया वापरकर्ता नाव टाका.");
                return;
            }
            if (pin.isEmpty()) {
                event.consume();
                showError("कृपया सुरक्षा पिन टाका.");
                return;
            }
            if (newP.isEmpty()) {
                event.consume();
                showError("कृपया नवीन पासवर्ड प्रविष्ट करा.");
                return;
            }
            if (!newP.equals(confP)) {
                event.consume();
                showError("दोन्ही पासवर्ड जुळत नाहीत! कृपया तपासा.");
                return;
            }

            boolean success = authService.resetPassword(u, pin, newP);
            if (!success) {
                event.consume();
                showError("सुरक्षा पिन चुकीचा आहे किंवा वापरकर्ता सापडला नाही!");
            }
        });

        dialog.showAndWait().ifPresent(response -> {
            if (response == resetBtnType) {
                usernameField.setText(userField.getText().trim());
                passwordField.setText(newPassField.getText());
                
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
                alert.setTitle("यशस्वी (Success)");
                alert.setHeaderText(null);
                alert.setContentText("पासवर्ड यशस्वीरित्या बदलला आहे!\nआता 'Login' बटनावर क्लिक करा.");
                if (rootPane != null && rootPane.getScene() != null && rootPane.getScene().getWindow() != null) {
                    alert.initOwner(rootPane.getScene().getWindow());
                    alert.initModality(javafx.stage.Modality.APPLICATION_MODAL);
                }
                alert.showAndWait();
            }
        });
    }

    private void showError(String msg) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.WARNING);
        alert.setTitle("माहिती (Warning)");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        if (rootPane != null && rootPane.getScene() != null && rootPane.getScene().getWindow() != null) {
            alert.initOwner(rootPane.getScene().getWindow());
            alert.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        }
        alert.showAndWait();
    }
}

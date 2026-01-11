package io.smartpos.ui.auth;

import io.smartpos.services.auth.AuthService;
import io.smartpos.ui.config.ServiceFactory;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {

    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label errorLabel;

    private final AuthService authService;

    public LoginController() {
        this.authService = ServiceFactory.authService();
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        try {
            authService.login(username, password);
            navigateToMain();
        } catch (Exception e) {
            errorLabel.setText(e.getMessage());
        }
    }

    private void navigateToMain() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/views/main-layout.fxml"));
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setTitle("SmartPOS - Enterprise Edition");
            stage.setScene(new Scene(root, 1280, 800));
            stage.setResizable(true);
            stage.centerOnScreen();
        } catch (Exception e) {
            e.printStackTrace();
            errorLabel.setText("Navigation error: " + e.getMessage());
        }
    }
}

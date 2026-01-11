package io.smartpos.ui.main;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.net.URL;

public class MainLayoutController {

    @FXML
    private StackPane contentArea;
    @FXML
    private Label pageTitle;

    @FXML
    private Button btnSale;
    @FXML
    private Button btnHistory;
    @FXML
    private Button btnProducts;
    @FXML
    private Button btnPurchases;
    @FXML
    private Button btnInventory;
    @FXML
    private Button btnCatalogs;
    @FXML
    private Button btnCash;
    @FXML
    private Button btnSettings;

    @FXML
    private Label userNameLabel;
    @FXML
    private Label userRoleLabel;

    @FXML
    public void initialize() {
        setupUserSession();
        // Default View
        showSale();
    }

    private void setupUserSession() {
        io.smartpos.services.auth.AuthService auth = io.smartpos.ui.config.ServiceFactory.authService();
        if (auth.isAuthenticated()) {
            io.smartpos.core.domain.user.User user = auth.getCurrentUser();
            userNameLabel.setText(user.getUsername());
            userRoleLabel.setText(user.getRole());

            // Simple role based access
            boolean isAdmin = auth.hasRole("ADMIN");
            btnInventory.setVisible(isAdmin);
            btnInventory.setManaged(isAdmin);
            btnCatalogs.setVisible(isAdmin);
            btnCatalogs.setManaged(isAdmin);
            btnPurchases.setVisible(isAdmin);
            btnPurchases.setManaged(isAdmin);
            btnSettings.setVisible(isAdmin);
            btnSettings.setManaged(isAdmin);
        }
    }

    @FXML
    private void handleLogout() {
        io.smartpos.ui.config.ServiceFactory.authService().logout();
        try {
            javafx.scene.Parent root = javafx.fxml.FXMLLoader.load(getClass().getResource("/views/login-view.fxml"));
            javafx.stage.Stage stage = (javafx.stage.Stage) userNameLabel.getScene().getWindow();
            stage.setTitle("SmartPOS - Login");
            stage.setScene(new javafx.scene.Scene(root, 800, 600));
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void showSale() {
        loadView("/views/sale-view.fxml", "Point of Sale");
        setActive(btnSale);
    }

    @FXML
    private void showHistory() {
        loadView("/views/sale/sale-history.fxml", "Sale History");
        setActive(btnHistory);
    }

    @FXML
    private void showProducts() {
        loadView("/views/product/product-list.fxml", "Product Management");
        setActive(btnProducts);
    }

    @FXML
    private void showPurchases() {
        loadView("/views/purchase/purchase-view.fxml", "Inventory Restocking");
        setActive(btnPurchases);
    }

    @FXML
    private void showReports() {
        loadView("/views/report/reports-view.fxml", "Business Reports");
        setActive(btnInventory);
    }

    @FXML
    private void showCatalogs() {
        loadView("/views/catalog/catalog-view.fxml", "Catalog Management");
        setActive(btnCatalogs);
    }

    @FXML
    private void showCashClosure() {
        try {
            io.smartpos.services.cash.CashSessionService cashService = io.smartpos.ui.config.ServiceFactory
                    .cashSessionService();
            io.smartpos.core.domain.user.User user = io.smartpos.ui.config.ServiceFactory.authService()
                    .getCurrentUser();

            if (user == null) {
                showAlert("Error", "No user logged in.");
                return;
            }

            if (cashService.getActiveSession(user.getId()).isPresent()) {
                loadView("/views/cash/cash-closure.fxml", "Cash Closure");
            } else {
                loadView("/views/cash/cash-open.fxml", "Open Cash Session");
            }
            setActive(btnCash);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Could not open Cash Session window: " + e.getMessage());
        }
    }

    private void loadView(String fxmlPath, String title) {
        try {
            URL resource = getClass().getResource(fxmlPath);
            if (resource == null) {
                showAlert("Error", "View resource not found: " + fxmlPath);
                return;
            }

            FXMLLoader loader = new FXMLLoader(resource);
            Parent view = loader.load();

            // Special handling for Open Session to pass layout
            Object controller = loader.getController();
            if (controller instanceof io.smartpos.ui.cash.CashOpenController) {
                ((io.smartpos.ui.cash.CashOpenController) controller).setMainLayout(this);
            }

            contentArea.getChildren().setAll(view);
            pageTitle.setText(title);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Error loading view [" + fxmlPath + "]: " + e.getMessage());
        }
    }

    private void showAlert(String title, String content) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void setActive(Button button) {
        // Reset all styles
        btnSale.getStyleClass().remove("active");
        if (btnHistory != null)
            btnHistory.getStyleClass().remove("active");
        btnProducts.getStyleClass().remove("active");
        btnPurchases.getStyleClass().remove("active");
        btnInventory.getStyleClass().remove("active");
        btnCatalogs.getStyleClass().remove("active");
        if (btnCash != null)
            btnCash.getStyleClass().remove("active");
        btnSettings.getStyleClass().remove("active");

        // Set active
        if (button != null)
            button.getStyleClass().add("active");
    }
}

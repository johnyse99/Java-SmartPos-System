package io.smartpos.ui.cash;

import io.smartpos.core.domain.user.User;
import io.smartpos.services.auth.AuthService;
import io.smartpos.services.cash.CashSessionService;
import io.smartpos.ui.config.ServiceFactory;
import io.smartpos.ui.main.MainLayoutController;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;

import java.math.BigDecimal;

public class CashOpenController {

    @FXML
    private TextField txtOpeningBalance;

    private final CashSessionService cashService;
    private final AuthService authService;
    private MainLayoutController mainLayout;

    public CashOpenController() {
        this.cashService = ServiceFactory.cashSessionService();
        this.authService = ServiceFactory.authService();
    }

    public void setMainLayout(MainLayoutController mainLayout) {
        this.mainLayout = mainLayout;
    }

    @FXML
    private void handleOpenSession() {
        User user = authService.getCurrentUser();
        if (user == null)
            return;

        try {
            BigDecimal balance = new BigDecimal(txtOpeningBalance.getText());
            cashService.openSession(user.getId(), balance);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setContentText("Shift started successfully!");
            alert.showAndWait();

            // Redirect to POS
            if (mainLayout != null) {
                mainLayout.showSale();
            }

        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Invalid opening balance.");
            alert.showAndWait();
        }
    }
}

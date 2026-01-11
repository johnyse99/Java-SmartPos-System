package io.smartpos.ui.cash;

import io.smartpos.core.domain.cash.CashSession;
import io.smartpos.core.domain.user.User;
import io.smartpos.ui.config.ServiceFactory;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.util.Optional;

public class CashClosureController {

    @FXML
    private Label lblOpeningBalance;
    @FXML
    private Label lblSalesTotal;
    @FXML
    private Label lblExpectedCash;
    @FXML
    private TextField txtActualCash;
    @FXML
    private VBox resultBox;
    @FXML
    private Label lblDifference;

    private CashSession currentSession;

    public CashClosureController() {
    }

    @FXML
    public void initialize() {
        try {
            loadSessionData();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Could not load session data: " + e.getMessage());
        }
    }

    private void loadSessionData() {
        User user = ServiceFactory.authService().getCurrentUser();
        if (user == null)
            return;

        Optional<CashSession> sessionOpt = ServiceFactory.cashSessionService().getActiveSession(user.getId());
        if (sessionOpt.isPresent()) {
            this.currentSession = sessionOpt.get();
            BigDecimal sales = ServiceFactory.cashSessionService().calculateSalesInSession(currentSession);

            lblOpeningBalance.setText(String.format("$%.2f", currentSession.getOpeningBalance()));
            lblSalesTotal.setText(String.format("$%.2f", sales));
            lblExpectedCash.setText(String.format("$%.2f", currentSession.getOpeningBalance().add(sales)));
        } else {
            // No open session - show an opening form or alert
            showAlert("No Session", "No active cash session found for this user.");
        }
    }

    @FXML
    private void handleClosure() {
        if (currentSession == null)
            return;

        try {
            BigDecimal actualCash = new BigDecimal(txtActualCash.getText());
            ServiceFactory.cashSessionService().closeSession(currentSession.getId(), actualCash);

            // Re-fetch the session to get the closed_at and final calculated totals
            this.currentSession = ServiceFactory.cashSessionDao().findById(currentSession.getId())
                    .orElse(currentSession);

            BigDecimal sales = ServiceFactory.cashSessionService().calculateSalesInSession(currentSession);
            currentSession.setTotalSales(sales);
            currentSession.setActualCash(actualCash);
            currentSession.setClosedAt(java.time.LocalDateTime.now());
            currentSession.setStatus("CLOSED");

            ServiceFactory.printService().printZReport(currentSession,
                    ServiceFactory.authService().getCurrentUser().getUsername());

            BigDecimal expected = currentSession.getOpeningBalance().add(sales);
            BigDecimal diff = actualCash.subtract(expected);

            resultBox.setVisible(true);
            lblDifference.setText(String.format("Difference: $%.2f", diff));
            if (diff.compareTo(BigDecimal.ZERO) < 0)
                lblDifference.setStyle("-fx-text-fill: red;");
            else
                lblDifference.setStyle("-fx-text-fill: green;");

            showAlert("Success", "Cash session closed and Z-Report recorded.");

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Invalid actual cash amount: " + e.getMessage());
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}

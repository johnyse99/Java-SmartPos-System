package io.smartpos.ui.sale;

import io.smartpos.core.domain.sale.Sale;
import io.smartpos.services.sale.SaleService;
import io.smartpos.ui.config.ServiceFactory;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

import java.math.BigDecimal;
import java.time.LocalDate;

public class SaleHistoryController {

    @FXML
    private TableView<Sale> saleTable;
    @FXML
    private TableColumn<Sale, Integer> colId;
    @FXML
    private TableColumn<Sale, java.time.LocalDateTime> colDate;
    @FXML
    private TableColumn<Sale, BigDecimal> colTotal;
    @FXML
    private TableColumn<Sale, String> colStatus;
    @FXML
    private TableColumn<Sale, Void> colActions;
    @FXML
    private DatePicker startDatePicker;
    @FXML
    private DatePicker endDatePicker;

    private final SaleService saleService;
    private final ObservableList<Sale> saleList = FXCollections.observableArrayList();

    public SaleHistoryController() {
        this.saleService = ServiceFactory.saleService();
    }

    @FXML
    public void initialize() {
        setupTable();
        loadRecentSales();

        startDatePicker.setValue(LocalDate.now().minusWeeks(1));
        endDatePicker.setValue(LocalDate.now());
    }

    private void setupTable() {
        colId.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getId()));
        colDate.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getSaleDate()));
        colTotal.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getTotalAmount()));
        colStatus.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getStatus()));

        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button btnCancel = new Button("❌ Cancel");
            private final Button btnPrint = new Button("🖨️");
            private final HBox container = new HBox(5, btnPrint, btnCancel);

            {
                btnCancel.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-cursor: hand;");
                btnPrint.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-cursor: hand;");

                btnCancel.setOnAction(event -> {
                    Sale sale = getTableView().getItems().get(getIndex());
                    handleCancel(sale);
                });

                btnPrint.setOnAction(event -> {
                    Sale sale = getTableView().getItems().get(getIndex());
                    ServiceFactory.printService().printSaleTicket(sale);
                    showAlert("Printing", "Sending ticket for Sale #" + sale.getId() + " to printer.");
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Sale sale = getTableView().getItems().get(getIndex());
                    btnCancel.setDisable("CANCELLED".equals(sale.getStatus()));
                    setGraphic(container);
                }
            }
        });

        saleTable.setItems(saleList);
    }

    @FXML
    private void loadRecentSales() {
        saleList.setAll(saleService.findRecent(50));
    }

    @FXML
    private void handleFilter() {
        LocalDate start = startDatePicker.getValue();
        LocalDate end = endDatePicker.getValue();
        if (start != null && end != null) {
            saleList.setAll(saleService.findByDateRange(start, end));
        }
    }

    private void handleCancel(Sale sale) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Cancel Sale");
        alert.setHeaderText("Are you sure you want to cancel Sale #" + sale.getId() + "?");
        alert.setContentText("This will restore the items to stock.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    saleService.cancelSale(sale.getId());
                    ServiceFactory.printService().printCancellationTicket(sale,
                            ServiceFactory.authService().getCurrentUser().getUsername());
                    loadRecentSales();
                    showAlert("Success", "Sale cancelled and stock restored.");
                } catch (Exception e) {
                    showAlert("Error", "Failed to cancel: " + e.getMessage());
                }
            }
        });
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}

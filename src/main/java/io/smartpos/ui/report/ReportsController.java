package io.smartpos.ui.report;

import io.smartpos.core.reporting.DailyRevenueReport;
import io.smartpos.core.reporting.SaleReport;
import io.smartpos.core.reporting.StockReport;
import io.smartpos.core.reporting.TopProductReport;
import io.smartpos.services.report.ReportService;
import io.smartpos.ui.config.ServiceFactory;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class ReportsController {

    private final ReportService reportService;

    // Analytics Dashboard
    @FXML
    private DatePicker chartStartDate;
    @FXML
    private DatePicker chartEndDate;
    @FXML
    private LineChart<String, Number> revenueChart;
    @FXML
    private javafx.scene.chart.BarChart<String, Number> cashierChart;

    // Sales Report Tab
    @FXML
    private DatePicker startDate;
    @FXML
    private DatePicker endDate;
    @FXML
    private TableView<SaleReport> salesTable;
    @FXML
    private TableColumn<SaleReport, Integer> colSaleId;
    @FXML
    private TableColumn<SaleReport, String> colSaleDate;
    @FXML
    private TableColumn<SaleReport, BigDecimal> colSaleTotal;

    // Stock Report Tab
    @FXML
    private TableView<StockReport> stockTable;
    @FXML
    private TableColumn<StockReport, Integer> colProdId;
    @FXML
    private TableColumn<StockReport, String> colProdName;
    @FXML
    private TableColumn<StockReport, BigDecimal> colProdQty;

    // Top Products Tab
    @FXML
    private TableView<TopProductReport> topTable;
    @FXML
    private TableColumn<TopProductReport, String> colTopName;
    @FXML
    private TableColumn<TopProductReport, BigDecimal> colTopQty;

    public ReportsController() {
        this.reportService = ServiceFactory.reportService();
    }

    @FXML
    public void initialize() {
        try {
            // Init dates
            startDate.setValue(LocalDate.now().minusDays(30));
            endDate.setValue(LocalDate.now());

            chartStartDate.setValue(LocalDate.now().minusDays(7));
            chartEndDate.setValue(LocalDate.now());

            setupSalesTable();
            setupStockTable();
            setupTopTable();

            refreshAll();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Initialization Error", "Could not initialize reports: " + e.getMessage());
        }
    }

    private void setupSalesTable() {
        colSaleId.setCellValueFactory(Data -> new SimpleObjectProperty<>(Data.getValue().getSaleId()));
        colSaleDate.setCellValueFactory(Data -> {
            LocalDate date = Data.getValue().getSaleDate();
            return new SimpleStringProperty(date != null ? date.toString() : "");
        });
        colSaleTotal.setCellValueFactory(Data -> new SimpleObjectProperty<>(Data.getValue().getTotal()));
    }

    private void setupStockTable() {
        colProdId.setCellValueFactory(Data -> new SimpleObjectProperty<>(Data.getValue().getProductId()));
        colProdName.setCellValueFactory(Data -> new SimpleStringProperty(Data.getValue().getProductName()));
        colProdQty.setCellValueFactory(Data -> new SimpleObjectProperty<>(Data.getValue().getQuantity()));
    }

    private void setupTopTable() {
        colTopName.setCellValueFactory(Data -> new SimpleStringProperty(Data.getValue().getProductName()));
        colTopQty.setCellValueFactory(Data -> new SimpleObjectProperty<>(Data.getValue().getTotalQuantity()));
    }

    @FXML
    public void handleGenerateSales() {
        System.out.println("Generating sales report...");
        try {
            if (startDate.getValue() == null || endDate.getValue() == null) {
                showAlert("Error", "Please select both start and end dates.");
                return;
            }
            salesTable.setItems(FXCollections.observableArrayList(
                    reportService.getSalesByDateRange(startDate.getValue(), endDate.getValue())));
            System.out.println("Sales report generated successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Could not generate sales report: " + e.getMessage());
        }
    }

    @FXML
    public void updateAnalyticsCharts() {
        System.out.println("Updating analytics charts...");
        try {
            if (chartStartDate.getValue() == null || chartEndDate.getValue() == null) {
                return;
            }
            // Revenue Chart
            revenueChart.getData().clear();
            XYChart.Series<String, Number> revSeries = new XYChart.Series<>();
            revSeries.setName("Revenue");

            List<DailyRevenueReport> revData = reportService.getDailyRevenue(chartStartDate.getValue(),
                    chartEndDate.getValue());
            for (DailyRevenueReport report : revData) {
                revSeries.getData().add(new XYChart.Data<>(report.getDate().toString(), report.getAmount()));
            }
            revenueChart.getData().add(revSeries);

            // Cashier Chart
            cashierChart.getData().clear();
            XYChart.Series<String, Number> cashSeries = new XYChart.Series<>();
            cashSeries.setName("Sales by Cashier");

            java.util.Map<String, BigDecimal> cashData = reportService.getSalesByCashier(chartStartDate.getValue(),
                    chartEndDate.getValue());
            cashData.forEach((cashier, total) -> {
                cashSeries.getData().add(new XYChart.Data<>(cashier, total));
            });
            cashierChart.getData().add(cashSeries);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Could not update analytics charts: " + e.getMessage());
        }
    }

    @FXML
    public void refreshAll() {
        System.out.println("Refreshing all report data...");
        try {
            handleGenerateSales();
            updateAnalyticsCharts();
            stockTable.setItems(FXCollections.observableArrayList(reportService.getCurrentStock()));
            topTable.setItems(FXCollections.observableArrayList(reportService.getTopSellingProducts(10)));
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Could not refresh data: " + e.getMessage());
        }
    }

    @FXML
    public void handlePrintReport() {
        try {
            if (salesTable.getItems().isEmpty()) {
                showAlert("Error", "Run the report first to get data to print.");
                return;
            }
            ServiceFactory.printService().printSalesReport(
                    salesTable.getItems(),
                    startDate.getValue(),
                    endDate.getValue());
            System.out.println("Sales report sent to printer.");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Printing failed: " + e.getMessage());
        }
    }

    private void showAlert(String title, String content) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}

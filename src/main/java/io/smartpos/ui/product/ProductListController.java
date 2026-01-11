package io.smartpos.ui.product;

import io.smartpos.core.domain.product.Product;
import io.smartpos.services.product.ProductService;
import io.smartpos.ui.config.ServiceFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

public class ProductListController {

    @FXML
    private TableView<Product> productTable;
    @FXML
    private TableColumn<Product, Integer> colId;
    @FXML
    private TableColumn<Product, String> colCode;
    @FXML
    private TableColumn<Product, String> colName;
    @FXML
    private TableColumn<Product, String> colCategory;
    @FXML
    private TableColumn<Product, String> colUnit;
    @FXML
    private TableColumn<Product, BigDecimal> colStock;
    @FXML
    private TableColumn<Product, BigDecimal> colPrice;
    @FXML
    private TableColumn<Product, Boolean> colActive;
    @FXML
    private TextField searchField;

    private final ProductService productService;
    private final ObservableList<Product> productList = FXCollections.observableArrayList();

    public ProductListController() {
        this.productService = ServiceFactory.productService();
    }

    @FXML
    public void initialize() {
        setupTable();
        setupSearch();
        loadProducts();
    }

    private void setupTable() {
        colId.setCellValueFactory(cell -> new javafx.beans.property.SimpleObjectProperty<>(cell.getValue().getId()));
        colCode.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getCode()));
        colName.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getName()));
        colCategory.setCellValueFactory(
                cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getCategoryName()));
        colUnit.setCellValueFactory(
                cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getUnitName()));
        colStock.setCellValueFactory(
                cell -> new javafx.beans.property.SimpleObjectProperty<>(cell.getValue().getCurrentStock()));
        colPrice.setCellValueFactory(
                cell -> new javafx.beans.property.SimpleObjectProperty<>(cell.getValue().getPrice()));
        colActive.setCellValueFactory(
                cell -> new javafx.beans.property.SimpleObjectProperty<>(cell.getValue().isActive()));

        // Low stock highlighting row factory
        productTable.setRowFactory(tv -> new TableRow<Product>() {
            @Override
            protected void updateItem(Product item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    getStyleClass().remove("low-stock");
                } else if (item.getCurrentStock() != null && item.getMinimumStock() != null &&
                        item.getCurrentStock().compareTo(item.getMinimumStock()) <= 0) {
                    if (!getStyleClass().contains("low-stock")) {
                        getStyleClass().add("low-stock");
                    }
                } else {
                    getStyleClass().remove("low-stock");
                }
            }
        });
    }

    private void setupSearch() {
        FilteredList<Product> filteredData = new FilteredList<>(productList, p -> true);
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(product -> {
                if (newValue == null || newValue.isEmpty())
                    return true;
                String lowerCaseFilter = newValue.toLowerCase();

                if (product.getName().toLowerCase().contains(lowerCaseFilter))
                    return true;
                if (product.getCode().toLowerCase().contains(lowerCaseFilter))
                    return true;
                if (product.getCategoryName() != null
                        && product.getCategoryName().toLowerCase().contains(lowerCaseFilter))
                    return true;

                return false;
            });
        });
        productTable.setItems(filteredData);
    }

    private void loadProducts() {
        productList.setAll(productService.findAllActive());
    }

    @FXML
    private void handleAdd() {
        showProductForm(new Product());
    }

    @FXML
    private void handleEdit() {
        Product selected = productTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No Selection", "Please select a product to edit.");
            return;
        }
        showProductForm(selected);
    }

    @FXML
    private void handleAdjustStock() {
        Product selected = productTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No Selection", "Please select a product to adjust stock.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog("0");
        dialog.setTitle("Stock Adjustment");
        dialog.setHeaderText("Adjust stock for: " + selected.getName());
        dialog.setContentText("Enter amount to add (positive) or remove (negative):");

        dialog.showAndWait().ifPresent(input -> {
            try {
                BigDecimal adjustment = new BigDecimal(input);
                if (adjustment.compareTo(BigDecimal.ZERO) == 0)
                    return;

                String type = adjustment.compareTo(BigDecimal.ZERO) > 0 ? "IN" : "OUT";
                BigDecimal quantity = adjustment.abs();

                ServiceFactory.inventoryService().registerAdjustment(selected.getId(), quantity, type);
                loadProducts();
                showAlert("Success", "Stock adjusted successfully.");
            } catch (NumberFormatException e) {
                showAlert("Error", "Invalid number format.");
            } catch (Exception e) {
                showAlert("Error", "Adjustment failed: " + e.getMessage());
            }
        });
    }

    @FXML
    private void handleDelete() {
        Product selected = productTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No Selection", "Please select a product to deactivate.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Deactivate Product");
        alert.setContentText("Are you sure you want to deactivate: " + selected.getName() + "?");
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    productService.deactivateProduct(selected.getId());
                    loadProducts();
                } catch (Exception e) {
                    showAlert("Error", e.getMessage());
                }
            }
        });
    }

    @FXML
    private void handleExport() {
        List<Product> lowStockItems = productList.stream()
                .filter(p -> p.getCurrentStock() != null && p.getMinimumStock() != null &&
                        p.getCurrentStock().compareTo(p.getMinimumStock()) <= 0)
                .toList();

        if (lowStockItems.isEmpty()) {
            showAlert("Export", "No products with low stock to export.");
            return;
        }

        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Save Low Stock Report");
        fileChooser.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        fileChooser.setInitialFileName("low_stock_report.csv");

        java.io.File file = fileChooser.showSaveDialog(searchField.getScene().getWindow());

        if (file != null) {
            try (java.io.PrintWriter writer = new java.io.PrintWriter(file)) {
                writer.println("Code,Name,Category,Stock,Min Stock,Price");
                for (Product p : lowStockItems) {
                    writer.printf("%s,%s,%s,%s,%s,%s\n",
                            p.getCode(),
                            p.getName(),
                            p.getCategoryName(),
                            p.getCurrentStock(),
                            p.getMinimumStock(),
                            p.getPrice());
                }
                showAlert("Success", "Report exported to: " + file.getAbsolutePath());
            } catch (Exception e) {
                showAlert("Error", "Could not export file: " + e.getMessage());
            }
        }
    }

    private void showProductForm(Product product) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/product/product-form.fxml"));
            Parent root = loader.load();

            ProductFormController controller = loader.getController();
            controller.setProduct(product);

            Stage stage = new Stage();
            stage.setTitle(product.getId() == null || product.getId() == 0 ? "New Product" : "Edit Product");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

            // Refresh table after close
            loadProducts();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Could not open form: " + e.getMessage());
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

package io.smartpos.ui.purchase;

import io.smartpos.core.domain.product.Product;
import io.smartpos.core.domain.purchase.Purchase;
import io.smartpos.core.domain.purchase.PurchaseItem;
import io.smartpos.core.domain.purchase.Supplier;
import io.smartpos.services.product.ProductService;
import io.smartpos.services.purchase.PurchaseService;
import io.smartpos.ui.config.ServiceFactory;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class PurchaseController {

    @FXML
    private ComboBox<Supplier> supplierComboBox;
    @FXML
    private DatePicker dateField;

    // Item Entry
    @FXML
    private TextField productIdField;
    @FXML
    private TextField quantityField;
    @FXML
    private TextField costField;
    @FXML
    private Label productNameLabel; // Need to add this to FXML or just use alerts

    @FXML
    private TableView<PurchaseItem> itemsTable;
    @FXML
    private TableColumn<PurchaseItem, String> colProductId; // Changing to show Name
    @FXML
    private TableColumn<PurchaseItem, BigDecimal> colQuantity;
    @FXML
    private TableColumn<PurchaseItem, BigDecimal> colCost;
    @FXML
    private TableColumn<PurchaseItem, BigDecimal> colTotal;

    @FXML
    private Label totalLabel;

    private final PurchaseService purchaseService;
    private final ProductService productService;
    private final ObservableList<PurchaseItem> currentItems = FXCollections.observableArrayList();
    private Product currentProduct;

    public PurchaseController() {
        this.purchaseService = ServiceFactory.purchaseService();
        this.productService = ServiceFactory.productService();
    }

    @FXML
    public void initialize() {
        dateField.setValue(LocalDate.now());
        setupSupplierList();

        colProductId.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getProductName()));
        colQuantity.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getQuantity()));
        colCost.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getUnitCost()));
        colTotal.setCellValueFactory(cell -> new SimpleObjectProperty<>(
                cell.getValue().getQuantity().multiply(cell.getValue().getUnitCost())));

        itemsTable.setItems(currentItems);

        productIdField.setOnAction(e -> lookupProduct());
    }

    private void setupSupplierList() {
        List<Supplier> suppliers = ServiceFactory.supplierDao().findAllActive();
        supplierComboBox.setItems(FXCollections.observableArrayList(suppliers));
        if (!suppliers.isEmpty()) {
            supplierComboBox.setValue(suppliers.get(0));
        }
    }

    private void lookupProduct() {
        String text = productIdField.getText().trim();
        if (text.isEmpty())
            return;

        Product p = productService.findByCode(text);
        if (p == null) {
            try {
                p = productService.findById(Integer.parseInt(text));
            } catch (Exception ignored) {
            }
        }

        if (p != null) {
            this.currentProduct = p;
            // Optionally update a label if we add it to FXML
            quantityField.requestFocus();
        } else {
            this.currentProduct = null;
            showAlert("Not Found", "Product not found.");
        }
    }

    @FXML
    private void handleAddItem() {
        try {
            if (currentProduct == null) {
                lookupProduct();
                if (currentProduct == null)
                    return;
            }

            BigDecimal qty = new BigDecimal(quantityField.getText());
            BigDecimal cost = new BigDecimal(costField.getText());

            PurchaseItem item = new PurchaseItem();
            item.setProductId(currentProduct.getId());
            item.setProductName(currentProduct.getName());
            item.setQuantity(qty);
            item.setUnitCost(cost);

            currentItems.add(item);
            calculateTotal();

            // Clear entry
            productIdField.clear();
            quantityField.clear();
            costField.clear();
            currentProduct = null;
            productIdField.requestFocus();

        } catch (Exception e) {
            showAlert("Invalid Item", "Please check your inputs.");
        }
    }

    @FXML
    private void handleRemoveItem() {
        PurchaseItem selected = itemsTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            currentItems.remove(selected);
            calculateTotal();
        }
    }

    @FXML
    private void handleSave() {
        try {
            if (currentItems.isEmpty()) {
                showAlert("Empty Purchase", "Please add items first.");
                return;
            }

            Supplier supplier = supplierComboBox.getValue();
            if (supplier == null) {
                showAlert("Error", "Please select a supplier.");
                return;
            }

            Purchase purchase = new Purchase();
            purchase.setSupplierId(supplier.getId());
            purchase.setPurchaseDate(dateField.getValue());
            purchase.setItems(currentItems);
            purchase.setTotalAmount(calculateTotalValue());

            purchaseService.registerPurchase(purchase);

            showAlert("Success", "Inventory updated successfully!");
            clearForm();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Could not register purchase: " + e.getMessage());
        }
    }

    private void calculateTotal() {
        totalLabel.setText(String.format("Total: $%.2f", calculateTotalValue()));
    }

    private BigDecimal calculateTotalValue() {
        return currentItems.stream()
                .map(i -> i.getQuantity().multiply(i.getUnitCost()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void clearForm() {
        currentItems.clear();
        calculateTotal();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}

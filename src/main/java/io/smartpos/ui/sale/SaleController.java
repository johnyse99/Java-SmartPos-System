package io.smartpos.ui.sale;

import io.smartpos.core.domain.customer.Customer;
import io.smartpos.core.domain.product.Product;
import io.smartpos.core.domain.sale.Sale;
import io.smartpos.core.domain.sale.SaleItem;
import io.smartpos.services.product.ProductService;
import io.smartpos.services.sale.SaleService;
import io.smartpos.ui.config.ServiceFactory;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.math.BigDecimal;
import java.util.List;

public class SaleController {

    @FXML
    private ComboBox<Customer> customerComboBox;
    @FXML
    private TextField productIdField;
    @FXML
    private TextField quantityField;
    @FXML
    private TextField priceField;
    @FXML
    private Label productNameLabel;

    @FXML
    private TableView<SaleItem> itemsTable;
    @FXML
    private TableColumn<SaleItem, String> productCol;
    @FXML
    private TableColumn<SaleItem, BigDecimal> quantityCol;
    @FXML
    private TableColumn<SaleItem, BigDecimal> priceCol;
    @FXML
    private TableColumn<SaleItem, BigDecimal> subtotalCol;

    @FXML
    private Label totalLabel;

    private final SaleService saleService;
    private final ProductService productService;

    private final ObservableList<SaleItem> saleItems = FXCollections.observableArrayList();
    private Product currentProduct;

    public SaleController() {
        this.saleService = ServiceFactory.saleService();
        this.productService = ServiceFactory.productService();
    }

    @FXML
    public void initialize() {
        setupTable();
        setupCustomerList();

        productIdField.setOnAction(e -> lookupProduct());
    }

    private void setupCustomerList() {
        List<Customer> customers = ServiceFactory.customerDao().findAllActive();
        customerComboBox.setItems(FXCollections.observableArrayList(customers));

        // Select Walk-in Customer by default (usually ID 1)
        customers.stream()
                .filter(c -> c.getId() == 1)
                .findFirst()
                .ifPresent(customerComboBox::setValue);
    }

    private void setupTable() {
        productCol.setCellValueFactory(
                cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getProductName()));
        quantityCol.setCellValueFactory(
                cell -> new javafx.beans.property.SimpleObjectProperty<>(cell.getValue().getQuantity()));
        priceCol.setCellValueFactory(
                cell -> new javafx.beans.property.SimpleObjectProperty<>(cell.getValue().getUnitPrice()));
        subtotalCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleObjectProperty<>(
                cell.getValue().getUnitPrice().multiply(cell.getValue().getQuantity())));

        itemsTable.setItems(saleItems);
    }

    private void lookupProduct() {
        String text = productIdField.getText().trim();
        if (text.isEmpty()) {
            currentProduct = null;
            return;
        }

        Product p = productService.findByCode(text);
        if (p == null) {
            try {
                int id = Integer.parseInt(text);
                p = productService.findById(id);
            } catch (NumberFormatException ignored) {
            }
        }

        if (p != null && p.isActive()) {
            this.currentProduct = p;
            productNameLabel.setText(p.getName() + " (" + p.getUnitName() + ")");
            priceField.setText(p.getPrice().toString());
            quantityField.requestFocus();
        } else {
            this.currentProduct = null;
            productNameLabel.setText("Product not found");
            priceField.clear();
        }
    }

    @FXML
    private void addItem() {
        try {
            // If user didn't press ENTER, try to lookup now
            if (currentProduct == null && !productIdField.getText().trim().isEmpty()) {
                lookupProduct();
            }

            if (currentProduct == null) {
                showAlert("Error", "Product not found. Please enter a valid Code or ID.");
                return;
            }

            BigDecimal quantity;
            try {
                quantity = new BigDecimal(quantityField.getText());
            } catch (Exception e) {
                showAlert("Error", "Invalid quantity. Please enter a number.");
                return;
            }

            BigDecimal unitPrice;
            try {
                unitPrice = new BigDecimal(priceField.getText());
            } catch (Exception e) {
                showAlert("Error", "Invalid price.");
                return;
            }

            SaleItem item = new SaleItem();
            item.setProductId(currentProduct.getId());
            item.setProductName(currentProduct.getName());
            item.setQuantity(quantity);
            item.setUnitPrice(unitPrice);

            saleItems.add(item);
            updateTotal();

            // Reset UI for next item
            productIdField.clear();
            quantityField.setText("1");
            priceField.clear();
            productNameLabel.setText("");
            currentProduct = null;
            productIdField.requestFocus();

        } catch (Exception ex) {
            showAlert("Error", "Unexpected error: " + ex.getMessage());
        }
    }

    @FXML
    private void saveSale() {
        try {
            if (saleItems.isEmpty()) {
                showAlert("Empty Sale", "Add items first.");
                return;
            }

            Customer customer = customerComboBox.getValue();
            if (customer == null) {
                showAlert("Error", "Please select a customer.");
                return;
            }

            Sale sale = new Sale();
            sale.setUserId(ServiceFactory.authService().getCurrentUser().getId());
            sale.setCustomerId(customer.getId());
            sale.setItems(saleItems);

            saleService.registerSale(sale);

            // Automatically print ticket
            ServiceFactory.printService().printSaleTicket(sale);

            showAlert("Success",
                    "Sale registered and ticket printed! Total: $" + totalLabel.getText().replace("Total: ", ""));

            saleItems.clear();
            updateTotal();
            productIdField.requestFocus();

        } catch (Exception ex) {
            showAlert("Error", ex.getMessage());
        }
    }

    private void updateTotal() {
        BigDecimal total = saleItems.stream()
                .map(i -> i.getQuantity().multiply(i.getUnitPrice()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        totalLabel.setText("Total: " + total.toString());
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

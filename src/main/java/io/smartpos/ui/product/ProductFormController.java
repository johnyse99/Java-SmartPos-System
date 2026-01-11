package io.smartpos.ui.product;

import io.smartpos.core.domain.product.Category;
import io.smartpos.core.domain.product.Product;
import io.smartpos.core.domain.product.UnitOfMeasure;
import io.smartpos.services.product.ProductService;
import io.smartpos.ui.config.ServiceFactory;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.math.BigDecimal;

public class ProductFormController {

    @FXML
    private TextField codeField;
    @FXML
    private TextField nameField;
    @FXML
    private ComboBox<Category> categoryCombo;
    @FXML
    private ComboBox<UnitOfMeasure> unitCombo;
    @FXML
    private TextField priceField;
    @FXML
    private TextField minStockField;

    private Product product;
    private final ProductService productService;

    public ProductFormController() {
        this.productService = ServiceFactory.productService();
    }

    @FXML
    public void initialize() {
        // Load data for combos
        categoryCombo.getItems().setAll(ServiceFactory.categoryDao().findAllActive());
        unitCombo.getItems().setAll(ServiceFactory.unitOfMeasureDao().findAll());
    }

    public void setProduct(Product product) {
        this.product = product;
        if (product != null && product.getId() != null && product.getId() > 0) {
            codeField.setText(product.getCode());
            nameField.setText(product.getName());
            priceField.setText(product.getPrice() != null ? product.getPrice().toString() : "0.00");
            minStockField.setText(product.getMinimumStock() != null ? product.getMinimumStock().toString() : "0.00");

            // Select in combo
            categoryCombo.getItems().stream()
                    .filter(c -> c.getId().equals(product.getCategoryId()))
                    .findFirst().ifPresent(c -> categoryCombo.setValue(c));

            unitCombo.getItems().stream()
                    .filter(u -> u.getId().equals(product.getUnitId()))
                    .findFirst().ifPresent(u -> unitCombo.setValue(u));
        }
    }

    @FXML
    private void handleSave() {
        try {
            validateInput();

            product.setCode(codeField.getText().trim());
            product.setName(nameField.getText().trim());
            product.setCategoryId(categoryCombo.getValue().getId());
            product.setUnitId(unitCombo.getValue().getId());
            product.setPrice(new BigDecimal(priceField.getText()));
            product.setMinimumStock(new BigDecimal(minStockField.getText()));

            if (product.getId() == null || product.getId() == 0) {
                productService.createProduct(product);
            } else {
                productService.updateProduct(product);
            }

            close();
        } catch (NumberFormatException e) {
            showAlert("Invalid Input", "Price and Stock must be valid numbers.");
        } catch (Exception e) {
            showAlert("Error", e.getMessage());
        }
    }

    private void validateInput() throws Exception {
        if (codeField.getText().trim().isEmpty())
            throw new Exception("Code is required");
        if (nameField.getText().trim().isEmpty())
            throw new Exception("Name is required");
        if (categoryCombo.getValue() == null)
            throw new Exception("Category is required");
        if (unitCombo.getValue() == null)
            throw new Exception("Unit is required");
    }

    @FXML
    private void handleCancel() {
        close();
    }

    private void close() {
        Stage stage = (Stage) nameField.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}

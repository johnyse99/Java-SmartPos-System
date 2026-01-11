/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package io.smartpos.ui.controller.sale;

import io.smartpos.core.domain.sale.Sale;
import io.smartpos.core.domain.sale.SaleItem;
import io.smartpos.services.sale.SaleService;
import io.smartpos.ui.config.ServiceFactory;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class SaleController {

    private final SaleService saleService
            = ServiceFactory.saleService();

    @FXML
    private TextField customerIdField;

    // later: table of items
    private final List<SaleItem> items = new ArrayList<>();

    @FXML
    public void onRegisterSale() {
        try {
            Sale sale = buildSaleFromForm();
            saleService.registerSale(sale);
            showInfo("Sale registered successfully");
            clearForm();
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    private Sale buildSaleFromForm() {

        Sale sale = new Sale();
        sale.setCustomerId(
                Integer.parseInt(customerIdField.getText())
        );
        sale.setItems(items);

        return sale;
    }

    private void clearForm() {
        customerIdField.clear();
        items.clear();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setContentText(message);
        alert.showAndWait();
    }
}

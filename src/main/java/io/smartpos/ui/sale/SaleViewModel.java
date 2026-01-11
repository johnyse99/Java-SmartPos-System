/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package io.smartpos.ui.sale;

import io.smartpos.core.domain.sale.SaleItem;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class SaleViewModel {

    private int customerId;
    private final List<SaleItem> items = new ArrayList<>();

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public int getCustomerId() {
        return customerId;
    }

    public List<SaleItem> getItems() {
        return items;
    }

    public void addItem(SaleItem item) {
        items.add(item);
    }

    public void removeItem(SaleItem item) {
        items.remove(item);
    }

    public BigDecimal calculateTotal() {
        return items.stream()
                .map(i -> i.getUnitPrice().multiply(i.getQuantity()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void clear() {
        items.clear();
    }
}

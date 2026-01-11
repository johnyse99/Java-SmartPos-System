/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package io.smartpos.core.domain.sale;

import io.smartpos.core.exceptions.ValidationException;

import java.math.BigDecimal;

import java.util.List;

public class Sale {

    private int id;
    private int userId;
    private int customerId;
    private java.time.LocalDateTime saleDate;
    private java.math.BigDecimal totalAmount;
    private String status;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    private List<SaleItem> items;

    public Sale() {
        this.saleDate = java.time.LocalDateTime.now();
        this.totalAmount = java.math.BigDecimal.ZERO;
        this.status = "REGISTERED";
    }

    // ===== Domain Behavior =====

    public void validate() {

        if (customerId <= 0) {
            throw new ValidationException("Invalid customer");
        }

        if (saleDate == null) {
            throw new ValidationException("Sale date is required");
        }

        if (items == null || items.isEmpty()) {
            throw new ValidationException("Sale must contain items");
        }

        for (SaleItem item : items) {

            if (item.getQuantity() == null
                    || item.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
                throw new ValidationException(
                        "Item quantity must be greater than zero");
            }

            if (item.getUnitPrice() == null
                    || item.getUnitPrice().compareTo(BigDecimal.ZERO) < 0) {
                throw new ValidationException(
                        "Item unit price cannot be negative");
            }
        }
    }

    public void calculateTotal() {

        BigDecimal total = BigDecimal.ZERO;

        for (SaleItem item : items) {
            total = total.add(
                    item.getUnitPrice().multiply(item.getQuantity()));
        }

        this.totalAmount = total;
    }

    // ===== Infrastructure hydration =====
    // Used by DAO only
    public void loadTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    // ===== Getters & Setters =====

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public java.time.LocalDateTime getSaleDate() {
        return saleDate;
    }

    public void setSaleDate(java.time.LocalDateTime saleDate) {
        this.saleDate = saleDate;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public List<SaleItem> getItems() {
        return items;
    }

    public void setItems(List<SaleItem> items) {
        this.items = items;
    }

    public void setTotalAmount(BigDecimal bigDecimal) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from
                                                                       // nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
}

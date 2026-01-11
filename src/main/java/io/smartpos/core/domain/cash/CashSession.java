package io.smartpos.core.domain.cash;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CashSession {
    private Integer id;
    private Integer userId;
    private LocalDateTime openedAt;
    private LocalDateTime closedAt;
    private BigDecimal openingBalance;
    private BigDecimal totalSales;
    private BigDecimal actualCash;
    private String status; // OPEN, CLOSED

    public CashSession() {
        this.openedAt = LocalDateTime.now();
        this.status = "OPEN";
        this.openingBalance = BigDecimal.ZERO;
        this.totalSales = BigDecimal.ZERO;
        this.actualCash = BigDecimal.ZERO;
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public LocalDateTime getOpenedAt() {
        return openedAt;
    }

    public void setOpenedAt(LocalDateTime openedAt) {
        this.openedAt = openedAt;
    }

    public LocalDateTime getClosedAt() {
        return closedAt;
    }

    public void setClosedAt(LocalDateTime closedAt) {
        this.closedAt = closedAt;
    }

    public BigDecimal getOpeningBalance() {
        return openingBalance;
    }

    public void setOpeningBalance(BigDecimal openingBalance) {
        this.openingBalance = openingBalance;
    }

    public BigDecimal getTotalSales() {
        return totalSales;
    }

    public void setTotalSales(BigDecimal totalSales) {
        this.totalSales = totalSales;
    }

    public BigDecimal getActualCash() {
        return actualCash;
    }

    public void setActualCash(BigDecimal actualCash) {
        this.actualCash = actualCash;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BigDecimal getExpectedCash() {
        return openingBalance.add(totalSales);
    }

    public BigDecimal getDifference() {
        if (actualCash == null)
            return BigDecimal.ZERO;
        return actualCash.subtract(getExpectedCash());
    }
}

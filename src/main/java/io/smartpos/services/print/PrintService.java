package io.smartpos.services.print;

import io.smartpos.core.domain.sale.Sale;

public interface PrintService {
    void printSaleTicket(Sale sale);

    void printZReport(io.smartpos.core.domain.cash.CashSession session, String username);

    void printCancellationTicket(Sale sale, String username);

    void printSalesReport(java.util.List<io.smartpos.core.reporting.SaleReport> sales, java.time.LocalDate start,
            java.time.LocalDate end);
}

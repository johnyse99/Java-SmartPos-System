/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package io.smartpos.services.report;

import io.smartpos.core.reporting.SaleReport;
import io.smartpos.core.reporting.StockReport;
import io.smartpos.core.reporting.TopProductReport;
import io.smartpos.core.reporting.DailyRevenueReport;
import java.time.LocalDate;
import java.util.List;

public interface ReportService {

    List<SaleReport> getSalesByDateRange(
            LocalDate startDate,
            LocalDate endDate);

    List<StockReport> getCurrentStock();

    List<TopProductReport> getTopSellingProducts(int limit);

    List<DailyRevenueReport> getDailyRevenue(LocalDate start, LocalDate end);

    java.util.Map<String, java.math.BigDecimal> getSalesByCashier(LocalDate start, LocalDate end);
}

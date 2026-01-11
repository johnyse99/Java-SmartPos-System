package io.smartpos.services.report;

import io.smartpos.core.reporting.SaleReport;
import io.smartpos.core.reporting.StockReport;
import io.smartpos.core.reporting.TopProductReport;
import io.smartpos.core.reporting.DailyRevenueReport;
import io.smartpos.infrastructure.dao.ReportDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.List;

public class ReportServiceImpl implements ReportService {

    private static final Logger logger = LoggerFactory.getLogger(ReportServiceImpl.class);
    private final ReportDao reportDao;

    public ReportServiceImpl(ReportDao reportDao) {
        this.reportDao = reportDao;
    }

    @Override
    public List<SaleReport> getSalesByDateRange(LocalDate startDate, LocalDate endDate) {
        logger.info("Generating sale report for range: {} to {}", startDate, endDate);
        return reportDao.getSalesByDateRange(startDate, endDate);
    }

    @Override
    public List<StockReport> getCurrentStock() {
        logger.info("Generating current stock report");
        return reportDao.getCurrentStock();
    }

    @Override
    public List<TopProductReport> getTopSellingProducts(int limit) {
        logger.info("Generating top {} selling products report", limit);
        return reportDao.getTopSellingProducts(limit);
    }

    @Override
    public List<DailyRevenueReport> getDailyRevenue(LocalDate start, LocalDate end) {
        logger.info("Generating daily revenue report for range: {} to {}", start, end);
        return reportDao.getDailyRevenue(start, end);
    }

    @Override
    public java.util.Map<String, java.math.BigDecimal> getSalesByCashier(LocalDate start, LocalDate end) {
        logger.info("Generating sales by cashier report for range: {} to {}", start, end);
        return reportDao.getSalesByCashier(start, end);
    }
}

package io.smartpos.services.sale;

import io.smartpos.core.domain.sale.Sale;
import io.smartpos.core.domain.sale.SaleItem;
import io.smartpos.core.exceptions.BusinessException;
import io.smartpos.infrastructure.dao.InventoryMovementDao;
import io.smartpos.infrastructure.dao.SaleDao;
import io.smartpos.infrastructure.dao.SaleItemDao;
import io.smartpos.infrastructure.datasource.DataSourceProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SaleServiceImpl implements SaleService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SaleServiceImpl.class);

    private final SaleDao saleDao;
    private final SaleItemDao saleItemDao;
    private final InventoryMovementDao inventoryDao;
    private final DataSourceProvider dataSourceProvider;

    public SaleServiceImpl(
            SaleDao saleDao,
            SaleItemDao saleItemDao,
            InventoryMovementDao inventoryDao,
            DataSourceProvider dataSourceProvider) {

        this.saleDao = saleDao;
        this.saleItemDao = saleItemDao;
        this.inventoryDao = inventoryDao;
        this.dataSourceProvider = dataSourceProvider;
    }

    @Override
    public void registerSale(Sale sale) {

        LOGGER.info("Starting sale registration");

        sale.validate();
        sale.calculateTotal();

        Connection connection = null;

        try {
            connection = dataSourceProvider.getConnection();
            connection.setAutoCommit(false);

            // 1. Validate stock (batch)
            validateStockBatch(sale.getItems(), connection);

            // 2. Save sale
            int saleId = saleDao.save(sale, connection);
            sale.setId(saleId);

            // 3. Attach saleId to items
            for (SaleItem item : sale.getItems()) {
                item.setSaleId(saleId);
            }

            // 4. Batch insert sale items
            saleItemDao.saveBatch(sale.getItems(), connection);

            // 5. Build quantity map by product
            Map<Integer, BigDecimal> quantityByProduct = new HashMap<>();

            for (SaleItem item : sale.getItems()) {
                quantityByProduct.merge(
                        item.getProductId(),
                        item.getQuantity(),
                        BigDecimal::add);
            }

            // 6. Batch inventory exit
            inventoryDao.registerExitBatch(
                    quantityByProduct,
                    "SALE",
                    connection);

            connection.commit();

            LOGGER.info(
                    "Sale registered successfully. Sale ID: {}",
                    saleId);

        } catch (Exception ex) {

            if (connection != null) {
                try {
                    connection.rollback();
                } catch (Exception ignored) {
                }
            }

            throw new BusinessException(
                    "Error registering sale",
                    ex);

        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (Exception ignored) {
                }
            }
        }
    }

    private void validateStockBatch(
            List<SaleItem> items,
            Connection connection) throws Exception {

        List<Integer> productIds = new ArrayList<>();
        for (SaleItem item : items) {
            productIds.add(item.getProductId());
        }

        Map<Integer, BigDecimal> stockByProduct = inventoryDao.getCurrentStockByProductIds(
                productIds,
                connection);

        for (SaleItem item : items) {

            BigDecimal currentStock = stockByProduct.getOrDefault(
                    item.getProductId(),
                    BigDecimal.ZERO);

            if (currentStock.compareTo(item.getQuantity()) < 0) {
                throw new BusinessException(
                        "Insufficient stock for product ID: "
                                + item.getProductId());
            }
        }
    }

    @Override
    public Sale findById(int saleId) {

        LOGGER.info("Finding sale by ID: {}", saleId);

        Connection connection = null;

        try {
            connection = dataSourceProvider.getConnection();

            Sale sale = saleDao.findById(saleId);

            if (sale == null) {
                return null;
            }

            List<SaleItem> items = saleItemDao.findBySaleId(
                    saleId,
                    connection);

            sale.setItems(items);
            sale.calculateTotal();

            return sale;

        } catch (Exception ex) {
            throw new BusinessException(
                    "Error finding sale by ID: " + saleId,
                    ex);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (Exception ignored) {
                }
            }
        }
    }

    @Override
    public List<Sale> findByDateRange(
            LocalDate startDate,
            LocalDate endDate) {

        LOGGER.info(
                "Finding sales between {} and {}",
                startDate,
                endDate);

        Connection connection = null;

        try {
            connection = dataSourceProvider.getConnection();

            List<Sale> sales = saleDao.findByDateRange(
                    startDate,
                    endDate);

            if (sales.isEmpty()) {
                return sales;
            }

            List<Integer> saleIds = new ArrayList<>();
            for (Sale sale : sales) {
                saleIds.add(sale.getId());
            }

            List<SaleItem> allItems = saleItemDao.findBySaleIds(
                    saleIds,
                    connection);

            Map<Integer, List<SaleItem>> itemsBySale = new HashMap<>();

            for (SaleItem item : allItems) {
                itemsBySale
                        .computeIfAbsent(
                                item.getSaleId(),
                                k -> new ArrayList<>())
                        .add(item);
            }

            for (Sale sale : sales) {
                List<SaleItem> items = itemsBySale.getOrDefault(
                        sale.getId(),
                        List.of());

                sale.setItems(items);
                sale.calculateTotal();
            }

            return sales;

        } catch (Exception ex) {
            throw new BusinessException(
                    "Error finding sales by date range",
                    ex);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (Exception ignored) {
                }
            }
        }
    }

    @Override
    public void cancelSale(int saleId) {
        Connection conn = null;
        try {
            conn = dataSourceProvider.getConnection();
            conn.setAutoCommit(false);

            Sale sale = findById(saleId);
            if (sale == null)
                throw new BusinessException("Sale not found: " + saleId);
            if ("CANCELLED".equals(sale.getStatus()))
                throw new BusinessException("Sale is already cancelled");

            // Update status
            saleDao.updateStatus(saleId, "CANCELLED", conn);

            // Restore stock
            for (SaleItem item : sale.getItems()) {
                inventoryDao.registerEntry(item.getProductId(), item.getQuantity(), "SALE", conn);
            }

            conn.commit();
            LOGGER.info("Sale #{} cancelled and stock restored.", saleId);

        } catch (Exception e) {
            if (conn != null)
                try {
                    conn.rollback();
                } catch (SQLException ignored) {
                }
            throw new BusinessException("Failed to cancel sale: " + e.getMessage(), e);
        } finally {
            if (conn != null)
                try {
                    conn.close();
                } catch (SQLException ignored) {
                }
        }
    }

    @Override
    public List<Sale> findRecent(int limit) {
        LOGGER.info("Finding recent sales, limit: {}", limit);
        List<Sale> sales = saleDao.findRecent(limit);

        if (sales.isEmpty()) {
            return sales;
        }

        Connection connection = null;
        try {
            connection = dataSourceProvider.getConnection();
            List<Integer> saleIds = new ArrayList<>();
            for (Sale sale : sales) {
                saleIds.add(sale.getId());
            }

            List<SaleItem> allItems = saleItemDao.findBySaleIds(saleIds, connection);
            Map<Integer, List<SaleItem>> itemsBySale = new HashMap<>();

            for (SaleItem item : allItems) {
                itemsBySale.computeIfAbsent(item.getSaleId(), k -> new ArrayList<>()).add(item);
            }

            for (Sale sale : sales) {
                sale.setItems(itemsBySale.getOrDefault(sale.getId(), new ArrayList<>()));
                sale.calculateTotal();
            }

        } catch (SQLException ex) {
            LOGGER.error("Error loading items for recent sales", ex);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException ignored) {
                }
            }
        }

        return sales;
    }
}

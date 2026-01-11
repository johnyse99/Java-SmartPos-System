/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package io.smartpos.services.purchase;

import io.smartpos.core.domain.purchase.Purchase;
import io.smartpos.core.domain.purchase.PurchaseItem; 
import io.smartpos.core.exceptions.BusinessException;
import io.smartpos.core.exceptions.ValidationException;
import io.smartpos.infrastructure.dao.PurchaseDao;
import io.smartpos.infrastructure.dao.PurchaseItemDao;
import io.smartpos.infrastructure.dao.InventoryMovementDao;
import io.smartpos.infrastructure.datasource.DataSourceProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.Connection;
import java.time.LocalDate;
import java.util.List;

public class PurchaseServiceImpl implements PurchaseService {

    private static final Logger logger =
            LoggerFactory.getLogger(PurchaseServiceImpl.class);

    private final PurchaseDao purchaseDao;
    private final PurchaseItemDao purchaseItemDao;
    private final InventoryMovementDao inventoryDao;
    private final DataSourceProvider dataSourceProvider;

    public PurchaseServiceImpl(
            PurchaseDao purchaseDao,
            PurchaseItemDao purchaseItemDao,
            InventoryMovementDao inventoryDao,
            DataSourceProvider dataSourceProvider
    ) {
        this.purchaseDao = purchaseDao;
        this.purchaseItemDao = purchaseItemDao;
        this.inventoryDao = inventoryDao;
        this.dataSourceProvider = dataSourceProvider;
    }

    @Override
    public void registerPurchase(Purchase purchase) {

        validatePurchase(purchase);

        logger.info("Starting purchase registration");

        try (Connection connection = dataSourceProvider.getConnection()) {

            connection.setAutoCommit(false);

            // 1. Save purchase header
            int purchaseId = purchaseDao.save(purchase, connection);

            // 2. Save items and inventory entries
            for (PurchaseItem item : purchase.getItems()) {

                item.setPurchaseId(purchaseId);
                purchaseItemDao.save(item, connection);

                inventoryDao.registerEntry(
                        item.getProductId(),
                        item.getQuantity(),
                        "PURCHASE",
                        connection
                );
            }

            connection.commit();
            logger.info("Purchase registered successfully");

        } catch (Exception ex) {
            logger.error("Error registering purchase", ex);
            throw new BusinessException("Error registering purchase", ex);
        }
    }

    private void validatePurchase(Purchase purchase) {

        if (purchase == null) {
            throw new ValidationException("Purchase cannot be null");
        }

        if (purchase.getSupplierId() <= 0) {
            throw new ValidationException("Invalid supplier");
        }

        if (purchase.getItems() == null || purchase.getItems().isEmpty()) {
            throw new ValidationException("Purchase must contain items");
        }

        for (PurchaseItem item : purchase.getItems()) {

            if (item.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
                throw new ValidationException(
                        "Quantity must be greater than zero"
                );
            }

            if (item.getUnitCost().compareTo(BigDecimal.ZERO) < 0) {
                throw new ValidationException(
                        "Unit cost cannot be negative"
                );
            }
        }
    }

    @Override
    public Purchase findById(int purchaseId) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public List<Purchase> findByDateRange(LocalDate startDate, LocalDate endDate) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

   
}

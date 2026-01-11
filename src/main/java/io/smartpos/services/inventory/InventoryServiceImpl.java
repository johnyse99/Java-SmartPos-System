package io.smartpos.services.inventory;

import io.smartpos.infrastructure.dao.InventoryMovementDao;
import io.smartpos.infrastructure.datasource.DataSourceProvider;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;

public class InventoryServiceImpl implements InventoryService {

    private final InventoryMovementDao inventoryDao;
    private final DataSourceProvider dataSource;

    public InventoryServiceImpl(InventoryMovementDao inventoryDao, DataSourceProvider dataSource) {
        this.inventoryDao = inventoryDao;
        this.dataSource = dataSource;
    }

    @Override
    public BigDecimal getCurrentStock(int productId) {
        try (Connection conn = dataSource.getConnection()) {
            return inventoryDao.getCurrentStock(productId, conn);
        } catch (SQLException e) {
            throw new RuntimeException("Error getting current stock", e);
        }
    }

    @Override
    public void registerEntry(int productId, BigDecimal quantity, String referenceType) {
        try (Connection conn = dataSource.getConnection()) {
            inventoryDao.registerEntry(productId, quantity, referenceType, conn);
        } catch (SQLException e) {
            throw new RuntimeException("Error registering entry", e);
        }
    }

    @Override
    public void registerExit(int productId, BigDecimal quantity, String referenceType) {
        try (Connection conn = dataSource.getConnection()) {
            inventoryDao.registerExit(productId, quantity, referenceType, conn);
        } catch (SQLException e) {
            throw new RuntimeException("Error registering exit", e);
        }
    }

    public void registerAdjustment(int productId, BigDecimal quantity, String type) {
        String sql = "INSERT INTO inventory_movement (product_id, movement_type, quantity, reference_type) VALUES (?, ?, ?, 'ADJUSTMENT')";
        try (Connection conn = dataSource.getConnection();
                java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            ps.setString(2, type); // 'IN', 'OUT' or 'ADJUSTMENT'
            ps.setBigDecimal(3, quantity);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error registering adjustment", e);
        }
    }
}

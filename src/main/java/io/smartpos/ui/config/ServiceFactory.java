/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package io.smartpos.ui.config;

import io.smartpos.infrastructure.dao.CategoryDao;
import io.smartpos.infrastructure.dao.UnitOfMeasureDao;
import io.smartpos.infrastructure.dao.CustomerDao;
import io.smartpos.infrastructure.dao.SupplierDao;
import io.smartpos.infrastructure.dao.product.CategoryDaoImpl;
import io.smartpos.infrastructure.dao.product.UnitOfMeasureDaoImpl;
import io.smartpos.infrastructure.dao.customer.CustomerDaoImpl;
import io.smartpos.infrastructure.dao.purchase.SupplierDaoImpl;
import io.smartpos.infrastructure.datasource.HikariDataSourceProvider;
import io.smartpos.infrastructure.dao.inventory.InventoryMovementDaoImpl;
import io.smartpos.infrastructure.dao.sale.SaleDaoImpl;
import io.smartpos.infrastructure.dao.sale.SaleItemDaoImpl;
import io.smartpos.services.sale.SaleService;
import io.smartpos.services.sale.SaleServiceImpl;

public class ServiceFactory {

    private static final HikariDataSourceProvider dataSource = new HikariDataSourceProvider();

    static {
        io.smartpos.infrastructure.database.DatabaseMigration.migrate(dataSource);
    }

    public static SaleService saleService() {
        return new SaleServiceImpl(
                new SaleDaoImpl(dataSource),
                new SaleItemDaoImpl(dataSource),
                new InventoryMovementDaoImpl(),
                dataSource);
    }

    public static io.smartpos.services.product.ProductService productService() {
        return new io.smartpos.services.product.ProductServiceImpl(
                new io.smartpos.infrastructure.dao.product.ProductDaoImpl(dataSource));
    }

    public static io.smartpos.services.purchase.PurchaseService purchaseService() {
        return new io.smartpos.services.purchase.PurchaseServiceImpl(
                new io.smartpos.infrastructure.dao.purchase.PurchaseDaoImpl(dataSource),
                new io.smartpos.infrastructure.dao.purchase.PurchaseItemDaoImpl(),
                new InventoryMovementDaoImpl(),
                dataSource);
    }

    public static io.smartpos.services.report.ReportService reportService() {
        return new io.smartpos.services.report.ReportServiceImpl(
                new io.smartpos.infrastructure.dao.report.ReportDaoImpl(dataSource));
    }

    private static final io.smartpos.services.auth.AuthService authService = new io.smartpos.services.auth.AuthServiceImpl(
            new io.smartpos.infrastructure.dao.user.UserDaoImpl(dataSource));

    public static io.smartpos.services.auth.AuthService authService() {
        return authService;
    }

    private static final io.smartpos.services.cash.CashSessionService cashSessionService = new io.smartpos.services.cash.CashSessionServiceImpl(
            new io.smartpos.infrastructure.dao.cash.CashSessionDaoImpl(dataSource), dataSource);

    public static io.smartpos.services.cash.CashSessionService cashSessionService() {
        return cashSessionService;
    }

    public static io.smartpos.infrastructure.dao.UserDao userDao() {
        return new io.smartpos.infrastructure.dao.user.UserDaoImpl(dataSource);
    }

    public static io.smartpos.services.print.PrintService printService() {
        return new io.smartpos.services.print.PrintServiceImpl();
    }

    public static io.smartpos.infrastructure.dao.CashSessionDao cashSessionDao() {
        return new io.smartpos.infrastructure.dao.cash.CashSessionDaoImpl(dataSource);
    }

    public static CategoryDao categoryDao() {
        return new CategoryDaoImpl(dataSource);
    }

    public static UnitOfMeasureDao unitOfMeasureDao() {
        return new UnitOfMeasureDaoImpl(dataSource);
    }

    public static io.smartpos.services.inventory.InventoryService inventoryService() {
        return new io.smartpos.services.inventory.InventoryServiceImpl(new InventoryMovementDaoImpl(), dataSource);
    }

    public static CustomerDao customerDao() {
        return new CustomerDaoImpl(dataSource);
    }

    public static SupplierDao supplierDao() {
        return new SupplierDaoImpl(dataSource);
    }
}

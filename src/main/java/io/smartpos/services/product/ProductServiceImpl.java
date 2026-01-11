package io.smartpos.services.product;

import io.smartpos.core.domain.product.Product;
import io.smartpos.core.exceptions.BusinessException;
import io.smartpos.core.exceptions.ValidationException;
import io.smartpos.infrastructure.dao.ProductDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;

public class ProductServiceImpl implements ProductService {

    private static final Logger logger = LoggerFactory.getLogger(ProductServiceImpl.class);
    private final ProductDao productDao;

    public ProductServiceImpl(ProductDao productDao) {
        this.productDao = productDao;
    }

    @Override
    public void createProduct(Product product) {
        validateProduct(product);

        // Check availability of SKU/Code
        Product existing = productDao.findByCode(product.getCode());
        if (existing != null) {
            throw new BusinessException("Product with code '" + product.getCode() + "' already exists.");
        }

        product.setActive(true);
        product.setCreatedAt(LocalDateTime.now());

        try {
            productDao.save(product);
            logger.info("Product created: {}", product.getName());
        } catch (Exception e) {
            throw new BusinessException("Error creating product", e);
        }
    }

    @Override
    public void updateProduct(Product product) {
        validateProduct(product);

        if (product.getId() <= 0) {
            throw new ValidationException("Invalid Product ID for update");
        }

        try {
            productDao.update(product);
            logger.info("Product updated: {}", product.getId());
        } catch (Exception e) {
            throw new BusinessException("Error updating product", e);
        }
    }

    @Override
    public void deactivateProduct(int productId) {
        Product p = productDao.findById(productId);
        if (p == null) {
            throw new BusinessException("Product not found");
        }
        p.setActive(false);
        productDao.update(p);
        logger.info("Product deactivated: {}", productId);
    }

    @Override
    public Product findById(int productId) {
        return productDao.findById(productId);
    }

    @Override
    public Product findByCode(String code) {
        return productDao.findByCode(code);
    }

    @Override
    public List<Product> findAllActive() {
        return productDao.findAllActive();
    }

    private void validateProduct(Product product) {
        if (product == null) {
            throw new ValidationException("Product cannot be null");
        }
        if (product.getName() == null || product.getName().trim().isEmpty()) {
            throw new ValidationException("Product name is required");
        }
        if (product.getCode() == null || product.getCode().trim().isEmpty()) {
            throw new ValidationException("Product code is required");
        }
        if (product.getCategoryId() == null || product.getCategoryId() <= 0) {
            throw new ValidationException("Product category is required");
        }
        if (product.getUnitId() == null || product.getUnitId() <= 0) {
            throw new ValidationException("Unit of measure is required");
        }
        if (product.getPrice() == null || product.getPrice().signum() < 0) {
            throw new ValidationException("Price must be a positive value");
        }
    }
}

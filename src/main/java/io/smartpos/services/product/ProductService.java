/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package io.smartpos.services.product;

import io.smartpos.core.domain.product.Product;
import java.util.List;

public interface ProductService {

    void createProduct(Product product);

    void updateProduct(Product product);

    void deactivateProduct(int productId);

    Product findById(int productId);

    Product findByCode(String code);

    List<Product> findAllActive();
}

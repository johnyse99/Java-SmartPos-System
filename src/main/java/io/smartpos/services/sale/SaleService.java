/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package io.smartpos.services.sale;

import io.smartpos.core.domain.sale.Sale;
import java.time.LocalDate;
import java.util.List;

public interface SaleService {

    void registerSale(Sale sale);

    Sale findById(int saleId);

    List<Sale> findByDateRange(
            LocalDate startDate,
            LocalDate endDate);

    void cancelSale(int saleId);

    List<Sale> findRecent(int limit);
}

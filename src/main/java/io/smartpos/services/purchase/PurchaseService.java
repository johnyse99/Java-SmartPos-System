/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package io.smartpos.services.purchase;

import io.smartpos.core.domain.purchase.Purchase;
import java.time.LocalDate;
import java.util.List;

public interface PurchaseService {

    void registerPurchase(Purchase purchase);

    Purchase findById(int purchaseId);

    List<Purchase> findByDateRange(
        LocalDate startDate,
        LocalDate endDate
    );
}

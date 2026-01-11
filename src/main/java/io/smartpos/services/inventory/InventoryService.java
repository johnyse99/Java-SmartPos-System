/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package io.smartpos.services.inventory;

import java.math.BigDecimal;

public interface InventoryService {

    BigDecimal getCurrentStock(int productId);

    void registerEntry(
            int productId,
            BigDecimal quantity,
            String referenceType);

    void registerExit(
            int productId,
            BigDecimal quantity,
            String referenceType);

    void registerAdjustment(int productId, BigDecimal quantity, String movementType);
}

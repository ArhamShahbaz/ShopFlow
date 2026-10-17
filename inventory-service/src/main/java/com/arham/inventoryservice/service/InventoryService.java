package com.arham.inventoryservice.service;

import com.arham.inventoryservice.dto.InventoryResponse;
import com.arham.inventoryservice.dto.SetStockRequest;

public interface InventoryService {

    /**
     * Sets or updates the stock quantity for a given SKU code.
     * If the inventory record already exists, updates quantity; otherwise creates a new record.
     *
     * @param request the set stock request payload containing skuCode and quantity
     * @return the updated inventory state
     */
    InventoryResponse setStock(SetStockRequest request);

    /**
     * Reads the current stock level and availability for a given SKU code.
     *
     * @param skuCode the unique SKU code
     * @return the current inventory state
     */
    InventoryResponse getStockBySkuCode(String skuCode);
}

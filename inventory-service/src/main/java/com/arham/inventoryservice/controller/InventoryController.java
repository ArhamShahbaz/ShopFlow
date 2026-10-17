package com.arham.inventoryservice.controller;

import com.arham.inventoryservice.dto.ApiResponse;
import com.arham.inventoryservice.dto.InventoryResponse;
import com.arham.inventoryservice.dto.SetStockRequest;
import com.arham.inventoryservice.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
@Slf4j
public class InventoryController {

    private final InventoryService inventoryService;

    /**
     * Set or update stock for a product SKU
     * POST /api/inventory/stock
     */
    @PostMapping("/stock")
    public ResponseEntity<ApiResponse<InventoryResponse>> setStock(@Valid @RequestBody SetStockRequest request) {
        log.info("REST request to set stock for SKU: {} with quantity: {}", request.getSkuCode(), request.getQuantity());
        InventoryResponse response = inventoryService.setStock(request);
        return ResponseEntity.ok(ApiResponse.ok(response, "Stock updated successfully"));
    }

    /**
     * Read stock for a product SKU
     * GET /api/inventory/{skuCode}
     */
    @GetMapping("/{skuCode}")
    public ResponseEntity<ApiResponse<InventoryResponse>> readStock(@PathVariable String skuCode) {
        log.info("REST request to read stock for SKU: {}", skuCode);
        InventoryResponse response = inventoryService.getStockBySkuCode(skuCode);
        return ResponseEntity.ok(ApiResponse.ok(response, "Stock retrieved successfully"));
    }
}

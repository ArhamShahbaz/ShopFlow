package com.arham.inventoryservice.service.impl;

import com.arham.inventoryservice.dto.InventoryResponse;
import com.arham.inventoryservice.dto.SetStockRequest;
import com.arham.inventoryservice.entity.Inventory;
import com.arham.inventoryservice.exception.ResourceNotFoundException;
import com.arham.inventoryservice.mapper.InventoryMapper;
import com.arham.inventoryservice.repository.InventoryRepository;
import com.arham.inventoryservice.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final InventoryMapper inventoryMapper;

    @Override
    @Transactional
    public InventoryResponse setStock(SetStockRequest request) {
        log.info("Setting stock for SKU: {} with quantity: {}", request.getSkuCode(), request.getQuantity());

        Inventory inventory = inventoryRepository.findBySkuCode(request.getSkuCode())
                .map(existing -> {
                    log.debug("Found existing inventory record for SKU: {}, updating quantity from {} to {}",
                            existing.getSkuCode(), existing.getQuantity(), request.getQuantity());
                    existing.setQuantity(request.getQuantity());
                    return existing;
                })
                .orElseGet(() -> {
                    log.debug("No existing inventory record for SKU: {}, creating new entry", request.getSkuCode());
                    return inventoryMapper.toEntity(request);
                });

        Inventory savedInventory = inventoryRepository.save(inventory);
        log.info("Stock updated successfully for SKU: {}, current quantity: {}",
                savedInventory.getSkuCode(), savedInventory.getQuantity());

        return inventoryMapper.toResponse(savedInventory);
    }

    @Override
    public InventoryResponse getStockBySkuCode(String skuCode) {
        log.debug("Reading stock for SKU: {}", skuCode);

        Inventory inventory = inventoryRepository.findBySkuCode(skuCode)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory", "skuCode", skuCode));

        return inventoryMapper.toResponse(inventory);
    }
}

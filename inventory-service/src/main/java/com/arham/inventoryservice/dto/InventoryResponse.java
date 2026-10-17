package com.arham.inventoryservice.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryResponse {

    private Long id;
    private String skuCode;
    private Integer quantity;
    private boolean inStock;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

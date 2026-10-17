package com.arham.inventoryservice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SetStockRequest {

    @NotBlank(message = "SKU code must not be blank")
    @Pattern(regexp = "^[A-Za-z0-9_-]{3,50}$", message = "SKU code must be alphanumeric with optional dashes or underscores (3-50 chars)")
    private String skuCode;

    @NotNull(message = "Quantity is required")
    @Min(value = 0, message = "Quantity cannot be negative")
    private Integer quantity;
}

package com.arham.inventoryservice.mapper;

import com.arham.inventoryservice.dto.InventoryResponse;
import com.arham.inventoryservice.dto.SetStockRequest;
import com.arham.inventoryservice.entity.Inventory;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface InventoryMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Inventory toEntity(SetStockRequest request);

    @Mapping(target = "inStock", expression = "java(entity.getQuantity() != null && entity.getQuantity() > 0)")
    InventoryResponse toResponse(Inventory entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromRequest(SetStockRequest request, @MappingTarget Inventory entity);
}

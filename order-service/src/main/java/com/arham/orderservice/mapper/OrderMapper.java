package com.arham.orderservice.mapper;

import com.arham.orderservice.dto.OrderItemRequest;
import com.arham.orderservice.dto.OrderItemResponse;
import com.arham.orderservice.dto.OrderRequest;
import com.arham.orderservice.dto.OrderResponse;
import com.arham.orderservice.entity.Order;
import com.arham.orderservice.entity.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrderMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "totalAmount", ignore = true)
    @Mapping(target = "orderItems", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Order toEntity(OrderRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "order", ignore = true)
    OrderItem toItemEntity(OrderItemRequest request);

    OrderResponse toResponse(Order entity);

    List<OrderResponse> toResponseList(List<Order> entities);

    OrderItemResponse toItemResponse(OrderItem entity);

    List<OrderItemResponse> toItemResponseList(List<OrderItem> entities);
}

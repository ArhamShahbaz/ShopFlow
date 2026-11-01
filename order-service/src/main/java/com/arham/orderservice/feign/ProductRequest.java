package com.arham.orderservice.feign;

import com.arham.orderservice.dto.ApiResponse;
import com.arham.orderservice.dto.ProductResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "PRODUCT-SERVICE")
public interface ProductRequest {

    @GetMapping("/api/v1/products/sku/{code}")
    ResponseEntity<ApiResponse<ProductResponse>> getProductBySkuCode(@PathVariable("code") String code);

    @GetMapping("/api/v1/products/sku/batch")
    ResponseEntity<ApiResponse<List<ProductResponse>>> getProductsBySkuCode(@RequestParam("code") List<String> code);
}


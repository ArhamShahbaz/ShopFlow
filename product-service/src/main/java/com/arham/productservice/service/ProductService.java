package com.arham.productservice.service;

import com.arham.productservice.dto.PageResponse;
import com.arham.productservice.dto.ProductRequest;
import com.arham.productservice.dto.ProductResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductService {

    ProductResponse createProduct(ProductRequest request);

    List<ProductResponse> createProducts(List<ProductRequest> requests);

    ProductResponse getProductById(Long id);

    List<ProductResponse> getProductsByName(String name);

    PageResponse<ProductResponse> getProductsByName(String name, Pageable pageable);

    PageResponse<ProductResponse> getAllProducts(Pageable pageable);

    ProductResponse updateProduct(Long id, ProductRequest request);

    void deleteProduct(Long id);
}

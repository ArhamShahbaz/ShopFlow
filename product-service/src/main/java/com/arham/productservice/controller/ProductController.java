package com.arham.productservice.controller;

import com.arham.productservice.dto.ApiResponse;
import com.arham.productservice.dto.PageResponse;
import com.arham.productservice.dto.ProductRequest;
import com.arham.productservice.dto.ProductResponse;
import com.arham.productservice.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Slf4j
public class ProductController {

    private final ProductService productService;

    /**
     * Create a single product
     * POST /api/v1/products
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(@Valid @RequestBody ProductRequest request) {
        log.info("REST request to create product: {}", request.getName());
        ProductResponse response = productService.createProduct(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.created(response, "Product created successfully"));
    }

    /**
     * Batch create products
     * POST /api/v1/products/batch
     */
    @PostMapping("/batch")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> createProducts(
            @Valid @RequestBody List<@Valid ProductRequest> requests) {
        log.info("REST request to create {} products in batch", requests.size());
        List<ProductResponse> responses = productService.createProducts(requests);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.created(responses, "Products created successfully"));
    }

    /**
     * Fetch a product by ID
     * GET /api/v1/products/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductById(@PathVariable Long id) {
        log.info("REST request to get product by ID: {}", id);
        ProductResponse response = productService.getProductById(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * Fetch products by name (search query)
     * GET /api/v1/products/search?name=xyz&page=0&size=10
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PageResponse<ProductResponse>>> searchProductsByName(
            @RequestParam String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        log.info("REST request to search products by name: '{}', page: {}, size: {}", name, page, size);
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        PageResponse<ProductResponse> response = productService.getProductsByName(name, pageable);
        return ResponseEntity.ok(ApiResponse.ok(response, "Products retrieved successfully"));
    }

    /**
     * Fetch all products with pagination and sorting
     * GET /api/v1/products?page=0&size=10&sortBy=id&sortDir=asc
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ProductResponse>>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        log.info("REST request to get all products with pagination: page={}, size={}, sortBy={}, sortDir={}",
                page, size, sortBy, sortDir);
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        PageResponse<ProductResponse> response = productService.getAllProducts(pageable);
        return ResponseEntity.ok(ApiResponse.ok(response, "Products retrieved successfully"));
    }

    /**
     * Update an existing product
     * PUT /api/v1/products/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request) {
        log.info("REST request to update product with ID: {}", id);
        ProductResponse response = productService.updateProduct(id, request);
        return ResponseEntity.ok(ApiResponse.ok(response, "Product updated successfully"));
    }

    /**
     * Delete product by ID
     * DELETE /api/v1/products/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Long id) {
        log.info("REST request to delete product with ID: {}", id);
        productService.deleteProduct(id);
        return ResponseEntity.ok(ApiResponse.ok(null, "Product deleted successfully"));
    }
}

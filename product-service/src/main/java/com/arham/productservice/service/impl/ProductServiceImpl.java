package com.arham.productservice.service.impl;

import com.arham.productservice.dto.PageResponse;
import com.arham.productservice.dto.ProductRequest;
import com.arham.productservice.dto.ProductResponse;
import com.arham.productservice.entity.Product;
import com.arham.productservice.exception.DuplicateResourceException;
import com.arham.productservice.exception.ResourceNotFoundException;
import com.arham.productservice.mapper.ProductMapper;
import com.arham.productservice.repository.ProductRepository;
import com.arham.productservice.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Override
    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        log.info("Creating product with SKU: {}", request.getSkuCode());

        if (productRepository.existsBySkuCode(request.getSkuCode())) {
            throw new DuplicateResourceException("Product", "skuCode", request.getSkuCode());
        }

        Product product = productMapper.toEntity(request);
        Product savedProduct = productRepository.save(product);

        log.info("Product created successfully with ID: {}", savedProduct.getId());
        return productMapper.toResponse(savedProduct);
    }

    @Override
    @Transactional
    public List<ProductResponse> createProducts(List<ProductRequest> requests) {
        log.info("Creating batch of {} products", requests != null ? requests.size() : 0);

        if (requests == null || requests.isEmpty()) {
            return List.of();
        }

        List<Product> productsToSave = new ArrayList<>();
        for (ProductRequest req : requests) {
            if (productRepository.existsBySkuCode(req.getSkuCode())) {
                throw new DuplicateResourceException("Product", "skuCode", req.getSkuCode());
            }
            productsToSave.add(productMapper.toEntity(req));
        }

        List<Product> savedProducts = productRepository.saveAll(productsToSave);
        log.info("Batch of {} products created successfully", savedProducts.size());
        return productMapper.toResponseList(savedProducts);
    }

    @Override
    public ProductResponse getProductById(Long id) {
        log.debug("Fetching product with ID: {}", id);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        return productMapper.toResponse(product);
    }

    @Override
    public List<ProductResponse> getProductsByName(String name) {
        log.debug("Fetching products matching name: {}", name);
        List<Product> products = productRepository.findByNameContainingIgnoreCase(name);
        return productMapper.toResponseList(products);
    }

    @Override
    public PageResponse<ProductResponse> getProductsByName(String name, Pageable pageable) {
        log.debug("Fetching paged products matching name: {}, page: {}", name, pageable.getPageNumber());
        Page<Product> productPage = productRepository.findByNameContainingIgnoreCase(name, pageable);
        Page<ProductResponse> dtoPage = productPage.map(productMapper::toResponse);
        return PageResponse.from(dtoPage);
    }

    @Override
    public PageResponse<ProductResponse> getAllProducts(Pageable pageable) {
        log.debug("Fetching all products with pagination: {}", pageable);
        Page<Product> productPage = productRepository.findAll(pageable);
        Page<ProductResponse> dtoPage = productPage.map(productMapper::toResponse);
        return PageResponse.from(dtoPage);
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        log.info("Updating product with ID: {}", id);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        // If skuCode is being updated, verify it doesn't collide with another product
        if (!product.getSkuCode().equalsIgnoreCase(request.getSkuCode())
                && productRepository.existsBySkuCode(request.getSkuCode())) {
            throw new DuplicateResourceException("Product", "skuCode", request.getSkuCode());
        }

        productMapper.updateEntityFromRequest(request, product);
        Product updatedProduct = productRepository.save(product);

        log.info("Product updated successfully with ID: {}", updatedProduct.getId());
        return productMapper.toResponse(updatedProduct);
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        log.info("Deleting product with ID: {}", id);
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product", "id", id);
        }
        productRepository.deleteById(id);
        log.info("Product deleted successfully with ID: {}", id);
    }
}

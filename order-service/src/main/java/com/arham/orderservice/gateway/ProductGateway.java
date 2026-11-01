package com.arham.orderservice.gateway;

import com.arham.orderservice.dto.ApiResponse;
import com.arham.orderservice.dto.ProductResponse;
import com.arham.orderservice.exception.ResourceNotFoundException;
import com.arham.orderservice.exception.ServiceUnavailableException;
import com.arham.orderservice.feign.ProductRequest;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Gateway / Anti-Corruption Layer for interacting with the Product Service.
 * Encapsulates OpenFeign client invocation, Resilience4j Circuit Breaker protection,
 * response unwrapping, and product validation logic.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ProductGateway {

    private static final String PRODUCT_CB = "productServiceCircuitBreaker";

    private final ProductRequest productRequest;

    /**
     * Fetches products for the specified SKU codes and validates that every requested SKU
     * is available in the catalog. Returns a map keyed by SKU code for fast lookup.
     *
     * @param skuCodes List of unique SKU codes
     * @return Map of skuCode to ProductResponse
     * @throws ResourceNotFoundException   if any requested SKU code is not found
     * @throws ServiceUnavailableException if the product service is down or circuit is open
     */
    @CircuitBreaker(name = PRODUCT_CB, fallbackMethod = "fallbackFetchAndValidateProducts")
    public Map<String, ProductResponse> fetchAndValidateProducts(List<String> skuCodes) {
        log.info("Fetching and validating {} products from product-service for SKU codes: {}", skuCodes.size(), skuCodes);

        if (skuCodes.isEmpty()) {
            return Collections.emptyMap();
        }

        List<ProductResponse> products = fetchProductsFromClient(skuCodes);

        Map<String, ProductResponse> productMap = products.stream()
                .filter(p -> p.getSkuCode() != null)
                .collect(Collectors.toMap(ProductResponse::getSkuCode, Function.identity(), (p1, p2) -> p1));

        List<String> missingSkus = skuCodes.stream().filter(sku -> !productMap.containsKey(sku)).toList();

        if (!missingSkus.isEmpty()) {
            log.error("Products not found for SKU codes: {}", missingSkus);
            throw new ResourceNotFoundException("Products not found for SKU codes: " + missingSkus);
        }

        return productMap;
    }

    /**
     * Fallback for fetchAndValidateProducts when Circuit Breaker trips or remote call fails.
     */
    public Map<String, ProductResponse> fallbackFetchAndValidateProducts(List<String> skuCodes, Throwable throwable) {
        log.error("Fallback triggered for fetchAndValidateProducts with SKU codes: {}. Reason: {}", skuCodes, throwable.getMessage());
        if (throwable instanceof ResourceNotFoundException notFound) {
            throw notFound;
        }
        throw new ServiceUnavailableException("Product Service is currently unavailable. Please try again later.", throwable);
    }

    /**
     * Fetches a list of products by their SKU codes without mandatory missing-check.
     * Protected with Resilience4j Circuit Breaker.
     *
     * @param codes List of SKU codes
     * @return List of ProductResponse
     */
    @CircuitBreaker(name = PRODUCT_CB, fallbackMethod = "fallbackGetProductsBySkuCode")
    public List<ProductResponse> getProductsBySkuCode(List<String> codes) {
        log.info("Fetching products from product-service for SKU codes: {}", codes);
        if (codes == null || codes.isEmpty()) {
            return Collections.emptyList();
        }
        return fetchProductsFromClient(codes);
    }

    /**
     * Fallback for getProductsBySkuCode when Circuit Breaker trips or remote call fails.
     */
    public List<ProductResponse> fallbackGetProductsBySkuCode(List<String> codes, Throwable throwable) {
        log.error("Fallback triggered for getProductsBySkuCode with SKU codes: {}. Reason: {}", codes, throwable.getMessage());
        if (throwable instanceof ResourceNotFoundException notFound) {
            throw notFound;
        }
        throw new ServiceUnavailableException("Product Service is currently unavailable. Please try again later.", throwable);
    }

    /**
     * Fetches a single product by SKU code.
     * Protected with Resilience4j Circuit Breaker.
     *
     * @param code The SKU code
     * @return ProductResponse
     */
    @CircuitBreaker(name = PRODUCT_CB, fallbackMethod = "fallbackGetSingleProductBySkuCode")
    public ProductResponse getProductBySkuCode(String code) {
        log.info("Fetching product from product-service for SKU code: {}", code);
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("SKU code must not be null or blank");
        }

        try {
            ResponseEntity<ApiResponse<ProductResponse>> responseEntity = productRequest.getProductBySkuCode(code);
            if (responseEntity == null || responseEntity.getBody() == null || responseEntity.getBody().getData() == null) {
                log.warn("Product service returned null/empty body for SKU code: {}", code);
                throw new ResourceNotFoundException("Product", "skuCode", code);
            }
            return responseEntity.getBody().getData();
        } catch (FeignException.NotFound ex) {
            log.warn("Product service returned 404 for SKU code: {}", code);
            throw new ResourceNotFoundException("Product", "skuCode", code);
        } catch (FeignException ex) {
            log.error("Feign client error while communicating with product-service for SKU code: {}", code, ex);
            throw ex;
        }
    }

    /**
     * Fallback for getProductBySkuCode when Circuit Breaker trips or remote call fails.
     */
    public ProductResponse fallbackGetSingleProductBySkuCode(String code, Throwable throwable) {
        log.error("Fallback triggered for getProductBySkuCode with SKU code: {}. Reason: {}", code, throwable.getMessage());
        if (throwable instanceof ResourceNotFoundException notFound) {
            throw notFound;
        }
        throw new ServiceUnavailableException("Product Service is currently unavailable. Please try again later.", throwable);
    }

    /**
     * Internal helper to execute batch Feign call and unpack response.
     */
    private List<ProductResponse> fetchProductsFromClient(List<String> skuCodes) {
        ResponseEntity<ApiResponse<List<ProductResponse>>> responseEntity;
        try {
            responseEntity = productRequest.getProductsBySkuCode(skuCodes);
        } catch (FeignException.NotFound ex) {
            log.error("Product service returned 404 for requested SKU codes: {}", skuCodes, ex);
            throw new ResourceNotFoundException("One or more products not found for SKU codes: " + skuCodes);
        } catch (FeignException ex) {
            log.error("Feign client error while communicating with product-service for SKU codes: {}", skuCodes, ex);
            throw ex;
        }

        if (responseEntity == null || responseEntity.getBody() == null || responseEntity.getBody().getData() == null) {
            log.error("Received null or empty response body from product-service for SKU codes: {}", skuCodes);
            throw new ResourceNotFoundException("Products not found for SKU codes: " + skuCodes);
        }

        return responseEntity.getBody().getData();
    }
}

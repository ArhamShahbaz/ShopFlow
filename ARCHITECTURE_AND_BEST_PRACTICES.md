# ShopFlow Microservices Architecture & Best Practices Blueprint

> **System Standard Operating Procedure (SOP)**  
> This document defines the architectural patterns, directory structure, coding standards, and best practices established in `product-service`.  
> **Mandatory Rule:** Any new microservice or feature implementation in ShopFlow (e.g., `inventory-service`, `order-service`, `payment-service`, `notification-service`) **MUST** follow this exact standard.

---

## 1. High-Level Architecture Overview

ShopFlow microservices follow the **Layered Clean MVC Pattern** with clear separation of concerns:
- **Presentation Layer (`controller`):** Exposes clean RESTful endpoints, handles HTTP status codes, and delegates directly to the service layer.
- **Service Contract & Business Logic (`service`, `service.impl`):** Defined via interfaces and implemented in separate `impl` subpackages with explicit transactional boundaries (`@Transactional`).
- **Data Access Layer (`repository`):** Spring Data JPA repositories with query-method naming conventions, pagination, and projection support.
- **Domain Model (`entity`):** JPA Entities representing PostgreSQL persistence schemas with table indexes, immutable creation dates, and updated audit timestamps.
- **Data Transfer Objects (`dto`):** Immutable, validated input requests and output response envelopes. Domain entities are **NEVER** exposed directly via HTTP endpoints.
- **Mapping Layer (`mapper`):** Compile-time, type-safe mapping using **MapStruct** with Lombok bindings. No manual builder/setter boilerplate in service classes.
- **Error Handling (`exception`):** Centralized `@RestControllerAdvice` converting domain and validation errors into predictable, structured HTTP envelopes.

---

## 2. Standard Directory & Package Structure

For any microservice `com.arham.<service_name>`:

```
<service-dir>/src/main/java/com/arham/<service_name>/
├── controller/
│   └── <Entity>Controller.java             # REST Controller
├── service/
│   ├── <Entity>Service.java                # Interface contract
│   └── impl/
│       └── <Entity>ServiceImpl.java        # Business logic & transactions
├── repository/
│   └── <Entity>Repository.java             # Spring Data JPA Repository
├── entity/
│   └── <Entity>.java                       # JPA Entity definition
├── dto/
│   ├── <Entity>Request.java                # Validated Request DTO
│   ├── <Entity>Response.java               # Sanitized Response DTO
│   ├── PageResponse.java                   # Generic pagination response wrapper
│   └── ApiResponse.java                    # Universal response envelope
├── mapper/
│   └── <Entity>Mapper.java                 # MapStruct interface
├── exception/
│   ├── ResourceNotFoundException.java      # 404 handler
│   ├── DuplicateResourceException.java     # 409 conflict handler
│   └── GlobalExceptionHandler.java         # Centralized @RestControllerAdvice
└── <ServiceApplication>.java               # Spring Boot entry point
```

---

## 3. Maven Build & Annotation Processing Standard

Every service `pom.xml` must configure the Java compiler plugin with the correct annotation processor execution order:
1. `lombok`
2. `lombok-mapstruct-binding`
3. `mapstruct-processor`

### Required Maven Dependencies:
```xml
<properties>
    <java.version>17</java.version>
    <org.mapstruct.version>1.6.3</org.mapstruct.version>
    <lombok.version>1.18.36</lombok.version>
    <lombok-mapstruct-binding.version>0.2.0</lombok-mapstruct-binding.version>
</properties>

<dependencies>
    <!-- Spring Data JPA -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>

    <!-- Spring Web -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-webmvc</artifactId>
    </dependency>

    <!-- Jakarta Validation -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>

    <!-- PostgreSQL Driver -->
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
        <scope>runtime</scope>
    </dependency>

    <!-- Lombok -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <version>${lombok.version}</version>
        <optional>true</optional>
    </dependency>

    <!-- MapStruct -->
    <dependency>
        <groupId>org.mapstruct</groupId>
        <artifactId>mapstruct</artifactId>
        <version>${org.mapstruct.version}</version>
    </dependency>
</dependencies>

<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <configuration>
                <source>${java.version}</source>
                <target>${java.version}</target>
                <annotationProcessorPaths>
                    <path>
                        <groupId>org.projectlombok</groupId>
                        <artifactId>lombok</artifactId>
                        <version>${lombok.version}</version>
                    </path>
                    <path>
                        <groupId>org.projectlombok</groupId>
                        <artifactId>lombok-mapstruct-binding</artifactId>
                        <version>${lombok-mapstruct-binding.version}</version>
                    </path>
                    <path>
                        <groupId>org.mapstruct</groupId>
                        <artifactId>mapstruct-processor</artifactId>
                        <version>${org.mapstruct.version}</version>
                    </path>
                </annotationProcessorPaths>
            </configuration>
        </plugin>
    </plugins>
</build>
```

---

## 4. Layer-by-Layer Implementation Standards

### 4.1. Entity Standards (`entity/`)
- Always specify table names explicitly: `@Table(name = "...", indexes = { ... })`.
- Include table-level database indexes on frequently queried/unique columns (`@Index`).
- Use `@CreationTimestamp` and `@UpdateTimestamp` for automatic audit trail fields.
- Combine `@Getter`, `@Setter`, `@NoArgsConstructor`, `@AllArgsConstructor`, and `@Builder`.
- Use `BigDecimal` with explicit `@Column(precision = 19, scale = 2)` for any financial or monetary amounts.

```java
@Entity
@Table(name = "product", indexes = {
    @Index(name = "idx_product_sku_code", columnList = "sku_code", unique = true),
    @Index(name = "idx_product_name", columnList = "name")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "sku_code", nullable = false, unique = true)
    private String skuCode;

    @Column(name = "price", nullable = false, precision = 19, scale = 2)
    private BigDecimal price;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
```

---

### 4.2. Repository Standards (`repository/`)
- Extend `JpaRepository<Entity, ID>`.
- Annotate with `@Repository`.
- Provide query methods for existence checks (`existsBy...`) to optimize validation before mutations.
- Use Spring Data naming conventions with `IgnoreCase` and `Containing` for flexible search.
- Supply `Pageable` overloads for list/search queries.

```java
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findBySkuCode(String skuCode);
    boolean existsBySkuCode(String skuCode);
    List<Product> findByNameContainingIgnoreCase(String name);
    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
```

---

### 4.3. DTO Standards (`dto/`)
- Separate **Request** (`ProductRequest`) and **Response** (`ProductResponse`) DTOs.
- Always validate incoming inputs using Jakarta Bean Validation:
  - `@NotBlank`, `@Size(min, max)` for strings.
  - `@NotNull`, `@DecimalMin`, `@Digits` for numbers and monetary values.
  - `@Pattern` for structured business codes (SKU, IDs).
- **Universal Response Wrapper (`ApiResponse<T>`):**
  - Standard fields: `success` (boolean), `message` (String), `data` (T), `errors` (Map), `timestamp` (LocalDateTime).
  - Use `@JsonInclude(JsonInclude.Include.NON_NULL)` to exclude empty error fields on success.
- **Universal Pagination Wrapper (`PageResponse<T>`):**
  - Decouples Spring Data `org.springframework.data.domain.Page` from HTTP JSON serialization.
  - Exposes: `content`, `pageNumber`, `pageSize`, `totalElements`, `totalPages`, `first`, `last`, `empty`.
  - Includes a static factory method: `PageResponse.from(Page<T> page)`.

---

### 4.4. Mapping Standards with MapStruct (`mapper/`)
- Set `@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)`.
- Ignore primary key `id`, `createdAt`, and `updatedAt` during creation entity mapping.
- For updates, use `@BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)` with `@MappingTarget` to perform clean partial/in-place entity updates.
- Provide batch mapping: `List<ResponseDTO> toResponseList(List<Entity> entities)`.

```java
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProductMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Product toEntity(ProductRequest request);

    ProductResponse toResponse(Product entity);
    List<ProductResponse> toResponseList(List<Product> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromRequest(ProductRequest request, @MappingTarget Product entity);
}
```

---

### 4.5. Service Standards (`service/` & `service/impl/`)
- **Interface Segregation:** Define business operations in the `service` interface; encapsulate implementation in `service.impl`.
- **Constructor Injection:** Use Lombok's `@RequiredArgsConstructor` for final fields. Never use field `@Autowired`.
- **Transactional Discipline:**
  - Class-level annotation: `@Transactional(readOnly = true)`.
  - Method-level annotation: `@Transactional` on write methods (`create`, `update`, `delete`, `batch`).
- **Logging:** Use `@Slf4j` with structured logs (`log.info`, `log.debug`) at business milestones.
- **Fail-Fast Validation:** Perform domain uniqueness/existence checks prior to DB operations and throw custom domain exceptions.
- **Batch Support:** Always implement batch creation (`createProducts(List<ProductRequest>)`) efficiently using `saveAll()`.

---

### 4.6. Controller Standards (`controller/`)
- Annotate with `@RestController`, `@RequestMapping("/api/<resource>")`, `@RequiredArgsConstructor`, and `@Slf4j`.
- Always validate incoming payload bodies using `@Valid @RequestBody`.
- Return proper HTTP status codes:
  - `201 CREATED` for single and batch creations.
  - `200 OK` for reads, updates, and deletes.
  - `400 BAD REQUEST` for validation errors.
  - `404 NOT FOUND` when resource does not exist.
  - `409 CONFLICT` on unique constraint violations.
- Wrap all responses inside `ResponseEntity<ApiResponse<T>>`.
- Support configurable pagination (`page`, `size`) and dynamic sorting (`sortBy`, `sortDir`).

```java
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Slf4j
public class ProductController {
    private final ProductService productService;

    @PostMapping
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(@Valid @RequestBody ProductRequest request) { ... }

    @PostMapping("/batch")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> createProducts(@Valid @RequestBody List<@Valid ProductRequest> requests) { ... }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductById(@PathVariable Long id) { ... }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PageResponse<ProductResponse>>> searchProductsByName(...) { ... }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ProductResponse>>> getAllProducts(...) { ... }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(@PathVariable Long id, @Valid @RequestBody ProductRequest request) { ... }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Long id) { ... }
}
```

---

### 4.7. Exception Handling Standards (`exception/`)
- Define specific unchecked exceptions extending `RuntimeException`:
  - `ResourceNotFoundException`: For missing entities (404).
  - `DuplicateResourceException`: For duplicate keys/SKUs/emails (409).
- Central `@RestControllerAdvice` (`GlobalExceptionHandler`):
  - Maps `ResourceNotFoundException` -> `HttpStatus.NOT_FOUND`.
  - Maps `DuplicateResourceException` -> `HttpStatus.CONFLICT`.
  - Maps `MethodArgumentNotValidException` -> `HttpStatus.BAD_REQUEST` with a structured `Map<String, String>` of field-level validation errors.
  - Maps `IllegalArgumentException` -> `HttpStatus.BAD_REQUEST`.
  - Maps general `Exception` -> `HttpStatus.INTERNAL_SERVER_ERROR`.

---

## 5. Execution Checklist for New Tasks

Whenever requested to implement or update a service:

1. [ ] **Verify / Update `pom.xml`**: Check for JPA, Validation, Postgres, Lombok, MapStruct, and correct `annotationProcessorPaths`.
2. [ ] **Define Entity**: Table name, unique indexes, constraints, audit timestamps, and `@Builder`.
3. [ ] **Create Repository**: Custom finders, exists queries, and `Pageable` methods.
4. [ ] **Create DTOs**: Request DTO (with `@NotBlank`, `@Size`, `@NotNull`, etc.), Response DTO, `PageResponse<T>`, and `ApiResponse<T>`.
5. [ ] **Define MapStruct Mapper**: Spring component model, ignore unmapped/audit fields, update mapping.
6. [ ] **Write Custom Exceptions & Global Handler**: Ensure consistent JSON error responses.
7. [ ] **Implement Service Interface & Implementation**: `@Transactional(readOnly = true)`, write `@Transactional`, logging, uniqueness checks, batch operations.
9. [ ] **Compile & Test**: Verify clean compilation (`./mvnw compile`) and test against running databases.

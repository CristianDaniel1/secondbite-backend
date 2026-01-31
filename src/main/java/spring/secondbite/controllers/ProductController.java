package spring.secondbite.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import spring.secondbite.dtos.PageResponseDto;
import spring.secondbite.dtos.products.ProductDto;
import spring.secondbite.dtos.products.ProductResponseDto;
import spring.secondbite.entities.enums.Category;
import spring.secondbite.services.ProductService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService service;

    @GetMapping
    public ResponseEntity<PageResponseDto<ProductResponseDto>> getAllProducts(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "category", required = false) Category category,
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "limit", defaultValue = "12") Integer limit) {

        PageResponseDto<ProductResponseDto> products = service
                .findAllProducts(name, category, page, limit);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/marketer/{id}")
    @PreAuthorize("hasRole('MARKETER')")
    public ResponseEntity<List<ProductResponseDto>> getProductsByMarketer(@PathVariable("id") UUID id) {
        List<ProductResponseDto> products = service.findProductsByMarketer(id);
        return ResponseEntity.ok(products);
    }

    @GetMapping("{id}")
    public ResponseEntity<ProductResponseDto> getProductById(@PathVariable("id") UUID id) {
        ProductResponseDto product = service.findProductById(id);
        return ResponseEntity.ok(product);
    }

    @PostMapping
    @PreAuthorize("hasRole('MARKETER')")
    public ResponseEntity<ProductResponseDto> createProduct(@RequestBody @Valid ProductDto dto) {
        ProductResponseDto createdProduct = service.createProduct(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProduct);
    }

    @PutMapping("{id}")
    @PreAuthorize("hasRole('MARKETER')")
    public ResponseEntity<ProductResponseDto> updateProduct(
            @PathVariable("id") UUID id,
            @RequestBody @Valid ProductDto dto
    ) {
        ProductResponseDto updatedProduct = service.updateProduct(id, dto);
        return ResponseEntity.ok(updatedProduct);
    }

    @DeleteMapping("{id}")
    @PreAuthorize("hasAnyRole('MARKETER', 'ADMIN')")
    public ResponseEntity<Void> deleteProduct(@PathVariable("id") UUID id) {
        service.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}

package spring.secondbite.dtos.products;

import spring.secondbite.entities.enums.Category;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ProductResponseDto (
        UUID id,
        String name,
        String description,
        String sizeType,
        LocalDate validation,
        BigDecimal price,
        Category category,
        Integer quantity,
        List<String> images,
        LocalDateTime modifiedAt,
        LocalDateTime createdAt
) {}

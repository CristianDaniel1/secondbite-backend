package spring.secondbite.dtos.products;

import spring.secondbite.entities.enums.Category;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ProductDetailResponseDto (
        UUID id,
        String name,
        String description,
        String sizeType,
        LocalDate validation,
        BigDecimal price,
        BigDecimal originalPrice,
        Integer discountPercentage,
        Category category,
        Integer quantity,
        List<String> images,
        LocalDateTime modifiedAt,
        LocalDateTime createdAt,
        UUID marketerId,
        String marketerName,
        String stallName,
        Boolean isAutoDiscount,
        Integer manualDiscountPercentage,
        Double marketerRating
) {}

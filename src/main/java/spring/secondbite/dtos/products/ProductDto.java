package spring.secondbite.dtos.products;

import jakarta.validation.constraints.*;
import spring.secondbite.entities.Marketer;
import spring.secondbite.entities.enums.Category;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record ProductDto(
        @NotBlank(message = "Name is required")
        @Size(max = 100, message = "Name must be at most 100 characters")
        String name,

        String description,

        @NotBlank(message = "SizeType is required")
        @Size(max = 10, message = "SizeType must be at most 10 characters")
        String sizeType,

        @NotNull(message = "Validation date is required")
        @Future(message = "Validation date must be in the future")
        LocalDate validation,

        @PositiveOrZero(message = "Price must be zero or positive")
        BigDecimal price,

        @NotNull(message = "Category is required")
        Category category,

        @Min(value = 1, message = "Quantity must be at least 1")
        Integer quantity,

        Boolean isAutoDiscount,

        @Min(value = 0, message = "Discount must be at least 0")
        @Max(value = 100, message = "Discount must be at most 100")
        Integer manualDiscountPercentage
) {}

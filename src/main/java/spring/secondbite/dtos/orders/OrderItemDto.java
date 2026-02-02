package spring.secondbite.dtos.orders;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderItemDto(
        UUID productId,
        String productName,
        String productImageUrl,
        Integer quantity,
        BigDecimal price,
        BigDecimal subTotal
) {}
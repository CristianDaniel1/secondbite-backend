package spring.secondbite.dtos.cart;

import spring.secondbite.dtos.products.ProductResponseDto;

import java.math.BigDecimal;
import java.util.UUID;

public record CartItemResponseDto(
        UUID id,
        ProductResponseDto product,
        Integer quantity,
        BigDecimal subTotal
) {}

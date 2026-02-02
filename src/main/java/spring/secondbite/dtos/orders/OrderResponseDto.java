package spring.secondbite.dtos.orders;

import spring.secondbite.entities.enums.Status;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record OrderResponseDto(
        UUID id,
        String consumerName,
        String marketerName,
        Status status,
        List<OrderItemDto> items,
        BigDecimal totalAmount,
        LocalDateTime createdAt
) {}

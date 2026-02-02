package spring.secondbite.dtos.orders;

import jakarta.validation.constraints.NotNull;
import spring.secondbite.entities.enums.Status;

public record UpdateOrderStatusDto(
        @NotNull(message = "Status is required")
        Status status
) {}

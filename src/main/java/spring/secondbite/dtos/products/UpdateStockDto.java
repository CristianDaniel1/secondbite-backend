package spring.secondbite.dtos.products;

import jakarta.validation.constraints.NotNull;

public record UpdateStockDto(
        @NotNull(message = "O ajuste de quantidade é obrigatório")
        Integer quantityAdjustment
) {}
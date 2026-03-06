package spring.secondbite.dtos.marketers;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record FlashSaleDto(
        @NotEmpty(message = "A lista de produtos não pode estar vazia")
        List<UUID> productIds,

        @NotNull(message = "A porcentagem de desconto é obrigatória")
        @Min(value = 0, message = "O desconto não pode ser menor que 0%")
        @Max(value = 100, message = "O desconto não pode ser maior que 100%")
        Integer discountPercentage
) {}
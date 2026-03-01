package spring.secondbite.dtos.orders;

import jakarta.validation.constraints.NotBlank;

public record CompleteOrderDto(
        @NotBlank(message = "O código de entrega é obrigatório")
        String deliveryCode
) {}
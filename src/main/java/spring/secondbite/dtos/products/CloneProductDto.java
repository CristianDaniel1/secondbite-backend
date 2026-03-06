package spring.secondbite.dtos.products;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CloneProductDto(
        @NotNull(message = "A data de validade é obrigatória")
        @Future(message = "A data de validade deve ser no futuro")
        LocalDate validation,

        @NotNull(message = "O preço é obrigatório")
        @PositiveOrZero(message = "O preço deve ser zero ou positivo")
        BigDecimal price,

        @NotNull(message = "A quantidade é obrigatória")
        @Min(value = 1, message = "A quantidade deve ser pelo menos 1")
        Integer quantity
) {}
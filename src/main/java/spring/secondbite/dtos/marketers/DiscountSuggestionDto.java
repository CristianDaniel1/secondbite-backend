package spring.secondbite.dtos.marketers;

import java.util.UUID;

public record DiscountSuggestionDto(
        UUID productId,
        String productName,
        Integer suggestedDiscountPercentage,
        String reason
) {}
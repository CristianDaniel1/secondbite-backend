package spring.secondbite.dtos.marketers;

import java.math.BigDecimal;
import java.util.List;

public record MarketerDashboardResponseDto(
        Long pendingOrdersCount,
        Long acceptedOrdersCount,
        BigDecimal revenueToday,
        Long activeProductsCount,
        Long expiringSoonCount,
        List<DiscountSuggestionDto> suggestedDiscounts,
        BigDecimal savedMoneyToday,
        Long savedItemsToday
) {}
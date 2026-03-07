package spring.secondbite.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import spring.secondbite.dtos.marketers.DiscountSuggestionDto;
import spring.secondbite.dtos.marketers.MarketerDashboardResponseDto;
import spring.secondbite.dtos.marketers.MarketerResponseDto;
import spring.secondbite.dtos.marketers.UpdateMarketerDto;
import spring.secondbite.entities.AppUser;
import spring.secondbite.entities.Marketer;
import spring.secondbite.entities.Product;
import spring.secondbite.entities.enums.Status;
import spring.secondbite.exceptions.UserNotFoundException;
import spring.secondbite.mappers.MarketerMapper;
import spring.secondbite.repositories.MarketerRepository;
import spring.secondbite.repositories.OrderRepository;
import spring.secondbite.repositories.ProductRepository;
import spring.secondbite.security.SecurityService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MarketerService {

    private final MarketerRepository repository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final SecurityService securityService;

    private final MarketerMapper mapper;

    public List<MarketerResponseDto> findAllMarketers() {
        List<Marketer> marketers = repository.findAll();

        return marketers
                .stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }

    public MarketerResponseDto findMarketerById(UUID id) {
        Marketer marketer = findOptionalMarketer(id);
        return mapper.toDTO(marketer);
    }

    @Transactional
    public MarketerResponseDto updateMarketer(UUID id, UpdateMarketerDto dto) {
        Marketer marketerEntity = findOptionalMarketer(id);
        Marketer marketer = mapper.partialUpdateMarketerFromDto(dto, marketerEntity);

        Marketer updatedMarketer = repository.save(marketer);
        return mapper.toDTO(updatedMarketer);
    }

    @Transactional
    public MarketerResponseDto deleteMarketer(UUID id) {
        Marketer marketer = findOptionalMarketer(id);
        repository.delete(marketer);
        return mapper.toDTO(marketer);
    }

    public MarketerDashboardResponseDto getDashboardMetrics() {
        AppUser user = securityService.getLoggedUserOrThrow();
        Marketer marketer = findMarketerByUser(user);
        UUID marketerId = marketer.getId();

        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);

        Long pending = orderRepository.countByMarketerIdAndStatus(marketerId, Status.PENDING);
        Long accepted = orderRepository.countByMarketerIdAndStatus(marketerId, Status.ACCEPTED);
        BigDecimal revenue = calculateTodayRevenue(marketerId);
        Long activeProducts = productRepository.countByMarketerIdAndQuantityGreaterThan(marketerId, 0);

        List<DiscountSuggestionDto> suggestions = generateDiscountSuggestions(marketerId);
        Long expiringSoon = (long) suggestions.size();

        BigDecimal savedMoney = orderRepository.sumSavedMoneyByDateRange(marketerId, Status.COMPLETED, startOfDay, endOfDay);
        savedMoney = savedMoney != null ? savedMoney : BigDecimal.ZERO;
        Long savedItems = orderRepository.sumSavedItemsByDateRange(marketerId, Status.COMPLETED, startOfDay, endOfDay);
        savedItems = savedItems != null ? savedItems : 0L;

        return new MarketerDashboardResponseDto(
                pending,
                accepted,
                revenue,
                activeProducts,
                expiringSoon,
                suggestions,
                savedMoney,
                savedItems
        );
    }

    private List<DiscountSuggestionDto> generateDiscountSuggestions(UUID marketerId) {
        LocalDate thresholdDate = LocalDate.now().plusDays(2);

        return productRepository.findExpiringSoonProducts(marketerId, thresholdDate)
                .stream()
                .filter(Product::isEligibleForDiscountSuggestion)
                .map(this::mapToDiscountSuggestionDto)
                .toList();
    }

    private DiscountSuggestionDto mapToDiscountSuggestionDto(Product product) {
        return new DiscountSuggestionDto(
                product.getId(),
                product.getName(),
                product.calculateSuggestedDiscount(),
                product.getDiscountSuggestionReason()
        );
    }

    private BigDecimal calculateTodayRevenue(UUID marketerId) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);

        BigDecimal revenue = orderRepository.sumRevenueByDateRange(
                marketerId, Status.COMPLETED, startOfDay, endOfDay
        );

        return Optional.ofNullable(revenue).orElse(BigDecimal.ZERO);
    }

    public Marketer findMarketerByUser(AppUser user) {
        return repository.findByUser(user)
                .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado"));
    }

    public Marketer findOptionalMarketer(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado"));
    }
}

package spring.secondbite.services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import spring.secondbite.dtos.orders.OrderResponseDto;
import spring.secondbite.entities.*;
import spring.secondbite.entities.enums.Role;
import spring.secondbite.entities.enums.Status;
import spring.secondbite.exceptions.ConflictException;
import spring.secondbite.exceptions.NotAllowedException;
import spring.secondbite.exceptions.NotFoundException;
import spring.secondbite.mappers.OrderMapper;
import spring.secondbite.repositories.OrderRepository;
import spring.secondbite.repositories.ProductRepository;
import spring.secondbite.repositories.specs.OrderSpecs;
import spring.secondbite.security.SecurityService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    private final CartService cartService;
    private final SecurityService securityService;
    private final ConsumerService consumerService;
    private final MarketerService marketerService;
    private final OrderMapper mapper;

    @Transactional
    public List<OrderResponseDto> checkout() {
        Consumer consumer = getLoggedConsumer();
        Cart cart = cartService.getCartEntity(consumer);

        validateCartNotEmpty(cart);
        List<Order> orders = processCheckout(cart, consumer);
        cartService.clearCart();

        return orders.stream().map(mapper::toDto).toList();
    }

    public List<OrderResponseDto> getOrders(Status status) {
        AppUser user = securityService.getLoggedUserOrThrow();
        Specification<Order> specs = buildOrderSpecification(user, status);

        return orderRepository.findAll(specs).stream()
                .map(mapper::toDto)
                .toList();
    }

    public OrderResponseDto getOrderById(UUID id) {
        Order order = findOrderOrThrow(id);
        checkPermission(order, securityService.getLoggedUserOrThrow());
        return mapper.toDto(order);
    }

    @Transactional
    public OrderResponseDto updateStatus(UUID id, Status newStatus) {
        Order order = findOrderOrThrow(id);
        AppUser user = securityService.getLoggedUserOrThrow();
        validateStatusChange(order, user, newStatus);

        if (newStatus == Status.CANCELED)
            restoreStock(order);

        order.setStatus(newStatus);
        return mapper.toDto(orderRepository.save(order));
    }

    private List<Order> processCheckout(Cart cart, Consumer consumer) {
        Map<Marketer, List<CartItem>> itemsByMarketer = cart.getItems().stream()
                .collect(Collectors.groupingBy(item -> item.getProduct().getMarketer()));

        List<Order> createdOrders = new ArrayList<>();

        itemsByMarketer.forEach((marketer, items) -> {
            Order order = createOrderForMarketer(consumer, marketer, items);
            createdOrders.add(order);
        });

        return createdOrders;
    }

    private Order createOrderForMarketer(Consumer consumer, Marketer marketer, List<CartItem> cartItems) {
        Order order = new Order();
        order.setConsumer(consumer);
        order.setMarketer(marketer);
        order.setStatus(Status.PENDING);

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (CartItem cartItem : cartItems) {
            OrderItem orderItem = createOrderItem(order, cartItem);
            orderItems.add(orderItem);
            totalAmount = totalAmount.add(orderItem.getSubTotal());
        }

        order.setItems(orderItems);
        order.setTotalAmount(totalAmount);

        return orderRepository.save(order);
    }

    private OrderItem createOrderItem(Order order, CartItem cartItem) {
        Product product = cartItem.getProduct();
        int quantity = cartItem.getQuantity();

        validateAndReduceStock(product, quantity);

        OrderItem orderItem = new OrderItem();
        orderItem.setOrder(order);
        orderItem.setProduct(product);
        orderItem.setQuantity(quantity);
        orderItem.setPriceAtPurchase(product.getPrice());

        return orderItem;
    }

    private void validateAndReduceStock(Product product, int quantity) {
        if (product.getQuantity() < quantity)
            throw new ConflictException("Estoque insuficiente para o produto: " + product.getName());
        product.setQuantity(product.getQuantity() - quantity);
        productRepository.save(product);
    }

    private void validateStatusChange(Order order, AppUser user, Status newStatus) {
        checkPermission(order, user);

        boolean isConsumer = user.getRoles().contains(Role.CONSUMER);
        boolean isMarketer = user.getRoles().contains(Role.MARKETER);

        if (isConsumer) {
            if (newStatus != Status.CANCELED)
                throw new NotAllowedException("Consumidores só podem cancelar pedidos.");
            if (order.getStatus() != Status.PENDING)
                throw new NotAllowedException("Só é possível cancelar pedidos pendentes.");
        } else if (isMarketer) {
            if (order.getStatus() == Status.CANCELED)
                throw new ConflictException("Não é possível alterar um pedido já cancelado.");
        }
    }

    private void restoreStock(Order order) {
        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            product.setQuantity(product.getQuantity() + item.getQuantity());
            productRepository.save(product);
        }
    }

    private void validateCartNotEmpty(Cart cart) {
        if (cart.getItems() == null || cart.getItems().isEmpty())
            throw new ConflictException("Não é possível finalizar um carrinho vazio.");
    }

    private Consumer getLoggedConsumer() {
        AppUser user = securityService.getLoggedUserOrThrow();
        return consumerService.findConsumerByUser(user);
    }

    private Specification<Order> buildOrderSpecification(AppUser user, Status status) {
        Specification<Order> specs = (root, query, cb) -> cb.conjunction();

        if (user.getRoles().contains(Role.CONSUMER)) {
            Consumer consumer = consumerService.findConsumerByUser(user);
            specs = specs.and(OrderSpecs.hasConsumer(consumer));
        } else if (user.getRoles().contains(Role.MARKETER)) {
            Marketer marketer = marketerService.findMarketerByUser(user);
            specs = specs.and(OrderSpecs.hasMarketer(marketer));
        }
        if (status != null)
            specs = specs.and(OrderSpecs.hasStatus(status));

        return specs;
    }

    private Order findOrderOrThrow(UUID id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Pedido não encontrado."));
    }

    private void checkPermission(Order order, AppUser user) {
        boolean isConsumerOwner = user.getRoles().contains(Role.CONSUMER)
                && order.getConsumer().getUser().getId().equals(user.getId());

        boolean isMarketerOwner = user.getRoles().contains(Role.MARKETER)
                && order.getMarketer().getUser().getId().equals(user.getId());

        boolean isAdmin = user.getRoles().contains(Role.ADMIN);

        if (!isConsumerOwner && !isMarketerOwner && !isAdmin)
            throw new NotAllowedException("Você não tem permissão para acessar este pedido.");
    }
}
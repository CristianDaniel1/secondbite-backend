package spring.secondbite.services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
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
import java.util.Random;
import java.util.UUID;

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
    public OrderResponseDto checkout() {
        Consumer consumer = getLoggedConsumer();
        Cart cart = cartService.getCartEntity(consumer);

        validateCartNotEmpty(cart);

        Marketer marketer = cart.getItems().getFirst().getProduct().getMarketer();
        for (CartItem item : cart.getItems()) {
            if (!item.getProduct().getMarketer().getId().equals(marketer.getId()))
                throw new ConflictException("Todos os produtos do carrinho devem pertencer ao mesmo feirante.");
        }

        Order order = new Order();
        order.setConsumer(consumer);
        order.setMarketer(marketer);
        order.setStatus(Status.PENDING);
        order.setDeliveryCode(generateDeliveryCode(consumer.getPhone()));

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (CartItem cartItem : cart.getItems()) {
            OrderItem orderItem = createOrderItem(order, cartItem);
            orderItems.add(orderItem);
            totalAmount = totalAmount.add(orderItem.getSubTotal());
        }

        order.setItems(orderItems);
        order.setTotalAmount(totalAmount);

        Order savedOrder = orderRepository.save(order);
        cartService.clearCart();

        return mapper.toDto(savedOrder);
    }

    public List<OrderResponseDto> getOrders(Status status) {
        AppUser user = securityService.getLoggedUserOrThrow();
        Specification<Order> specs = buildOrderSpecification(user, status);

        Sort sortedOrders = Sort.by(Sort.Direction.DESC, "createdAt");
        return orderRepository.findAll(specs, sortedOrders).stream()
                .map(mapper::toDto)
                .toList();
    }

    public OrderResponseDto getOrderById(UUID id) {
        Order order = findOrderOrThrow(id);
        checkPermission(order, securityService.getLoggedUserOrThrow());
        return mapper.toDto(order);
    }

    @Transactional
    public OrderResponseDto acceptOrder(UUID id) {
        Order order = findOrderOrThrow(id);
        AppUser user = securityService.getLoggedUserOrThrow();
        checkPermission(order, user);

        if (!user.getRoles().contains(Role.MARKETER))
            throw new NotAllowedException("Apenas o feirante pode aceitar o pedido.");
        if (order.getStatus() != Status.PENDING)
            throw new ConflictException("Apenas pedidos pendentes (PENDING) podem ser aceitos.");

        order.setStatus(Status.ACCEPTED);
        return mapper.toDto(orderRepository.save(order));
    }

    @Transactional
    public OrderResponseDto cancelOrder(UUID id) {
        Order order = findOrderOrThrow(id);
        AppUser user = securityService.getLoggedUserOrThrow();
        checkPermission(order, user);

        if (order.getStatus() == Status.COMPLETED || order.getStatus() == Status.CANCELED)
            throw new ConflictException("Este pedido já foi finalizado ou cancelado.");

        if (user.getRoles().contains(Role.CONSUMER) && order.getStatus() != Status.PENDING)
            throw new ConflictException("O feirante já está separando o pedido. Não é possível cancelar.");

        restoreStock(order);

        order.setStatus(Status.CANCELED);
        return mapper.toDto(orderRepository.save(order));
    }

    @Transactional
    public OrderResponseDto markAsReady(UUID id) {
        Order order = findOrderOrThrow(id);
        AppUser user = securityService.getLoggedUserOrThrow();
        checkPermission(order, user);

        if (!user.getRoles().contains(Role.MARKETER))
            throw new NotAllowedException("Apenas o feirante pode marcar o pedido como pronto.");

        if (order.getStatus() != Status.ACCEPTED)
            throw new ConflictException("O pedido precisa estar aceito (ACCEPTED) antes de ser marcado como pronto.");

        order.setStatus(Status.READY_FOR_PICKUP);
        return mapper.toDto(orderRepository.save(order));
    }

    @Transactional
    public OrderResponseDto completeOrder(UUID id, String deliveryCode) {
        Order order = findOrderOrThrow(id);
        AppUser user = securityService.getLoggedUserOrThrow();
        checkPermission(order, user);

        if (!user.getRoles().contains(Role.MARKETER))
            throw new NotAllowedException("Apenas o feirante pode dar baixa na entrega.");

        if (order.getStatus() != Status.READY_FOR_PICKUP)
            throw new ConflictException("O pedido precisa estar pronto para coleta (READY_FOR_PICKUP) para ser concluído.");

        if (!order.getDeliveryCode().equals(deliveryCode))
            throw new ConflictException("Código de entrega incorreto.");

        order.setStatus(Status.COMPLETED);
        return mapper.toDto(orderRepository.save(order));
    }

    private String generateDeliveryCode(String phone) {
        if (phone == null || phone.isEmpty()) return "0000";

        String digits = phone.replaceAll("\\D", "");
        if (digits.length() < 4)
            return String.format("%04d", new Random().nextInt(10000));
        return digits.substring(digits.length() - 4);
    }

    private OrderItem createOrderItem(Order order, CartItem cartItem) {
        Product product = cartItem.getProduct();
        int quantity = cartItem.getQuantity();

        validateAndReduceStock(product, quantity);

        OrderItem orderItem = new OrderItem();
        orderItem.setOrder(order);
        orderItem.setProduct(product);
        orderItem.setQuantity(quantity);
        orderItem.setPriceAtPurchase(product.getDiscountedPrice());
        return orderItem;
    }

    private void validateAndReduceStock(Product product, int quantity) {
        if (product.getQuantity() < quantity)
            throw new ConflictException("Estoque insuficiente para o produto: " + product.getName());
        product.setQuantity(product.getQuantity() - quantity);
        productRepository.save(product);
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

        if (!isConsumerOwner && !isMarketerOwner && !user.getRoles().contains(Role.ADMIN))
            throw new NotAllowedException("Você não tem permissão para acessar este pedido.");
    }
}
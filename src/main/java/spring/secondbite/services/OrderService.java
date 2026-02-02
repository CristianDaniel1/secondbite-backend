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
        AppUser user = securityService.getLoggedUserOrThrow();
        Consumer consumer = consumerService.findConsumerByUser(user);

        Cart cart = cartService.getCartEntity(consumer);

        if (cart.getItems().isEmpty())
            throw new ConflictException("Carrinho vazio.");

        Map<Marketer, List<CartItem>> itemsByMarketer = cart.getItems().stream()
                .collect(Collectors.groupingBy(item -> item.getProduct().getMarketer()));

        List<Order> createdOrders = new ArrayList<>();

        itemsByMarketer.forEach((marketer, items) -> {
            Order order = new Order();
            order.setConsumer(consumer);
            order.setMarketer(marketer);
            order.setStatus(Status.PENDING);

            BigDecimal total = BigDecimal.ZERO;
            List<OrderItem> orderItems = new ArrayList<>();

            for (CartItem cartItem : items) {
                Product product = cartItem.getProduct();

                if (product.getQuantity() < cartItem.getQuantity())
                    throw new ConflictException("Produto " + product.getName() + " sem estoque suficiente.");

                product.setQuantity(product.getQuantity() - cartItem.getQuantity());
                productRepository.save(product);

                OrderItem orderItem = new OrderItem();
                orderItem.setOrder(order);
                orderItem.setProduct(product);
                orderItem.setQuantity(cartItem.getQuantity());
                orderItem.setPriceAtPurchase(product.getPrice());

                total = total.add(orderItem.getSubTotal());
                orderItems.add(orderItem);
            }

            order.setItems(orderItems);
            order.setTotalAmount(total);

            createdOrders.add(orderRepository.save(order));
        });

        cartService.clearCart();

        return createdOrders.stream().map(mapper::toDto).toList();
    }

    public List<OrderResponseDto> getOrders(Status status) {
        AppUser user = securityService.getLoggedUserOrThrow();

        Specification<Order> specs = Specification.where(null);

        if (user.getRoles().contains(Role.CONSUMER)) {
            Consumer consumer = consumerService.findConsumerByUser(user);
            specs = specs.and(OrderSpecs.hasConsumer(consumer));
        } else if (user.getRoles().contains(Role.MARKETER)) {
            Marketer marketer = marketerService.findMarketerByUser(user);
            specs = specs.and(OrderSpecs.hasMarketer(marketer));
        }

        if (status != null)
            specs = specs.and(OrderSpecs.hasStatus(status));

        return orderRepository.findAll(specs).stream()
                .map(mapper::toDto)
                .toList();
    }

    public OrderResponseDto getOrderById(UUID id) {
        Order order = findOrderOrThrow(id);
        checkPermission(order);
        return mapper.toDto(order);
    }

    @Transactional
    public OrderResponseDto updateStatus(UUID id, Status newStatus) {
        Order order = findOrderOrThrow(id);
        AppUser user = securityService.getLoggedUserOrThrow();

        if (user.getRoles().contains(Role.CONSUMER)) {
            checkPermission(order);
            if (newStatus == Status.CANCELED && order.getStatus() == Status.PENDING) {
                restoreStock(order);
                order.setStatus(Status.CANCELED);
            } else
                throw new NotAllowedException("Consumidores só podem cancelar pedidos pendentes.");
        }
        else if (user.getRoles().contains(Role.MARKETER)) {
            checkPermission(order);
            if (newStatus == Status.CANCELED) restoreStock(order);

            order.setStatus(newStatus);
        } else
            throw new NotAllowedException("Acesso negado.");

        return mapper.toDto(orderRepository.save(order));
    }

    private void restoreStock(Order order) {
        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            product.setQuantity(product.getQuantity() + item.getQuantity());
            productRepository.save(product);
        }
    }

    private Order findOrderOrThrow(UUID id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Pedido não encontrado"));
    }

    private void checkPermission(Order order) {
        AppUser user = securityService.getLoggedUserOrThrow();

        boolean isConsumerOwner = user.getRoles().contains(Role.CONSUMER)
                && order.getConsumer().getUser().getId().equals(user.getId());

        boolean isMarketerOwner = user.getRoles().contains(Role.MARKETER)
                && order.getMarketer().getUser().getId().equals(user.getId());

        if (!isConsumerOwner && !isMarketerOwner && !user.getRoles().contains(Role.ADMIN))
            throw new NotAllowedException("Você não tem permissão para acessar este pedido.");
    }
}

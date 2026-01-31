package spring.secondbite.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import spring.secondbite.dtos.cart.CartItemDto;
import spring.secondbite.dtos.cart.CartResponseDto;
import spring.secondbite.entities.*;
import spring.secondbite.exceptions.ConflictException;
import spring.secondbite.exceptions.NotFoundException;
import spring.secondbite.mappers.CartMapper;
import spring.secondbite.repositories.CartItemRepository;
import spring.secondbite.repositories.CartRepository;
import spring.secondbite.security.SecurityService;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CartService {
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductService productService;
    private final ConsumerService consumerService;
    private final SecurityService securityService;

    private final CartMapper mapper;

    @Transactional(readOnly = true)
    public CartResponseDto getMyCart() {
        Cart cart = getOrCreateCartForLoggedUser();
        return mapper.toCartDto(cart);
    }

    @Transactional
    public CartResponseDto addItemToCart(CartItemDto dto) {
        Cart cart = getOrCreateCartForLoggedUser();
        Product product = productService.findProductOrThrow(dto.productId());

        if (product.getQuantity() < dto.quantity())
            throw new ConflictException("Estoque insuficiente. Disponível: " + product.getQuantity());

        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(product.getId()))
                .findFirst();

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            int newQuantity = item.getQuantity() + dto.quantity();

            if (product.getQuantity() < newQuantity)
                throw new ConflictException("Quantidade total excede o estoque. Disponível: " + product.getQuantity());

            item.setQuantity(newQuantity);
        } else {
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setProduct(product);
            newItem.setQuantity(dto.quantity());
            cart.getItems().add(newItem);
        }

        Cart savedCart = cartRepository.save(cart);
        return mapper.toCartDto(savedCart);
    }

    @Transactional
    public CartResponseDto updateItemQuantity(UUID itemId, Integer quantity) {
        Cart cart = getOrCreateCartForLoggedUser();

        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Item do carrinho não encontrado"));

        if (!item.getCart().getId().equals(cart.getId()))
            throw new NotFoundException("Item não encontrado neste carrinho.");

        if (quantity <= 0) {
            cart.getItems().remove(item);
            cartItemRepository.delete(item);
        } else {
            if (item.getProduct().getQuantity() < quantity)
                throw new ConflictException("Estoque insuficiente.");

            item.setQuantity(quantity);
            cartItemRepository.save(item);
        }

        return mapper.toCartDto(cartRepository.save(cart));
    }

    @Transactional
    public CartResponseDto removeItemFromCart(UUID itemId) {
        Cart cart = getOrCreateCartForLoggedUser();

        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Item não encontrado"));

        if (!item.getCart().getId().equals(cart.getId()))
            throw new NotFoundException("Item não pertence ao seu carrinho.");

        cart.getItems().remove(item);
        cartItemRepository.delete(item);

        return mapper.toCartDto(cartRepository.save(cart));
    }

    @Transactional
    public void clearCart() {
        Cart cart = getOrCreateCartForLoggedUser();
        cart.getItems().clear();
        cartRepository.save(cart);
    }

    private Cart getOrCreateCartForLoggedUser() {
        AppUser user = securityService.getLoggedUserOrThrow();
        Consumer consumer = consumerService.findConsumerByUser(user);

        return cartRepository.findByConsumer(consumer)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setConsumer(consumer);
                    return cartRepository.save(newCart);
                });
    }
}

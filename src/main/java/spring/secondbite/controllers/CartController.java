package spring.secondbite.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import spring.secondbite.dtos.cart.CartItemDto;
import spring.secondbite.dtos.cart.CartResponseDto;
import spring.secondbite.services.CartService;

import java.util.UUID;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    @PreAuthorize("hasRole('CONSUMER')")
    public ResponseEntity<CartResponseDto> getCart() {
        CartResponseDto cart = cartService.getMyCart();
        return ResponseEntity.ok(cart);
    }

    @PostMapping("/items")
    @PreAuthorize("hasRole('CONSUMER')")
    public ResponseEntity<CartResponseDto> addItem(@RequestBody @Valid CartItemDto dto) {
        CartResponseDto cart = cartService.addItemToCart(dto);
        return ResponseEntity.ok(cart);
    }

    @PutMapping("/items/{itemId}")
    @PreAuthorize("hasRole('CONSUMER')")
    public ResponseEntity<CartResponseDto> updateItemQuantity(
            @PathVariable UUID itemId,
            @RequestBody @Valid CartItemDto dto
    ) {
        CartResponseDto updatedCart = cartService.updateItemQuantity(itemId, dto.quantity());
        return ResponseEntity.ok(updatedCart);
    }

    @DeleteMapping("/items/{itemId}")
    @PreAuthorize("hasRole('CONSUMER')")
    public ResponseEntity<CartResponseDto> removeItem(@PathVariable UUID itemId) {
        CartResponseDto updatedCart = cartService.removeItemFromCart(itemId);
        return ResponseEntity.ok(updatedCart);
    }

    @DeleteMapping
    @PreAuthorize("hasRole('CONSUMER')")
    public ResponseEntity<Void> clearCart() {
        cartService.clearCart();
        return ResponseEntity.noContent().build();
    }
}

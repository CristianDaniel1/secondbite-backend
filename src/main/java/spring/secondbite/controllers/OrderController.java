package spring.secondbite.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import spring.secondbite.dtos.orders.OrderResponseDto;
import spring.secondbite.dtos.orders.CompleteOrderDto;
import spring.secondbite.entities.enums.Status;
import spring.secondbite.services.OrderService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService service;

    @PostMapping
    @PreAuthorize("hasRole('CONSUMER')")
    public ResponseEntity<OrderResponseDto> checkout() {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.checkout());
    }

    @GetMapping
    public ResponseEntity<List<OrderResponseDto>> getOrders(
            @RequestParam(value = "status", required = false) Status status
    ) {
        return ResponseEntity.ok(service.getOrders(status));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponseDto> getOrderById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.getOrderById(id));
    }

    @PatchMapping("/{id}/accept")
    @PreAuthorize("hasRole('MARKETER')")
    public ResponseEntity<OrderResponseDto> acceptOrder(@PathVariable UUID id) {
        return ResponseEntity.ok(service.acceptOrder(id));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<OrderResponseDto> cancelOrder(@PathVariable UUID id) {
        return ResponseEntity.ok(service.cancelOrder(id));
    }

    @PatchMapping("/{id}/ready")
    @PreAuthorize("hasRole('MARKETER')")
    public ResponseEntity<OrderResponseDto> markAsReady(@PathVariable UUID id) {
        return ResponseEntity.ok(service.markAsReady(id));
    }

    @PatchMapping("/{id}/complete")
    @PreAuthorize("hasRole('MARKETER')")
    public ResponseEntity<OrderResponseDto> completeOrder(
            @PathVariable UUID id,
            @RequestBody @Valid CompleteOrderDto dto
    ) {
        return ResponseEntity.ok(service.completeOrder(id, dto.deliveryCode()));
    }
}
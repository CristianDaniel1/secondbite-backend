package spring.secondbite.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import spring.secondbite.dtos.orders.OrderResponseDto;
import spring.secondbite.dtos.orders.UpdateOrderStatusDto;
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
    public ResponseEntity<List<OrderResponseDto>> checkout() {
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

    @PatchMapping("/{id}/status")
    public ResponseEntity<OrderResponseDto> updateStatus(
            @PathVariable UUID id,
            @RequestBody @Valid UpdateOrderStatusDto dto
    ) {
        return ResponseEntity.ok(service.updateStatus(id, dto.status()));
    }
}

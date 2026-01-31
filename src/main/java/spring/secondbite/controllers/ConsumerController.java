package spring.secondbite.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import spring.secondbite.dtos.consumers.ConsumerResponseDto;
import spring.secondbite.dtos.consumers.UpdateConsumerDto;
import spring.secondbite.services.ConsumerService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("consumers")
@RequiredArgsConstructor
public class ConsumerController {
    private final ConsumerService service;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ConsumerResponseDto>> getConsumers() {
        List<ConsumerResponseDto> consumers = service.findAllConsumers();
        return ResponseEntity.ok(consumers);
    }

    @GetMapping("{id}")
    public ResponseEntity<ConsumerResponseDto> getConsumerById(@PathVariable("id") UUID id) {
        ConsumerResponseDto consumer = service.findConsumerById(id);
        return ResponseEntity.ok(consumer);
    }

    @PutMapping("{id}")
    @PreAuthorize("hasAnyRole('CONSUMER', 'ADMIN')")
    public ResponseEntity<ConsumerResponseDto> updateConsumer(
            @PathVariable("id") UUID id, @RequestBody @Valid UpdateConsumerDto dto) {
        ConsumerResponseDto updatedConsumer = service.updateConsumer(id, dto);
        return ResponseEntity.ok(updatedConsumer);
    }

    @DeleteMapping("{id}")
    @PreAuthorize("hasAnyRole('CONSUMER', 'ADMIN')")
    public ResponseEntity<ConsumerResponseDto> deleteConsumer(@PathVariable("id") UUID id) {
        ConsumerResponseDto deletedConsumer = service.deleteConsumer(id);
        return ResponseEntity.ok(deletedConsumer);
    }
}

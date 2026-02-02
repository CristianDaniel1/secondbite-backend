package spring.secondbite.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import spring.secondbite.dtos.marketers.MarketerResponseDto;
import spring.secondbite.dtos.marketers.UpdateMarketerDto;
import spring.secondbite.services.MarketerService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("marketers")
@RequiredArgsConstructor
public class MarketerController {
    private final MarketerService service;

    @GetMapping
    public ResponseEntity<List<MarketerResponseDto>> getMarketers() {
        List<MarketerResponseDto> marketers = service.findAllMarketers();
        return ResponseEntity.ok(marketers);
    }

    @GetMapping("{id}")
    public ResponseEntity<MarketerResponseDto> getMarketerById(@PathVariable("id") UUID id) {
        MarketerResponseDto marketer = service.findMarketerById(id);
        return ResponseEntity.ok(marketer);
    }

    @PutMapping("{id}")
    @PreAuthorize("hasAnyRole('MARKETER', 'ADMIN')")
    public ResponseEntity<MarketerResponseDto> updateMarketer(
            @PathVariable("id") UUID id, @RequestBody @Valid UpdateMarketerDto dto) {
        MarketerResponseDto updatedMarketer = service.updateMarketer(id, dto);
        return ResponseEntity.ok(updatedMarketer);
    }

    @DeleteMapping("{id}")
    @PreAuthorize("hasAnyRole('MARKETER', 'ADMIN')")
    public ResponseEntity<MarketerResponseDto> deleteMarketer(@PathVariable("id") UUID id) {
        MarketerResponseDto deletedMarketer = service.deleteMarketer(id);
        return ResponseEntity.ok(deletedMarketer);
    }

    @GetMapping("/map-locations")
    public ResponseEntity<List<MarketerResponseDto>> getAllMarketersForMap() {
        // Pode ser otimizado ainda
        return ResponseEntity.ok(service.findAllMarketers());
    }
}

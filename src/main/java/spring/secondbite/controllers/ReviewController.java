package spring.secondbite.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import spring.secondbite.dtos.PageResponseDto;
import spring.secondbite.dtos.reviews.ReviewDto;
import spring.secondbite.dtos.reviews.ReviewResponseDto;
import spring.secondbite.services.ReviewService;

import java.util.UUID;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService service;

    @PostMapping
    @PreAuthorize("hasRole('CONSUMER')")
    public ResponseEntity<ReviewResponseDto> createReview(@RequestBody @Valid ReviewDto dto) {
        ReviewResponseDto review = service.createReview(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(review);
    }

    @GetMapping("/marketer/{marketerId}")
    public ResponseEntity<PageResponseDto<ReviewResponseDto>> getMarketerReviews(
            @PathVariable UUID marketerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "desc") String sortDirection
    ) {
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDirection) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, limit, Sort.by(direction, "createdAt"));
        PageResponseDto<ReviewResponseDto> reviews = service.getReviewsByMarketer(marketerId, pageable);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/marketer/{marketerId}/average")
    public ResponseEntity<Double> getAverageRating(@PathVariable UUID marketerId) {
        Double averageRating = service.getAverageRating(marketerId);
        return ResponseEntity.ok(averageRating);
    }
}
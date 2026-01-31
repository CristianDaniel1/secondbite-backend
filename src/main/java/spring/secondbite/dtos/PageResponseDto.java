package spring.secondbite.dtos;

import java.util.List;

public record PageResponseDto<T>(
        int page,
        int limit,
        long totalElements,
        int totalPages,
        boolean isLast,
        List<T> content
) {
}

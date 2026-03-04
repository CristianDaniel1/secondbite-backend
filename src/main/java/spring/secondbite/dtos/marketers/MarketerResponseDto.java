package spring.secondbite.dtos.marketers;

import jakarta.validation.constraints.Size;
import spring.secondbite.entities.enums.Role;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

public record MarketerResponseDto(
        UUID id,
        String name,
        String email,
        String address,
        String cnpj,
        Double latitude,
        Double longitude,
        String stallName,
        String operatingSchedule,
        String phone,
        Set<Role> roles,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt
) {
}

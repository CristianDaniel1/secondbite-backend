package spring.secondbite.dtos.marketers;

import spring.secondbite.entities.enums.Role;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

public record MarketerResponseDto(
        UUID id,
        String name,
        String email,
        String zipcode,
        String cnpj,
        String phone,
        Set<Role> roles,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt
) {
}

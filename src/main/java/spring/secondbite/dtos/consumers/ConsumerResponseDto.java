package spring.secondbite.dtos.consumers;

import spring.secondbite.entities.enums.Role;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

public record ConsumerResponseDto(
        UUID id,
        String name,
        String email,
        String address,
        String cpf,
        String phone,
        Set<Role> roles,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt
) {
}

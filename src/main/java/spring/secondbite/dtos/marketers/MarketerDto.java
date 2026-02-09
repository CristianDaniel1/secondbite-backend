package spring.secondbite.dtos.marketers;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record MarketerDto(
        UUID id,

        @NotBlank(message = "Name is required")
        @Size(max = 100, message = "Name must be at most 100 characters")
        String name,

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        @Size(max = 100, message = "Email must be at most 100 characters")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 6, message = "Password must be at least 6 characters")
        String password,

        @NotBlank(message = "CNPJ is required")
        String cnpj,

        @Size(max = 255, message = "Address must be at most 255 characters")
        String address,

        @Size(max = 15, message = "Phone must be at most 15 characters")
        String phone,
        Double latitude,
        Double longitude,
        String stallName) {
}

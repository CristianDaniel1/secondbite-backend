package spring.secondbite.dtos.consumers;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record ConsumerDto(
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

        @NotBlank(message = "CPF is required")
        String cpf,

        @NotBlank(message = "zipcode is required")
        @Size(max = 20, message = "Zipcode must be at most 20 characters")
        String zipcode,

        @Size(max = 15, message = "Phone must be at most 15 characters")
        String phone) {
}

package spring.secondbite.dtos.consumers;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UpdateConsumerDto(
        @Size(max = 100, message = "Name must be at most 100 characters")
        String name,

        @Email(message = "Invalid email format")
        @Size(max = 100, message = "Email must be at most 100 characters")
        String email,

        @Size(min = 6, message = "Password must be at least 6 characters")
        String password,

        String cpf,

        @Size(max = 255, message = "Address must be at most 255 characters")
        String address,

        @Size(max = 15, message = "Phone must be at most 15 characters")
        String phone
) {
}

package spring.secondbite.dtos.marketers;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateMarketerDto(
        @Size(max = 100, message = "Name must be at most 100 characters")
        String name,

        @Email(message = "Invalid email format")
        @Size(max = 100, message = "Email must be at most 100 characters")
        String email,

        @Size(min = 6, message = "Password must be at least 6 characters")
        String password,

        @Size(min = 14, max = 14, message = "CNPJ must be exactly 14 digits")
        String cnpj,

        @Size(max = 20, message = "Zipcode must be at most 20 characters")
        String zipcode,

        @Size(max = 15, message = "Phone must be at most 15 characters")
        String phone) {
}

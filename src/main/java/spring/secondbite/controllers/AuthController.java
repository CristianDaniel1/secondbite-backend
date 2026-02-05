package spring.secondbite.controllers;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import spring.secondbite.dtos.auth.AuthResponseDto;
import spring.secondbite.dtos.auth.LoginUserDto;
import spring.secondbite.dtos.consumers.ConsumerDto;
import spring.secondbite.dtos.marketers.MarketerDto;
import spring.secondbite.services.AuthService;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(
            @RequestBody LoginUserDto dto,
            HttpServletResponse response) {
        AuthResponseDto authUser = authService.login(dto, response);
        return ResponseEntity.ok(authUser);
    }

    @PostMapping("/register/consumer")
    public ResponseEntity<AuthResponseDto> registerConsumer(
            @Valid @RequestBody ConsumerDto dto, HttpServletResponse response) {
        AuthResponseDto consumer = authService.createConsumer(dto, response);
        return ResponseEntity.status(HttpStatus.CREATED).body(consumer);
    }

    @PostMapping("/register/marketer")
    public ResponseEntity<AuthResponseDto> registerMarketer(
            @Valid @RequestBody MarketerDto dto,
            HttpServletResponse response) {
        AuthResponseDto marketer = authService.createMarketer(dto, response);
        return ResponseEntity.status(HttpStatus.CREATED).body(marketer);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletResponse response) {
        authService.logout(response);
        return ResponseEntity.ok("Successfully logged out.");
    }

    @GetMapping("/check")
    public ResponseEntity<Object> check() {
        Object user = authService.checkUser();
        return ResponseEntity.ok(user);
    }
}
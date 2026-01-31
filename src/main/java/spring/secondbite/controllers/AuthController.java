package spring.secondbite.controllers;

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
    public ResponseEntity<AuthResponseDto> login(@RequestBody LoginUserDto dto) {
        AuthResponseDto authUser = authService.login(dto);
        return ResponseEntity.ok(authUser);
    }

    @PostMapping("/register/consumer")
    public ResponseEntity<AuthResponseDto> registerConsumer(@Valid @RequestBody ConsumerDto dto) {
        AuthResponseDto consumer = authService.createConsumer(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(consumer);
    }

    @PostMapping("/register/marketer")
    public ResponseEntity<AuthResponseDto> registerMarketer(
            @Valid @RequestBody MarketerDto dto) {
        AuthResponseDto marketer = authService.createMarketer(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(marketer);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout() {
        return ResponseEntity.ok("Successfully logged out (token should be discarded by client).");
    }

    @GetMapping("/check")
    public ResponseEntity<Object> check() {
        Object user = authService.checkUser();
        return ResponseEntity.ok(user);
    }
}
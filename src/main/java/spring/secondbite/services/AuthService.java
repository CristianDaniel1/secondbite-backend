package spring.secondbite.services;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import spring.secondbite.dtos.auth.AuthResponseDto;
import spring.secondbite.dtos.auth.LoginUserDto;
import spring.secondbite.dtos.consumers.ConsumerDto;
import spring.secondbite.dtos.marketers.MarketerDto;
import spring.secondbite.entities.AppUser;
import spring.secondbite.entities.Consumer;
import spring.secondbite.entities.Marketer;
import spring.secondbite.entities.enums.Role;
import spring.secondbite.exceptions.ConflictException;
import spring.secondbite.exceptions.UserNotFoundException;
import spring.secondbite.mappers.ConsumerMapper;
import spring.secondbite.mappers.MarketerMapper;
import spring.secondbite.repositories.AppUserRepository;
import spring.secondbite.repositories.ConsumerRepository;
import spring.secondbite.repositories.MarketerRepository;
import spring.secondbite.security.JwtService;
import spring.secondbite.security.SecurityService;

import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AppUserRepository userRepository;
    private final ConsumerRepository consumerRepository;
    private final MarketerRepository marketerRepository;

    private final ConsumerMapper consumerMapper;
    private final MarketerMapper marketerMapper;

    private final JwtService jwtService;
    private final SecurityService securityService;

    public AuthResponseDto createConsumer(ConsumerDto consumerDto, HttpServletResponse response) {
        checkUserExists(consumerDto.email());
        Consumer consumerEntity = consumerMapper.toEntity(consumerDto);
        consumerEntity.getUser().setRoles(Set.of(Role.CONSUMER));

        String rawPassword = consumerEntity.getUser().getPassword();
        consumerEntity.getUser().setPassword(jwtService.encodePassword(rawPassword));

        Consumer consumer = consumerRepository.save(consumerEntity);
        String token = jwtService.generateToken(consumer.getUser());
        jwtService.setJwtCookie(response, token);

        return new AuthResponseDto(consumerMapper.toDTO(consumer), token);
    }

    public AuthResponseDto createMarketer(MarketerDto marketerDto, HttpServletResponse response) {
        checkUserExists(marketerDto.email());
        Marketer marketerEntity = marketerMapper.toEntity(marketerDto);
        marketerEntity.getUser().setRoles(Set.of(Role.MARKETER));

        String rawPassword = marketerEntity.getUser().getPassword();
        marketerEntity.getUser().setPassword(jwtService.encodePassword(rawPassword));

        Marketer marketer = marketerRepository.save(marketerEntity);
        String token = jwtService.generateToken(marketer.getUser());
        jwtService.setJwtCookie(response, token);

        return new AuthResponseDto(marketerMapper.toDTO(marketer), token);
    }

    public AuthResponseDto login(LoginUserDto userDto, HttpServletResponse response) {
        AppUser user = userRepository.findByEmail(userDto.email())
                .orElseThrow(() -> new UsernameNotFoundException("Invalid credentials"));

        jwtService.isMatching(userDto.password(), user.getPassword());
        String token = jwtService.generateToken(user);

        Object userDetails;
        if (user.getRoles().contains(Role.CONSUMER)) {
            Consumer consumer = findConsumerByUser(user);
            userDetails = consumerMapper.toDTO(consumer);
        } else if (user.getRoles().contains(Role.MARKETER)) {
            Marketer marketer = findMarketerByUser(user);
            userDetails = marketerMapper.toDTO(marketer);
        } else
            throw new BadCredentialsException("Unknown role");

        jwtService.setJwtCookie(response, token);
        return new AuthResponseDto(userDetails, token);
    }

    public void logout(HttpServletResponse response) {
        jwtService.expireJwtCookie(response);
    }

    public Object checkUser() {
        AppUser user = securityService.getLoggedUserOrThrow();
        if (user.getRoles().contains(Role.CONSUMER))
            return consumerMapper.toDTO(findConsumerByUser(user));

        if (user.getRoles().contains(Role.MARKETER))
            return marketerMapper.toDTO(findMarketerByUser(user));

        return null;
    }

    public Optional<AppUser> findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Consumer findConsumerByUser(AppUser user) {
        return consumerRepository.findByUser(user)
                .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado"));
    }

    public Marketer findMarketerByUser(AppUser user) {
        return marketerRepository.findByUser(user)
                .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado"));
    }

    public void checkUserExists(String email) {
        if (userRepository.existsByEmail(email)) throwConflict();
    }

    private void throwConflict() {
        throw new ConflictException("Já existe um usuário com esses dados");
    }

}

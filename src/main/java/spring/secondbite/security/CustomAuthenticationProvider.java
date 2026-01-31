package spring.secondbite.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import spring.secondbite.entities.AppUser;
import spring.secondbite.services.AuthService;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationProvider implements AuthenticationProvider {
    private final AuthService userService;
    private final PasswordEncoder encoder;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String loginEmail = authentication.getName();
        String passwordProvided = authentication.getCredentials().toString();

        Optional<AppUser> userOptional = userService.findUserByEmail(loginEmail);

        if (userOptional.isEmpty())
            throw userNotFoundException();

        AppUser user = userOptional.get();
        String password = user.getPassword();

        boolean passwordIsMatching = encoder.matches(passwordProvided, password);

        if (passwordIsMatching)
            return new CustomAuthentication(user);

        throw userNotFoundException();
    }

    private UsernameNotFoundException userNotFoundException() {
        return new UsernameNotFoundException("Invalid credentials");
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.isAssignableFrom(UsernamePasswordAuthenticationToken.class);
    }
}

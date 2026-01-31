package spring.secondbite.security;

import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import spring.secondbite.entities.AppUser;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JwtService {

    private final SecretKey secretKey;
    private final PasswordEncoder encoder;

    /**
     * Codifica uma senha em texto simples para uma representação segura (hash).
     *
     * @param password A senha em texto simples.
     * @return A senha codificada (hash) para ser armazenada com segurança.
     */
    public String encodePassword(String password) {
        return encoder.encode(password);
    }

    /**
     * Verifica se a senha digitada pelo usuário corresponde à senha codificada armazenada.
     *
     * @param digitedPassword A senha em texto simples digitada pelo usuário.
     * @param encodedPassword A senha codificada armazenada no banco.
     * @throws BadCredentialsException se as senhas não coincidirem.
     */
    public void isMatching(String digitedPassword, String encodedPassword) {
        if (!encoder.matches(digitedPassword, encodedPassword))
            throw new BadCredentialsException("Invalid credentials");
    }

    /**
     * Gera um token JWT assinado com base nas informações do usuário.
     *
     * @param user Instância de AppUser contendo os dados do usuário autenticado.
     * @return String contendo o token JWT gerado.
     */
    public String generateToken(AppUser user) {
        Map<String, Object> claims = new HashMap<>();

        return Jwts.builder()
                .claims()
                .add(claims)
                .subject(user.getEmail())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() * 60 * 60 * 60))
                .and()
                .signWith(secretKey)
                .compact();
    }

    /**
     * Extrai o e-mail (subject) de um token JWT.
     *
     * @param token O token JWT em formato String.
     * @return O e-mail do usuário (subject) contido no token.
     */
    public String extractUsername(String token) {
        return Jwts.parser()
                .verifyWith(secretKey).build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }
}
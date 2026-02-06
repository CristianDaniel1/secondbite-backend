package spring.secondbite.security;

import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
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

    private static final long EXPIRATION_TIME_MS = 1000 * 60 * 60 * 5;
    private static final int COOKIE_EXPIRATION_SEC = 5 * 60 * 60;

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
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME_MS))
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

    /**
     * Adiciona um cookie HTTP com o token JWT na resposta.
     *
     * @param response resposta HTTP
     * @param token    token JWT
     */
    public void setJwtCookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie("access_token", token);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(COOKIE_EXPIRATION_SEC);
        cookie.setSecure(false);
        response.addCookie(cookie);
    }

    /**
     * Expira (remove) o cookie JWT da resposta.
     *
     * @param response resposta HTTP
     */
    public void expireJwtCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie("access_token", "");
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        cookie.setSecure(false);
        response.addCookie(cookie);
    }
}
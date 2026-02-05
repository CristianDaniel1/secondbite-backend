package spring.secondbite.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import spring.secondbite.entities.AppUser;
import spring.secondbite.exceptions.UnauthorizedException;
import spring.secondbite.services.AuthService;

import java.io.IOException;
import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class JwtCustomAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final AuthService userService;
//    private final JwtAuthenticationEntryPoint entryPoint;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String token = extractTokenFromCookie(request);

        if (token != null) {
            try {
                String email = jwtService.extractUsername(token);
                AppUser user = userService.findUserByEmail(email)
                        .orElseThrow(() -> new UnauthorizedException("User not found"));

                Authentication auth = new CustomAuthentication(user);
                SecurityContextHolder.getContext().setAuthentication(auth);

            } catch (JwtException | UnauthorizedException e) {
//                if (!isPublicEndpoint(request)) {
//                    entryPoint.commence(request, response, new BadCredentialsException("Invalid or expired token."));
//                    return;
//                }
            }
        }

        filterChain.doFilter(request, response);
    }

    private String extractTokenFromHeader(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    private String extractTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() == null) return null;

        return Arrays.stream(request.getCookies())
                .filter(c -> c.getName().equals("access_token"))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }

//    private boolean isPublicEndpoint(HttpServletRequest request) {
//        String path = request.getRequestURI();
//        String method = request.getMethod();
//
//        return path.startsWith("/auth")
//                || (path.startsWith("/users") && "POST".equalsIgnoreCase(method));
//    }
}
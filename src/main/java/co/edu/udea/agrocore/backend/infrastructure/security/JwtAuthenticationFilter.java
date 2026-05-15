package co.edu.udea.agrocore.backend.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Extrae el Bearer token del header Authorization, lo valida con JwtService
 * y popula el SecurityContext. Si el token es ausente o inválido, el request
 * sigue sin Authentication (el resultado depende de la política de la filterChain).
 *
 * No es un @Component — lo instancia SecurityConfig para que @WebMvcTest
 * no intente wiring automatico del filtro fuera del contexto de security.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            jwtService.parse(token).ifPresent(decoded -> {
                var auth = new UsernamePasswordAuthenticationToken(
                        decoded,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + decoded.role().name()))
                );
                SecurityContextHolder.getContext().setAuthentication(auth);
            });
        }
        chain.doFilter(request, response);
    }
}

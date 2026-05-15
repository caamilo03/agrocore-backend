package co.edu.udea.agrocore.backend.infrastructure.security;

import co.edu.udea.agrocore.backend.domain.model.Role;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class JwtAuthenticationFilterTest {

    private JwtService jwtService;
    private JwtAuthenticationFilter filter;
    private FilterChain chain;

    @BeforeEach
    void setUp() {
        jwtService = mock(JwtService.class);
        filter = new JwtAuthenticationFilter(jwtService);
        chain = mock(FilterChain.class);
        SecurityContextHolder.clearContext();
    }

    @Test
    void validToken_populatesSecurityContext() throws Exception {
        DecodedJwt decoded = new DecodedJwt(UUID.randomUUID(), "op@x.com", Role.OPERADOR);
        when(jwtService.parse("valid-token")).thenReturn(Optional.of(decoded));

        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("Authorization", "Bearer valid-token");

        filter.doFilter(req, new MockHttpServletResponse(), chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal())
                .isEqualTo(decoded);
        verify(chain).doFilter(any(), any());
    }

    @Test
    void missingHeader_doesNotPopulateSecurityContext() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();

        filter.doFilter(req, new MockHttpServletResponse(), chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verifyNoInteractions(jwtService);
        verify(chain).doFilter(any(), any());
    }

    @Test
    void invalidToken_doesNotPopulateSecurityContextButContinues() throws Exception {
        when(jwtService.parse("bad-token")).thenReturn(Optional.empty());
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("Authorization", "Bearer bad-token");

        filter.doFilter(req, new MockHttpServletResponse(), chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(chain).doFilter(any(), any()); // siempre continua
    }

    @Test
    void headerWithoutBearerPrefix_isIgnored() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("Authorization", "Basic dXNlcjpwYXNz");

        filter.doFilter(req, new MockHttpServletResponse(), chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verifyNoInteractions(jwtService);
    }
}

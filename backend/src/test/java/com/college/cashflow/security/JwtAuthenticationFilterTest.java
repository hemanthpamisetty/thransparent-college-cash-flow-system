package com.college.cashflow.security;

import com.college.cashflow.entity.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for JwtAuthenticationFilter.
 *
 * Uses hand-written stubs instead of Mockito to avoid the byte-buddy
 * dynamic agent hang on JDK 21 when mocking concrete Spring classes.
 *
 * We call doFilterInternal() via reflection to bypass the
 * OncePerRequestFilter servlet-context attribute check, which would
 * require a full WebApplicationContext.
 */
class JwtAuthenticationFilterTest {

    @BeforeEach
    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    // ── Stub helpers ────────────────────────────────────────────────────────────

    /** Build a JwtTokenProvider that accepts/rejects specific token strings. */
    private JwtTokenProvider stubJwt(Map<String, String> validTokenToUsername) {
        // Use a 32-char secret so the real provider initialises without error.
        String secret = "test-secret-key-that-is-32chars!";
        long expiry = 3_600_000L;

        return new JwtTokenProvider(secret, expiry) {
            @Override
            public boolean validateToken(String token) {
                return validTokenToUsername.containsKey(token);
            }
            @Override
            public String getUsernameFromToken(String token) {
                return validTokenToUsername.get(token);
            }
        };
    }

    /** Build a CustomUserDetailsService that returns the supplied UserDetails for the given username. */
    private CustomUserDetailsService stubUds(String username, UserDetails details) {
        return new CustomUserDetailsService(null) {
            @Override
            public UserDetails loadUserByUsername(String u) {
                if (username.equals(u)) return details;
                throw new UsernameNotFoundException("User not found: " + u);
            }
        };
    }

    /** Build a CustomUserDetailsService that always throws UsernameNotFoundException. */
    private CustomUserDetailsService stubUdsThrows() {
        return new CustomUserDetailsService(null) {
            @Override
            public UserDetails loadUserByUsername(String u) {
                throw new UsernameNotFoundException("User deleted: " + u);
            }
        };
    }

    /** Build an active/disabled Spring Security User. */
    private UserDetails user(String username, boolean enabled) {
        return new org.springframework.security.core.userdetails.User(
                username, "hashedPass",
                enabled, true, true, true,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_MAIN_ADMIN"))
        );
    }

    /** Invoke the protected doFilterInternal via reflection. */
    private void runFilter(JwtAuthenticationFilter filter,
                           MockHttpServletRequest req,
                           MockHttpServletResponse res,
                           MockFilterChain chain) throws Exception {
        Method m = JwtAuthenticationFilter.class.getDeclaredMethod(
                "doFilterInternal",
                jakarta.servlet.http.HttpServletRequest.class,
                jakarta.servlet.http.HttpServletResponse.class,
                jakarta.servlet.FilterChain.class
        );
        m.setAccessible(true);
        m.invoke(filter, req, res, chain);
    }

    // ── Tests ────────────────────────────────────────────────────────────────────

    @Test
    void activeUser_withValidToken_isAuthenticated() throws Exception {
        UserDetails activeUser = user("admin", true);
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(
                stubJwt(Map.of("valid.jwt.token", "admin")),
                stubUds("admin", activeUser)
        );

        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("Authorization", "Bearer valid.jwt.token");

        runFilter(filter, req, new MockHttpServletResponse(), new MockFilterChain());

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("admin", SecurityContextHolder.getContext().getAuthentication().getName());
    }

    @Test
    void deactivatedUser_withValidToken_isRejected() throws Exception {
        UserDetails disabledUser = user("cseadmin", false);   // enabled = false
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(
                stubJwt(Map.of("valid.jwt.token", "cseadmin")),
                stubUds("cseadmin", disabledUser)
        );

        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("Authorization", "Bearer valid.jwt.token");

        runFilter(filter, req, new MockHttpServletResponse(), new MockFilterChain());

        // Security context must remain unauthenticated for deactivated users
        assertNull(SecurityContextHolder.getContext().getAuthentication(),
                "Deactivated user must not be authenticated");
    }

    @Test
    void invalidToken_isIgnored_noUserLookup() throws Exception {
        AtomicBoolean udsWasCalled = new AtomicBoolean(false);

        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(
                stubJwt(Map.of()),   // no valid tokens
                new CustomUserDetailsService(null) {
                    @Override
                    public UserDetails loadUserByUsername(String u) {
                        udsWasCalled.set(true);
                        throw new UsernameNotFoundException("should not be called");
                    }
                }
        );

        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("Authorization", "Bearer expired.jwt.token");

        runFilter(filter, req, new MockHttpServletResponse(), new MockFilterChain());

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        assertFalse(udsWasCalled.get(), "UserDetailsService must not be called for invalid tokens");
    }

    @Test
    void deletedUser_handledGracefully_noException() {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(
                stubJwt(Map.of("valid.token.deleted.user", "deleted_user")),
                stubUdsThrows()
        );

        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("Authorization", "Bearer valid.token.deleted.user");

        assertDoesNotThrow(() ->
                runFilter(filter, req, new MockHttpServletResponse(), new MockFilterChain()));
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void noAuthHeader_filterPassesThrough_noAuthentication() throws Exception {
        AtomicBoolean udsWasCalled = new AtomicBoolean(false);

        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(
                stubJwt(Map.of()),
                new CustomUserDetailsService(null) {
                    @Override
                    public UserDetails loadUserByUsername(String u) {
                        udsWasCalled.set(true);
                        throw new UsernameNotFoundException("should not be called");
                    }
                }
        );

        MockHttpServletRequest req = new MockHttpServletRequest(); // no Authorization header

        runFilter(filter, req, new MockHttpServletResponse(), new MockFilterChain());

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        assertFalse(udsWasCalled.get(), "UserDetailsService must not be called when no token present");
    }
}

package com.climbingapp.boot.config.security;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

class AuthenticationProviderTest {

    private final WebSecurityConfig config = new WebSecurityConfig();

    private final DaoAuthenticationProvider provider =
            config.authenticationProvider(
                    new InMemoryUserDetailsManager(
                            User.withUsername("pending@x.com")
                                    .password(config.passwordEncoder().encode("right-password"))
                                    .roles("USER")
                                    .disabled(true)
                                    .build()));

    @Test
    void wrongPasswordOnDisabledAccountLooksLikeUnknownAccount() {
        assertThrows(BadCredentialsException.class, () -> login("pending@x.com", "wrong"));
        assertThrows(BadCredentialsException.class, () -> login("nobody@x.com", "wrong"));
    }

    @Test
    void rightPasswordOnDisabledAccountStillReportsDisabled() {
        assertThrows(DisabledException.class, () -> login("pending@x.com", "right-password"));
    }

    private void login(String email, String password) {
        provider.authenticate(new UsernamePasswordAuthenticationToken(email, password));
    }
}

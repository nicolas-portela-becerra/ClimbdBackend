package com.climbingapp.jwt.utils;

import com.climbingapp.domain.dto.UserCredentials;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

import java.io.Serial;
import java.util.List;

/**
 * Logged-in user. Carries the database id so authorization checks and controllers don't have to
 * look the user up by email again, and never trust a user id sent by the client.
 */
@Getter
@EqualsAndHashCode(callSuper = true)
public class AuthenticatedUser extends User {

    @Serial
    private static final long serialVersionUID = -2274464063191563649L;

    public static final String ROLE_ADMIN = "ROLE_ADMIN";

    private final Integer id;

    public AuthenticatedUser(UserCredentials credentials) {
        super(
                credentials.email(),
                credentials.passwordHash() != null ? credentials.passwordHash() : "",
                credentials.active(),
                true,
                true,
                true,
                List.of(new SimpleGrantedAuthority("ROLE_" + credentials.role())));
        this.id = credentials.id();
    }

    public boolean isAdmin() {
        return getAuthorities().stream().anyMatch(a -> ROLE_ADMIN.equals(a.getAuthority()));
    }

    /** The user of the current request. Only call it from authenticated endpoints. */
    public static AuthenticatedUser current() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null
                && authentication.getPrincipal() instanceof AuthenticatedUser user) {
            return user;
        }
        throw new IllegalStateException("No authenticated user in the security context");
    }
}

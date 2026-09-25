package com.climbingapp.jwt.utils;

import com.climbingapp.domain.dto.UserCredentials;
import com.climbingapp.domain.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JwtUserDetailsService implements UserDetailsService {

    @Autowired private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserCredentials credentials = userRepository.findCredentialsByEmail(email);

        if (credentials != null) {
            return new User(
                    credentials.email(),
                    credentials.passwordHash(),
                    credentials.active(),
                    true,
                    true,
                    true,
                    List.of(new SimpleGrantedAuthority("ROLE_" + credentials.role())));
        }
        throw new UsernameNotFoundException("User not found");
    }
}

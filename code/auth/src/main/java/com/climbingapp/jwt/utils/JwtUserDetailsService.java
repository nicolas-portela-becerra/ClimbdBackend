package com.climbingapp.jwt.utils;

import com.climbingapp.domain.dto.UserCredentials;
import com.climbingapp.domain.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class JwtUserDetailsService implements UserDetailsService {

    @Autowired private UserRepository userRepository;

    @Override
    public AuthenticatedUser loadUserByUsername(String email) throws UsernameNotFoundException {
        UserCredentials credentials = userRepository.findCredentialsByEmail(email);

        if (credentials != null) {
            return new AuthenticatedUser(credentials);
        }
        throw new UsernameNotFoundException("User not found");
    }
}

package com.sportflow.gestor_reservas.auth.security;

import com.sportflow.gestor_reservas.models.Role;
import com.sportflow.gestor_reservas.models.User;
import com.sportflow.gestor_reservas.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserDetailsServiceImplTest {

    private UserRepository userRepository;
    private UserDetailsServiceImpl userDetailsService;

    @BeforeEach
    void setUp() {
        userRepository = Mockito.mock(UserRepository.class);
        userDetailsService = new UserDetailsServiceImpl(userRepository);
    }

    @Test
    void shouldLoadUserByUsername() {
        User user = new User(
                "test@example.com",
                "encodedPassword",
                "test@example.com",
                Role.USER,
                true
        );
        user.setRole(Role.USER);
        user.setPassword("encodedPassword");
        
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        UserDetails userDetails = userDetailsService.loadUserByUsername("test@example.com");

        assertNotNull(userDetails);
        assertEquals("test@example.com", userDetails.getUsername());
        assertEquals("encodedPassword", userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_USER")));
        
        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> {
            userDetailsService.loadUserByUsername("nonexistent@example.com");
        });

        verify(userRepository).findByEmail("nonexistent@example.com");
    }
}

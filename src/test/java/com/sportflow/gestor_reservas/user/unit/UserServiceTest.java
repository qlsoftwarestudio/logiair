package com.sportflow.gestor_reservas.user.unit;

import com.sportflow.gestor_reservas.models.Role;
import com.sportflow.gestor_reservas.models.User;
import com.sportflow.gestor_reservas.tenant.TenantContext;
import com.sportflow.gestor_reservas.user.dto.PaginatedResponse;
import com.sportflow.gestor_reservas.user.dto.UserResponseDTO;
import com.sportflow.gestor_reservas.user.mapper.UserMapper;
import com.sportflow.gestor_reservas.user.repository.UserRepository;
import com.sportflow.gestor_reservas.user.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNotNull;
import static org.mockito.Mockito.*;


public class UserServiceTest {
    private UserRepository repository;
    private PasswordEncoder passwordEncoder;
    private UserMapper userMapper;
    private UserService service;

    @BeforeEach
    void setUp() {
        repository = Mockito.mock(UserRepository.class);
        passwordEncoder = Mockito.mock(PasswordEncoder.class);
        userMapper = Mockito.mock(UserMapper.class);
        service = new UserService(repository, passwordEncoder, userMapper);
        
        // Setup tenant context for tests
        TenantContext.setCurrentTenant(1L);
    }
    
    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }
    @Test
    void shouldCreateUserSuccessfully() {
        // given
        User inputUser = new User("Emilio", "Quilodran", "emi@mail.com", Role.COACH, true);
        inputUser.setPassword("123");
        User savedUser = new User("Emilio", "Quilodran", "emi@mail.com", Role.COACH, true);
        savedUser.setPassword("hashed_password");
        try {
            java.lang.reflect.Field idField = User.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(savedUser, 1L);
        } catch (Exception e) {
            Assertions.fail("Error seteando ID por reflexión");
        }

        when(passwordEncoder.encode(isNotNull(String.class))).thenReturn("hashed_password");
        when(repository.existsByEmailAndTenantId("emi@mail.com", 1L)).thenReturn(false);
        when(repository.save(any(User.class))).thenReturn(savedUser);
        
        UserResponseDTO expectedResponse = new UserResponseDTO(1L, "Emilio", "emi@mail.com");
        when(userMapper.toResponse(savedUser)).thenReturn(expectedResponse);

        // when
        UserResponseDTO result = service.createUser(inputUser);

        // then
        assertNotNull(result);
        assertEquals("Emilio", result.getName());
        assertEquals("emi@mail.com", result.getEmail());
        assertEquals(1L, result.getId());

        verify(passwordEncoder, times(1)).encode("123");
        verify(repository, times(1)).save(any(User.class));
    }

    @Test
    void shouldGetAllUsersWithPagination() {
        // given
        List<User> users = List.of(
            new User("User1", "Test1", "user1@test.com", Role.USER, true),
            new User("User2", "Test2", "user2@test.com", Role.ADMIN, true)
        );
        Page<User> userPage = new PageImpl<>(users, PageRequest.of(0, 10), 2);
        List<UserResponseDTO> responseDTOs = List.of(
            new UserResponseDTO(1L, "User1", "user1@test.com"),
            new UserResponseDTO(2L, "User2", "user2@test.com")
        );
        Page<UserResponseDTO> responsePage = new PageImpl<>(responseDTOs, PageRequest.of(0, 10), 2);

        when(repository.findByTenantId(eq(1L), any(Pageable.class))).thenReturn(userPage);
        when(userMapper.toResponse(any(User.class))).thenReturn(responseDTOs.get(0), responseDTOs.get(1));

        // when
        PaginatedResponse<UserResponseDTO> result = service.getAllUsers(0, 10, "id");

        // then
        assertNotNull(result);
        assertEquals(2, result.content().size());
        assertEquals(0, result.currentPage());
        assertEquals(10, result.pageSize());
        assertEquals(2, result.totalElements());
        assertEquals(1, result.totalPages());
        assertTrue(result.first());
        assertTrue(result.last());

        verify(repository, times(1)).findByTenantId(eq(1L), any(Pageable.class));
        verify(userMapper, times(2)).toResponse(any(User.class));
    }

    @Test
    void shouldFailWhenEmailIsNull() {
        User user = new User("Emilio", "Quilodran", null, Role.COACH, true);

        Assertions.assertThrows(RuntimeException.class, () -> service.createUser(user));
    }

    @Test
    void shouldFailWhenRoleIsNull() {
        User user = new User("Emilio", "Quilodran", "emiq@mail.com", null, true);

        Assertions.assertThrows(RuntimeException.class, () -> service.createUser(user));
    }

    @Test
    void shouldReturnAllUsers() {
        List<User> users = new ArrayList<>();
        users.add(new User("Emilio", "Quilodran", "emi@mail.com", Role.COACH, true));
        users.add(new User("Aitor", "fagoaga", "aitor@mail.com", Role.ADMIN, true));

        Page<User> userPage = new PageImpl<>(users);
        when(repository.findByTenantId(eq(1L), any(Pageable.class))).thenReturn(userPage);
        when(userMapper.toResponse(any(User.class))).thenReturn(new UserResponseDTO());

        PaginatedResponse<UserResponseDTO> result = service.getAllUsers(0, 10, "id");

        assertNotNull(result);
        assertEquals(2, result.content().size());
        verify(repository).findByTenantId(eq(1L), any(Pageable.class));
    }

    @Test
    void shouldFailWhenEmailExists() {
        User user = new User("Emilio", "Quilodran", "emi@mail.com", Role.COACH, true);
        when(repository.existsByEmailAndTenantId("emi@mail.com", 1L)).thenReturn(true);

        Assertions.assertThrows(IllegalArgumentException.class, () -> service.createUser(user));

    }

    @Test
    void shouldFindUserById() {
        User user = new User("Emilio", "Quilodran", "emi@mail.com", Role.COACH, true);
        try {
            java.lang.reflect.Field idField = User.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(user, 1L);
        } catch (Exception e) {
            Assertions.fail("Error seteando ID por reflexión");
        }
        UserResponseDTO expectedResponse = new UserResponseDTO(1L, "Emilio", "emi@mail.com");
        when(repository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userMapper.toResponse(user)).thenReturn(expectedResponse);
        Optional<UserResponseDTO> result = service.getUserById(user.getId());

        assertNotNull(result);
        assertTrue(result.isPresent(), "User should be found");
        assertEquals("Emilio", result.get().getName());
        assertEquals("emi@mail.com", result.get().getEmail());
        assertEquals(1L, result.get().getId());
    }

    @Test
    void shouldDeleteUserSuccessfully() {
        User user = new User("Emilio", "Quilodran", "emi@mail.com", Role.COACH, true);
        try {
            java.lang.reflect.Field idField = User.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(user, 1L);
        } catch (Exception e) {
            Assertions.fail("Error seteando ID por reflexión");
        }
        when(repository.existsById(user.getId())).thenReturn(true);

        service.deleteUser(user.getId());

        verify(repository, times(1)).deleteById(user.getId());
    }
}

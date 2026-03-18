package com.logiair.os.user.controller;

import com.logiair.os.exceptions.ResourceNotFoundException;
import com.logiair.os.models.User;
import com.logiair.os.user.dto.PaginatedResponse;
import com.logiair.os.user.dto.UserRequestDTO;
import com.logiair.os.user.dto.UserResponseDTO;
import com.logiair.os.user.mapper.UserMapper;
import com.logiair.os.user.service.UserService;
import com.logiair.os.tenant.TenantContext;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final UserService service;
    private final UserMapper userMapper;

    public UserController(UserService service, UserMapper userMapper) {
        this.service = service;
        this.userMapper = userMapper;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDTO> create(@Valid @RequestBody UserRequestDTO request) {
        logger.info("User created: " + request.toString());
        UserResponseDTO createdUser = service.createUser(userMapper.toEntity(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR_LOGISTICS', 'ADMINISTRATION')")
    public PaginatedResponse<UserResponseDTO> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy) {
        return service.getAllUsers(page, size, sortBy);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR_LOGISTICS', 'ADMINISTRATION')")
    public ResponseEntity<UserResponseDTO> findById(@PathVariable Long id) {
        UserResponseDTO user = service.getUserById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}

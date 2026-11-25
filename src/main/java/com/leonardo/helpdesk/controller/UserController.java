package com.leonardo.helpdesk.controller;

import com.leonardo.helpdesk.dto.request.UserRequestDto;
import com.leonardo.helpdesk.dto.response.UserResponseDto;
import com.leonardo.helpdesk.dto.update.ChangePasswordUpdateDto;
import com.leonardo.helpdesk.dto.update.UserUpdateDto;
import com.leonardo.helpdesk.entity.User;
import com.leonardo.helpdesk.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "User controller", description = "User endpoints class")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }


    @Operation(summary = "Create User", description = "Creates a new user")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "User created"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "401", description = "Authentication required or invalid token"),
            @ApiResponse(responseCode = "403", description = "Authenticated user does not have permission"),
            @ApiResponse(responseCode = "409", description = "Email already registered")
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDto> create(@Valid @RequestBody UserRequestDto requestDto) {
        UserResponseDto savedUser = userService.create(requestDto);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(savedUser.id())
                .toUri();

        return ResponseEntity.created(location).body(savedUser);
    }


    @Operation(summary = "Find User", description = "Finds a user by its ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User found successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid ID format"),
            @ApiResponse(responseCode = "401", description = "Authentication required or invalid token"),
            @ApiResponse(responseCode = "403", description = "Authenticated user does not have permission"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> findById(@PathVariable UUID id) {
        UserResponseDto user = userService.findById(id);
        return ResponseEntity.ok(user);
    }

    @Operation(summary = "Return users", description = "Return all registered users")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User successfully returned"),
            @ApiResponse(responseCode = "401", description = "Authentication required or invalid token"),
            @ApiResponse(responseCode = "403", description = "Authenticated user does not have permission")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<Page<UserResponseDto>> findAll(Pageable pageable) {
        Page<UserResponseDto> users = userService.findAll(pageable);
        return ResponseEntity.ok(users);
    }

    @Operation(summary = "Find current User", description = "Find current authenticated user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User successfully found"),
            @ApiResponse(responseCode = "401", description = "Authentication required or invalid token"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> findCurrentUser(@AuthenticationPrincipal User authenticatedUser) {
        UserResponseDto user = userService.findCurrentUser(authenticatedUser);
        return ResponseEntity.ok(user);
    }

    @Operation(summary = "Update User", description = "Updates a user by its ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User successfully updated"),
            @ApiResponse(responseCode = "400", description = "Invalid request data or ID format"),
            @ApiResponse(responseCode = "401", description = "Authentication required or invalid token"),
            @ApiResponse(responseCode = "403", description = "Authenticated user does not have permission"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "409", description = "Email already registered")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}")
    public ResponseEntity<UserResponseDto> updateById(@PathVariable UUID id, @Valid @RequestBody UserUpdateDto updateDto) {
        UserResponseDto updatedUser = userService.update(id, updateDto);
        return ResponseEntity.ok(updatedUser);
    }

    @Operation(summary = "Update current User", description = "Update authenticated User")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User successfully updated"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "401", description = "Authentication required or invalid token"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "409", description = "Email already registered")
    })
    @PatchMapping("/me")
    public ResponseEntity<UserResponseDto> updateCurrentUser(@AuthenticationPrincipal User authenticatedUser, @Valid @RequestBody UserUpdateDto updateDto) {
        UserResponseDto updatedUser = userService.update(authenticatedUser.getId(), updateDto);
        return ResponseEntity.ok(updatedUser);
    }

    @Operation(summary = "Update user password", description = "Update user password by its ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Password successfully updated"),
            @ApiResponse(responseCode = "400", description = "Invalid request data or ID format"),
            @ApiResponse(responseCode = "401", description = "Authentication required or invalid token"),
            @ApiResponse(responseCode = "403", description = "Authenticated user does not have permission"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/password")
    public ResponseEntity<UserResponseDto> updatePasswordById(@PathVariable UUID id, @Valid @RequestBody ChangePasswordUpdateDto passwordUpdateDto) {
        UserResponseDto updatedUser = userService.updatePassword(id, passwordUpdateDto);
        return ResponseEntity.ok(updatedUser);
    }

    @Operation(summary = "Update current user password", description = "Updates the authenticated user's password")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Password successfully updated"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "401", description = "Authentication required or invalid token"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PutMapping("/me/password")
    public ResponseEntity<UserResponseDto> updateCurrentUserPassword(@AuthenticationPrincipal User authenticatedUser, @Valid @RequestBody ChangePasswordUpdateDto passwordUpdateDto) {
        UserResponseDto updatedUser = userService.updatePassword(authenticatedUser.getId(), passwordUpdateDto);
        return ResponseEntity.ok(updatedUser);
    }

    @Operation(summary = "Deactivate user", description = "Deactivates a user by its ID")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "User successfully deactivated"),
            @ApiResponse(responseCode = "400", description = "Invalid ID format"),
            @ApiResponse(responseCode = "401", description = "Authentication required or invalid token"),
            @ApiResponse(responseCode = "403", description = "Authenticated user does not have permission"),
            @ApiResponse(responseCode = "404", description = "User not found"),
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivate(@PathVariable UUID id) {
        userService.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}

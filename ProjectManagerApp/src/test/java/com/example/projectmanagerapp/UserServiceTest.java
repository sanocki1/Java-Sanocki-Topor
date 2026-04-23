package com.example.projectmanagerapp;

import com.example.projectmanagerapp.entity.User;
import com.example.projectmanagerapp.repository.UserRepository;
import com.example.projectmanagerapp.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class UserServiceTest {

    private UserRepository userRepository;
    private UserService userService;

    @BeforeEach
    void setup() {
        userRepository = Mockito.mock(UserRepository.class);
        userService = new UserService(userRepository);
    }

    @Test
    @DisplayName("Should return all users")
    void shouldReturnAllUsers() {
        User user1 = new User();
        user1.setUsername("TestUser1");
        User user2 = new User();
        user2.setUsername("TestUser2");

        when(userRepository.findAll()).thenReturn(Arrays.asList(user1, user2));

        List<User> users = userService.getAllUsers();

        assertEquals(2, users.size());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should return user by id")
    void shouldReturnUserById() {
        User u = new User();
        u.setId(1L);
        u.setUsername("U1");

        when(userRepository.findById(1L)).thenReturn(java.util.Optional.of(u));

        User result = userService.getUserById(1L);

        assertEquals("U1", result.getUsername());
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should throw when user not found by id")
    void shouldThrowWhenGetUserByIdNotFound() {
        when(userRepository.findById(2L)).thenReturn(java.util.Optional.empty());

        org.junit.jupiter.api.Assertions.assertThrows(org.springframework.web.server.ResponseStatusException.class,
                () -> userService.getUserById(2L));

        verify(userRepository, times(1)).findById(2L);
    }

    @Test
    @DisplayName("Should create user")
    void shouldCreateUser() {
        User u = new User();
        u.setUsername("NewUser");

        when(userRepository.save(u)).thenReturn(u);

        User created = userService.createUser(u);

        assertEquals("NewUser", created.getUsername());
        verify(userRepository, times(1)).save(u);
    }

    @Test
    @DisplayName("Should update user")
    void shouldUpdateUser() {
        User u = new User();
        u.setId(1L);
        u.setUsername("UpdatedUser");

        when(userRepository.existsById(1L)).thenReturn(true);
        when(userRepository.save(u)).thenReturn(u);

        User updated = userService.updateUser(u);

        assertEquals("UpdatedUser", updated.getUsername());
        verify(userRepository, times(1)).existsById(1L);
        verify(userRepository, times(1)).save(u);
    }

    @Test
    @DisplayName("Should throw when updating non-existing user")
    void shouldThrowWhenUpdateUserNotFound() {
        User u = new User();
        u.setId(2L);
        u.setUsername("DoesNotExist");

        when(userRepository.existsById(2L)).thenReturn(false);

        org.junit.jupiter.api.Assertions.assertThrows(org.springframework.web.server.ResponseStatusException.class,
                () -> userService.updateUser(u));

        verify(userRepository, times(1)).existsById(2L);
    }

    @Test
    @DisplayName("Should delete user")
    void shouldDeleteUser() {
        when(userRepository.existsById(1L)).thenReturn(true);

        userService.deleteUser(1L);

        verify(userRepository, times(1)).existsById(1L);
        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Should throw when deleting non-existing user")
    void shouldThrowWhenDeleteUserNotFound() {
        when(userRepository.existsById(2L)).thenReturn(false);

        org.junit.jupiter.api.Assertions.assertThrows(org.springframework.web.server.ResponseStatusException.class,
                () -> userService.deleteUser(2L));

        verify(userRepository, times(1)).existsById(2L);
    }
}

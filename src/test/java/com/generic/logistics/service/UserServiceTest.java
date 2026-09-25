package com.generic.logistics.service;

import com.generic.logistics.exception.DuplicateResourceException;
import com.generic.logistics.model.User;
import com.generic.logistics.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    public void getAllUsersSuccessfully() {
        User user = new User();
        user.setId(1L);
        user.setFirstName("First Name");
        user.setLastName("Last Name");
        user.setEmail("test@email.com");

        User user2 = new User();
        user2.setId(2L);
        user2.setFirstName("First Name 2");
        user2.setLastName("Last Name 2 ");
        user2.setEmail("test2@email.com");

        when(userRepository.findAll()).thenReturn(List.of(user, user2));
        List<User> allUsers = userService.getAllUsers();
        assertTrue(allUsers.contains(user));
        assertTrue(allUsers.contains(user2));
    }

    @Test
    public void saveUserEmailAlreadyExists() {
        String emailToTest = "test@test";
        User existingUser = new User();
        User newUser = new User();
        newUser.setEmail(emailToTest);
        when(userRepository.findByEmail(emailToTest)).thenReturn(Optional.of(existingUser));
        assertThrows(DuplicateResourceException.class, () -> userService.saveUser(newUser));
    }

    @Test
    public void saveUserSuccessfully() {
        User user = new User();
        user.setFirstName("First Name");
        user.setLastName("Last Name");
        user.setEmail("test@test");

        when(userRepository.findByEmail("test@test")).thenReturn(Optional.empty());
        when(userRepository.save(user)).thenReturn(user);
        when(passwordEncoder.encode(user.getPassword())).thenReturn(user.getPassword());

        User savedUser = userService.saveUser(user);
        assertTrue(savedUser.getEmail().equals("test@test"));
        assertTrue(savedUser.getFirstName().equals("First Name"));
        assertTrue(savedUser.getLastName().equals("Last Name"));
    }
}
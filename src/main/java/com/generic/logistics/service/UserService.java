package com.generic.logistics.service;

import com.generic.logistics.exception.DuplicateResourceException;
import com.generic.logistics.model.User;
import com.generic.logistics.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        log.info("Finding all users");
        return userRepository.findAll();
    }

    @Transactional
    public User saveUser(User user) {
        log.info("Attempting to save user with email {}", user.getEmail());

        userRepository.findByEmail(user.getEmail()).ifPresent(existingUser -> {
            throw new DuplicateResourceException("User with email" + user.getEmail() +  " already exists");
        });

        String encryptedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encryptedPassword);

        User newUser = userRepository.save(user);
        log.info("User {} {} successfully registered with id {}", user.getFirstName(), user.getLastName(), user.getId());
        return newUser;
    }
}
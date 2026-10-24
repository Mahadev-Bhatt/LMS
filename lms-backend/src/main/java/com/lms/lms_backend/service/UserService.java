package com.lms.lms_backend.service;

import com.lms.lms_backend.enums.UserType;
import com.lms.lms_backend.exception.DuplicateResourceException;
import com.lms.lms_backend.exception.BadRequestException;
import com.lms.lms_backend.exception.ResourceNotFoundException;
import com.lms.lms_backend.model.User;
import com.lms.lms_backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public User createUser(User user) {
        throw new BadRequestException("Users must sign up themselves to create a password-protected account");
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    public User getActiveByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .filter(User::isActive)
                .orElseThrow(() -> new ResourceNotFoundException("Active user account not found"));
    }

    @Transactional
    public User updateUser(Long id, User updated) {
        User existing = getUserById(id);

        // Allow email update only if the new email is not taken by a DIFFERENT user
        if (!existing.getEmail().equalsIgnoreCase(updated.getEmail())
                && userRepository.existsByEmailIgnoreCase(updated.getEmail())) {
            throw new DuplicateResourceException("Email already in use: " + updated.getEmail());
        }

        existing.setName(updated.getName());
        existing.setEmail(updated.getEmail());
        existing.setUserType(updated.getUserType());
        existing.setActive(updated.isActive());
        return userRepository.save(existing);
    }

    @Transactional
    public void deleteUser(Long id) {
        getUserById(id); // throws 404 if not found
        userRepository.deleteById(id);
    }

    public List<User> getUsersByType(UserType userType) {
        return userRepository.findByUserType(userType);
    }
}

package org.example.service;

import org.example.exception.ApiException;
import org.example.model.User;
import org.example.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public User createUser(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            throw ApiException.badRequest("Имя пользователя обязательно");
        }
        user.setId(null);
        return userRepository.save(user);
    }

    @Transactional
    public User updateUser(Long id, User updatedData) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return null;
        }
        if (updatedData.getName() != null) {
            if (updatedData.getName().isBlank()) {
                throw ApiException.badRequest("Имя пользователя не может быть пустым");
            }
            user.setName(updatedData.getName());
        }
        return user;
    }

    public boolean deleteUser(Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
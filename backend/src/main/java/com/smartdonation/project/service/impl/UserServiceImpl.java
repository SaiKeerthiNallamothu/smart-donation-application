package com.smartdonation.project.service.impl;

import com.smartdonation.project.common.exception.ResourceNotFoundException;
import com.smartdonation.project.common.exception.UserNotFoundException;
import com.smartdonation.project.dto.response.UserResponseDto;
import com.smartdonation.project.entities.User;
import com.smartdonation.project.enums.Role;
import com.smartdonation.project.repository.UserRepository;
import com.smartdonation.project.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserResponseDto getUserById(Long id) throws UserNotFoundException {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
        return mapToResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDto getUserByEmail(String email) throws UserNotFoundException {
        User user = userRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
        return mapToResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponseDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponseDto> getAllNonAdminUsers() {
        return userRepository.findByRoleNot(Role.ADMIN).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponseDto> getUsersByRole(Role role) {
        return userRepository.findAllByRole(role).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public long countUsers() {
        return userRepository.count();
    }

    @Override
    @Transactional
    public void deactivateUser(Long id) throws ResourceNotFoundException {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        if (!user.getActive()) {
            log.warn("User with id {} is already deactivated", id);
            return;
        }

        user.setActive(false);
        userRepository.save(user);
        log.info("User deactivated: id={}, email={}", id, user.getEmail());
    }

    private UserResponseDto mapToResponse(User user) {
        return UserResponseDto.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole())
                .message(null)
                .build();
    }
}

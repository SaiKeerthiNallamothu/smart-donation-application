package com.smartdonation.project.service;

import com.smartdonation.project.common.exception.ResourceNotFoundException;
import com.smartdonation.project.common.exception.UserNotFoundException;
import com.smartdonation.project.dto.response.UserResponseDto;
import com.smartdonation.project.enums.Role;

import java.util.List;

public interface UserService {

    /** Returns a user by their id. */
    UserResponseDto getUserById(Long id) throws UserNotFoundException;

    /** Returns a user by their email. */
    UserResponseDto getUserByEmail(String email) throws UserNotFoundException;

    /** Returns all registered users. */
    List<UserResponseDto> getAllUsers();

    /** Returns all users with the given role. */
    List<UserResponseDto> getUsersByRole(Role role);

    /** Returns the total number of users. */
    long countUsers();

    /** Soft-deactivates a user by id. */
    void deactivateUser(Long id) throws ResourceNotFoundException;
}

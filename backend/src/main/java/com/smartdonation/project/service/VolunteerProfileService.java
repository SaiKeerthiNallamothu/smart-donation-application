package com.smartdonation.project.service;

import com.smartdonation.project.common.exception.DuplicateResourceException;
import com.smartdonation.project.common.exception.ResourceNotFoundException;
import com.smartdonation.project.dto.request.DrivingLicenseRequest;
import com.smartdonation.project.dto.request.DrivingLicenseVerifyRequest;
import com.smartdonation.project.dto.request.VolunteerProfileRequest;
import com.smartdonation.project.dto.response.DrivingLicenseResponse;
import com.smartdonation.project.dto.response.VolunteerProfileResponse;
import com.smartdonation.project.dto.request.update.VolunteerProfileUpdateRequest;

import java.util.List;

public interface VolunteerProfileService {

    // ─── Volunteer: own profile CRUD ──────────────────────────

    /** Creates a new volunteer profile for the currently authenticated user. */
    VolunteerProfileResponse createProfile(String email, VolunteerProfileRequest request)
            throws DuplicateResourceException;

    /** Returns the volunteer profile of the currently authenticated user. */
    VolunteerProfileResponse getProfile(String email)
            throws ResourceNotFoundException;

    /** Updates the volunteer profile of the currently authenticated user. */
    VolunteerProfileResponse updateProfile(String email, VolunteerProfileUpdateRequest request)
            throws ResourceNotFoundException;

    /** Deletes the volunteer profile of the currently authenticated user. */
    void deleteProfile(String email)
            throws ResourceNotFoundException;

    // ─── Volunteer: own driving licence ───────────────────────

    /** Volunteer submits or updates their own driving licence. */
    DrivingLicenseResponse submitLicense(String email, DrivingLicenseRequest request)
            throws ResourceNotFoundException;

    /** Volunteer retrieves their own driving licence. */
    DrivingLicenseResponse getOwnLicense(String email)
            throws ResourceNotFoundException;

    // ─── Admin: volunteer management ──────────────────────────

    /** Admin: returns all volunteer profiles. */
    List<VolunteerProfileResponse> getAllVolunteerProfiles();

    /** Admin: returns volunteer profiles with pending licence verification. */
    List<VolunteerProfileResponse> getPendingVolunteerProfiles();

    /** Admin: returns a single volunteer profile by ID. */
    VolunteerProfileResponse getVolunteerProfileById(Long profileId)
            throws ResourceNotFoundException;

    /** Admin: verifies volunteer licence and confirms licence type. */
    DrivingLicenseResponse verifyLicense(Long profileId, DrivingLicenseVerifyRequest request)
            throws ResourceNotFoundException;

    /** Admin: rejects volunteer licence. */
    DrivingLicenseResponse rejectLicense(Long profileId)
            throws ResourceNotFoundException;
}

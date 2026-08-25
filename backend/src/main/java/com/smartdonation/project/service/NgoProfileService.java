package com.smartdonation.project.service;

import com.smartdonation.project.common.exception.DuplicateResourceException;
import com.smartdonation.project.common.exception.ResourceNotFoundException;
import com.smartdonation.project.dto.request.NgoProfileRequest;
import com.smartdonation.project.dto.response.NgoProfileResponse;
import com.smartdonation.project.dto.response.NgoPublicResponse;
import com.smartdonation.project.dto.request.update.NgoProfileUpdateRequest;
import com.smartdonation.project.enums.VerificationStatus;

import java.util.List;

public interface NgoProfileService {

    // ─── NGO self-service ────────────────────────────────────

    /** Creates a new NGO profile for the currently authenticated user. */
    NgoProfileResponse createProfile(String email, NgoProfileRequest request)
            throws DuplicateResourceException;

    /** Returns the NGO profile of the currently authenticated user. */
    NgoProfileResponse getProfile(String email)
            throws ResourceNotFoundException;

    /** Updates the NGO profile of the currently authenticated user. */
    NgoProfileResponse updateProfile(String email, NgoProfileUpdateRequest request)
            throws ResourceNotFoundException;

    /** Deletes the NGO profile of the currently authenticated user. */
    void deleteProfile(String email)
            throws ResourceNotFoundException;

    // ─── Admin: NGO verification ─────────────────────────────

    /** Admin: returns all NGO profiles. */
    List<NgoProfileResponse> getAllNgoProfiles();

    /** Admin: returns NGO profiles with PENDING verification status. */
    List<NgoProfileResponse> getPendingNgoProfiles();

    /** Admin: returns a single NGO profile by ID. */
    NgoProfileResponse getNgoProfileById(Long profileId) throws ResourceNotFoundException;

    /** Admin: updates the verification status (VERIFIED or REJECTED). */
    NgoProfileResponse updateVerificationStatus(Long profileId, VerificationStatus status)
            throws ResourceNotFoundException;

    // ─── Donor: view verified NGOs ───────────────────────────

    /** Donor: returns all VERIFIED NGO profiles as public responses. */
    List<NgoPublicResponse> getVerifiedNgos();

    /** Donor: returns a single VERIFIED NGO by profile ID. */
    NgoPublicResponse getVerifiedNgoById(Long profileId) throws ResourceNotFoundException;
}

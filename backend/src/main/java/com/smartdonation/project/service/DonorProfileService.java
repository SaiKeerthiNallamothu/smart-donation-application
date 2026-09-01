package com.smartdonation.project.service;

import com.smartdonation.project.common.exception.DuplicateResourceException;
import com.smartdonation.project.common.exception.ResourceNotFoundException;
import com.smartdonation.project.dto.request.DonorProfileRequest;
import com.smartdonation.project.dto.request.update.DonorProfileUpdateRequest;
import com.smartdonation.project.dto.response.DonorProfileResponse;

import java.util.List;

public interface DonorProfileService {

    /** Creates a new donor profile for the currently authenticated user. */
    DonorProfileResponse createProfile(String email, DonorProfileRequest request)
            throws DuplicateResourceException;

    /** Returns the donor profile of the currently authenticated user. */
    DonorProfileResponse getProfile(String email)
            throws ResourceNotFoundException;

    /** Updates the donor profile of the currently authenticated user. */
    DonorProfileResponse updateProfile(String email, DonorProfileUpdateRequest request)
            throws ResourceNotFoundException;

    /** Deletes the donor profile of the currently authenticated user. */
    void deleteProfile(String email)
            throws ResourceNotFoundException;

    /** Admin: returns all donor profiles. */
    List<DonorProfileResponse> getAllDonorProfiles();

    /** Admin: returns a single donor profile by profile ID. */
    DonorProfileResponse getDonorProfileById(Long profileId)
            throws ResourceNotFoundException;
}

package com.smartdonation.project.service;

import com.smartdonation.project.common.exception.DuplicateResourceException;
import com.smartdonation.project.common.exception.ResourceNotFoundException;
import com.smartdonation.project.dto.request.DonorProfileRequest;
import com.smartdonation.project.dto.response.DonorProfileResponse;

public interface DonorProfileService {

    /** Creates a new donor profile for the currently authenticated user. */
    DonorProfileResponse createProfile(String email, DonorProfileRequest request)
            throws DuplicateResourceException;

    /** Returns the donor profile of the currently authenticated user. */
    DonorProfileResponse getProfile(String email)
            throws ResourceNotFoundException;

    /** Updates the donor profile of the currently authenticated user. */
    DonorProfileResponse updateProfile(String email, DonorProfileRequest request)
            throws ResourceNotFoundException;

    /** Deletes the donor profile of the currently authenticated user. */
    void deleteProfile(String email)
            throws ResourceNotFoundException;
}

package com.smartdonation.project.service.impl;

import com.smartdonation.project.common.exception.DuplicateResourceException;
import com.smartdonation.project.common.exception.ResourceNotFoundException;
import com.smartdonation.project.dto.request.AddressRequest;
import com.smartdonation.project.dto.request.NgoProfileRequest;
import com.smartdonation.project.dto.response.AddressResponse;
import com.smartdonation.project.dto.response.NgoProfileResponse;
import com.smartdonation.project.dto.response.NgoPublicResponse;
import com.smartdonation.project.dto.response.UserResponseDto;
import com.smartdonation.project.entities.Address;
import com.smartdonation.project.entities.NgoProfile;
import com.smartdonation.project.entities.User;
import com.smartdonation.project.enums.Role;
import com.smartdonation.project.enums.VerificationStatus;
import com.smartdonation.project.repository.NgoProfileRepository;
import com.smartdonation.project.repository.UserRepository;
import com.smartdonation.project.service.NgoProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NgoProfileServiceImpl implements NgoProfileService {

    private final NgoProfileRepository ngoProfileRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public NgoProfileResponse createProfile(String email, NgoProfileRequest request) {
        User user = getUserByEmail(email);

        if (user.getRole() != Role.NGO) {
            throw new IllegalArgumentException("Only NGO users can create an NGO profile");
        }

        if (ngoProfileRepository.existsByUserId(user.getId())) {
            throw new DuplicateResourceException("NGO profile already exists for this user");
        }

        Address address = buildAddress(request.getAddress());

        NgoProfile profile = NgoProfile.builder()
                .user(user)
                .ngoName(request.getNgoName().trim())
                .registrationNumber(request.getRegistrationNumber().trim())
                .contactPersonName(request.getContactPersonName().trim())
                .description(
                        request.getDescription() != null
                                ? request.getDescription().trim()
                                : null
                )
                .website(
                        request.getWebsite() != null
                                ? request.getWebsite().trim()
                                : null
                )
                .verificationStatus(VerificationStatus.PENDING)
                .address(address)
                .build();

        NgoProfile saved = ngoProfileRepository.save(profile);
        log.info("NGO profile created for user: {}", email);

        return mapToResponse(saved, "NGO profile created successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public NgoProfileResponse getProfile(String email) {
        User user = getUserByEmail(email);

        NgoProfile profile = ngoProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("NGO profile not found"));

        return mapToResponse(profile, "NGO profile fetched successfully");
    }

    @Override
    @Transactional
    public NgoProfileResponse updateProfile(String email, NgoProfileRequest request) {
        User user = getUserByEmail(email);

        NgoProfile profile = ngoProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("NGO profile not found"));

        profile.setNgoName(request.getNgoName().trim());
        profile.setRegistrationNumber(request.getRegistrationNumber().trim());
        profile.setContactPersonName(request.getContactPersonName().trim());
        profile.setDescription(
                request.getDescription() != null ? request.getDescription().trim() : null
        );
        profile.setWebsite(
                request.getWebsite() != null ? request.getWebsite().trim() : null
        );

        // Update address fields
        Address address = profile.getAddress();
        AddressRequest addrReq = request.getAddress();
        address.setAddressLine1(addrReq.getAddressLine1().trim());
        address.setAddressLine2(
                addrReq.getAddressLine2() != null ? addrReq.getAddressLine2().trim() : null
        );
        address.setCity(addrReq.getCity().trim());
        address.setState(addrReq.getState().trim());
        address.setPincode(addrReq.getPincode().trim());
        address.setCountry(addrReq.getCountry().trim());

        NgoProfile saved = ngoProfileRepository.save(profile);
        log.info("NGO profile updated for user: {}", email);

        return mapToResponse(saved, "NGO profile updated successfully");
    }

    @Override
    @Transactional
    public void deleteProfile(String email) {
        User user = getUserByEmail(email);

        NgoProfile profile = ngoProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("NGO profile not found"));

        ngoProfileRepository.delete(profile);
        log.info("NGO profile deleted for user: {}", email);
    }

    // ─── Admin: NGO verification ─────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<NgoProfileResponse> getAllNgoProfiles() {
        return ngoProfileRepository.findAll().stream()
                .map(p -> mapToResponse(p, null))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<NgoProfileResponse> getPendingNgoProfiles() {
        return ngoProfileRepository.findByVerificationStatus(VerificationStatus.PENDING).stream()
                .map(p -> mapToResponse(p, null))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public NgoProfileResponse getNgoProfileById(Long profileId) throws ResourceNotFoundException {
        NgoProfile profile = ngoProfileRepository.findById(profileId)
                .orElseThrow(() -> new ResourceNotFoundException("NGO profile not found with id: " + profileId));
        return mapToResponse(profile, null);
    }

    @Override
    @Transactional
    public NgoProfileResponse updateVerificationStatus(Long profileId, VerificationStatus status)
            throws ResourceNotFoundException {
        if (status != VerificationStatus.VERIFIED && status != VerificationStatus.REJECTED) {
            throw new IllegalArgumentException("Only VERIFIED or REJECTED status is allowed");
        }

        NgoProfile profile = ngoProfileRepository.findById(profileId)
                .orElseThrow(() -> new ResourceNotFoundException("NGO profile not found with id: " + profileId));

        profile.setVerificationStatus(status);
        NgoProfile saved = ngoProfileRepository.save(profile);
        log.info("NGO profile {} verification status updated to {} by admin", profileId, status);

        return mapToResponse(saved, "NGO profile verification status updated to " + status);
    }

    // ─── Donor: view verified NGOs ───────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<NgoPublicResponse> getVerifiedNgos() {
        return ngoProfileRepository.findByVerificationStatus(VerificationStatus.VERIFIED).stream()
                .map(this::mapToPublicResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public NgoPublicResponse getVerifiedNgoById(Long profileId) throws ResourceNotFoundException {
        NgoProfile profile = ngoProfileRepository.findByIdAndVerificationStatus(
                        profileId, VerificationStatus.VERIFIED)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Verified NGO profile not found with id: " + profileId));
        return mapToPublicResponse(profile);
    }

    // ─── helpers ───────────────────────────────────────────────

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    private Address buildAddress(AddressRequest req) {
        return Address.builder()
                .addressLine1(req.getAddressLine1().trim())
                .addressLine2(req.getAddressLine2() != null ? req.getAddressLine2().trim() : null)
                .city(req.getCity().trim())
                .state(req.getState().trim())
                .pincode(req.getPincode().trim())
                .country(req.getCountry().trim())
                .build();
    }

    private NgoProfileResponse mapToResponse(NgoProfile profile, String message) {
        User user = profile.getUser();
        Address addr = profile.getAddress();

        UserResponseDto userDto = UserResponseDto.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole())
                .build();

        AddressResponse addressResponse = AddressResponse.builder()
                .id(addr.getId())
                .addressLine1(addr.getAddressLine1())
                .addressLine2(addr.getAddressLine2())
                .city(addr.getCity())
                .state(addr.getState())
                .pincode(addr.getPincode())
                .country(addr.getCountry())
                .build();

        return NgoProfileResponse.builder()
                .profileId(profile.getId())
                .ngoName(profile.getNgoName())
                .registrationNumber(profile.getRegistrationNumber())
                .contactPersonName(profile.getContactPersonName())
                .description(profile.getDescription())
                .website(profile.getWebsite())
                .verificationStatus(profile.getVerificationStatus())
                .user(userDto)
                .address(addressResponse)
                .message(message)
                .build();
    }

    private NgoPublicResponse mapToPublicResponse(NgoProfile profile) {
        Address addr = profile.getAddress();

        AddressResponse addressResponse = AddressResponse.builder()
                .id(addr.getId())
                .addressLine1(addr.getAddressLine1())
                .addressLine2(addr.getAddressLine2())
                .city(addr.getCity())
                .state(addr.getState())
                .pincode(addr.getPincode())
                .country(addr.getCountry())
                .build();

        return NgoPublicResponse.builder()
                .profileId(profile.getId())
                .ngoName(profile.getNgoName())
                .description(profile.getDescription())
                .website(profile.getWebsite())
                .verificationStatus(profile.getVerificationStatus())
                .address(addressResponse)
                .build();
    }
}

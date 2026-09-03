package com.smartdonation.project.service.impl;

import com.smartdonation.project.common.exception.DuplicateResourceException;
import com.smartdonation.project.common.exception.ResourceNotFoundException;
import com.smartdonation.project.dto.request.AddressRequest;
import com.smartdonation.project.dto.request.DonorProfileRequest;
import com.smartdonation.project.dto.request.update.DonorProfileUpdateRequest;
import com.smartdonation.project.dto.response.AddressResponse;
import com.smartdonation.project.dto.response.DonorProfileResponse;
import com.smartdonation.project.dto.response.UserResponseDto;
import com.smartdonation.project.entities.Address;
import com.smartdonation.project.entities.DonorProfile;
import com.smartdonation.project.entities.User;
import com.smartdonation.project.enums.Role;
import com.smartdonation.project.repository.DonorProfileRepository;
import com.smartdonation.project.repository.UserRepository;
import com.smartdonation.project.service.DonorProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DonorProfileServiceImpl implements DonorProfileService {

    private final DonorProfileRepository donorProfileRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public DonorProfileResponse createProfile(String email, DonorProfileRequest request) {
        User user = getUserByEmail(email);

        if (user.getRole() != Role.DONOR) {
            throw new IllegalArgumentException("Only donors can create a donor profile");
        }

        if (donorProfileRepository.existsByUserId(user.getId())) {
            throw new DuplicateResourceException("Donor profile already exists for this user");
        }

        Address address = buildAddress(request.getAddress());

        DonorProfile profile = DonorProfile.builder()
                .user(user)
                .dob(request.getDob())
                .gender(request.getGender())
                .alternativePhone(
                        request.getAlternativePhone() != null
                                ? request.getAlternativePhone().trim()
                                : null
                )
                .address(address)
                .build();

        DonorProfile saved = donorProfileRepository.save(profile);
        log.info("Donor profile created for user: {}", email);

        return mapToResponse(saved, "Profile created successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public DonorProfileResponse getProfile(String email) {
        User user = getUserByEmail(email);

        DonorProfile profile = donorProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Donor profile not found"));

        return mapToResponse(profile, "Profile fetched successfully");
    }

    @Override
    @Transactional
    public DonorProfileResponse updateProfile(String email, DonorProfileUpdateRequest request) {
        User user = getUserByEmail(email);

        DonorProfile profile = donorProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Donor profile not found"));

        // Update profile fields only if supplied
        if (request.getDob() != null) {
            profile.setDob(request.getDob());
        }
        if (request.getGender() != null) {
            profile.setGender(request.getGender());
        }
        if (request.getAlternativePhone() != null) {
            profile.setAlternativePhone(request.getAlternativePhone().trim());
        }

        // Update user fields only if supplied
        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName().trim());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName().trim());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone().trim());
        }

        // Update address fields only if supplied
        if (request.getAddress() != null) {
            Address address = profile.getAddress();
            AddressRequest addrReq = request.getAddress();
            if (addrReq.getAddressLine1() != null) {
                address.setAddressLine1(addrReq.getAddressLine1().trim());
            }
            if (addrReq.getAddressLine2() != null) {
                address.setAddressLine2(addrReq.getAddressLine2().trim());
            }
            if (addrReq.getCity() != null) {
                address.setCity(addrReq.getCity().trim());
            }
            if (addrReq.getState() != null) {
                address.setState(addrReq.getState().trim());
            }
            if (addrReq.getPincode() != null) {
                address.setPincode(addrReq.getPincode().trim());
            }
            if (addrReq.getCountry() != null) {
                address.setCountry(addrReq.getCountry().trim());
            }
        }

        DonorProfile saved = donorProfileRepository.save(profile);
        log.info("Donor profile updated for user: {}", email);

        return mapToResponse(saved, "Profile updated successfully");
    }

    @Override
    @Transactional
    public void deleteProfile(String email) {
        User user = getUserByEmail(email);

        DonorProfile profile = donorProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Donor profile not found"));

        donorProfileRepository.delete(profile);
        log.info("Donor profile deleted for user: {}", email);
    }

    // ═══════════════════════════════════════════════════════════
    // ADMIN → DONOR MANAGEMENT
    // ═══════════════════════════════════════════════════════════

    @Override
    @Transactional(readOnly = true)
    public List<DonorProfileResponse> getAllDonorProfiles() {
        return donorProfileRepository.findAll()
                .stream()
                .map(profile -> mapToResponse(profile, null))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public DonorProfileResponse getDonorProfileById(Long profileId) {
        DonorProfile profile = donorProfileRepository.findById(profileId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Donor profile not found with id: " + profileId
                        )
                );
        return mapToResponse(profile, null);
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

    private DonorProfileResponse mapToResponse(DonorProfile profile, String message) {
        User user = profile.getUser();
        Address addr = profile.getAddress();

        UserResponseDto userResponse = UserResponseDto.builder()
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

        return DonorProfileResponse.builder()
                .profileId(profile.getId())
                .dob(profile.getDob())
                .gender(profile.getGender())
                .alternativePhone(profile.getAlternativePhone())
                .user(userResponse)
                .address(addressResponse)
                .message(message)
                .build();
    }
}

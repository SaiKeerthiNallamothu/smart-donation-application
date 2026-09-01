package com.smartdonation.project.service.impl;

import com.smartdonation.project.common.exception.DuplicateResourceException;
import com.smartdonation.project.common.exception.ResourceNotFoundException;
import com.smartdonation.project.dto.request.AddressRequest;
import com.smartdonation.project.dto.request.DrivingLicenseRequest;
import com.smartdonation.project.dto.request.DrivingLicenseVerifyRequest;
import com.smartdonation.project.dto.request.VolunteerProfileRequest;
import com.smartdonation.project.dto.response.AddressResponse;
import com.smartdonation.project.dto.response.DrivingLicenseResponse;
import com.smartdonation.project.dto.response.VolunteerProfileResponse;
import com.smartdonation.project.dto.request.update.VolunteerProfileUpdateRequest;
import com.smartdonation.project.entities.Address;
import com.smartdonation.project.entities.DrivingLicense;
import com.smartdonation.project.entities.User;
import com.smartdonation.project.entities.VolunteerProfile;
import com.smartdonation.project.enums.LicenseType;
import com.smartdonation.project.enums.LicenseVerificationStatus;
import com.smartdonation.project.enums.Role;
import com.smartdonation.project.enums.VehicleType;
import com.smartdonation.project.repository.DrivingLicenseRepository;
import com.smartdonation.project.repository.UserRepository;
import com.smartdonation.project.repository.VolunteerProfileRepository;
import com.smartdonation.project.service.VolunteerProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class VolunteerProfileServiceImpl implements VolunteerProfileService {

    private final VolunteerProfileRepository volunteerProfileRepository;
    private final DrivingLicenseRepository drivingLicenseRepository;
    private final UserRepository userRepository;

    private static final int MIN_VOLUNTEER_AGE = 18;
    private static final int MIN_COMMERCIAL_AGE = 20;

    // ═══════════════════════════════════════════════════════════
    // VOLUNTEER → OWN PROFILE CRUD
    // ═══════════════════════════════════════════════════════════

    @Override
    @Transactional
    public VolunteerProfileResponse createProfile(String email, VolunteerProfileRequest request) {
        User user = getUserByEmail(email);

        if (user.getRole() != Role.VOLUNTEER) {
            throw new IllegalArgumentException("Only volunteers can create a volunteer profile");
        }

        if (volunteerProfileRepository.existsByUserId(user.getId())) {
            throw new DuplicateResourceException("Volunteer profile already exists for this user");
        }

        validateAge(request.getDob());
        validateVehicleEligibility(request.getDob(), request.getVehicleType());

        Address address = buildAddress(request.getAddress());

        // Validate driving license dates
        DrivingLicenseRequest licenseRequest = request.getDrivingLicense();
        validateLicenseDates(licenseRequest.getIssueDate(), licenseRequest.getExpiryDate());

        // Determine initial licence verification status based on expiry
        LicenseVerificationStatus licenseStatus;
        if (licenseRequest.getExpiryDate().isBefore(LocalDate.now())) {
            licenseStatus = LicenseVerificationStatus.EXPIRED;
        } else {
            licenseStatus = LicenseVerificationStatus.PENDING;
        }

        // Create DrivingLicense
        DrivingLicense drivingLicense = DrivingLicense.builder()
                .licenseNumber(licenseRequest.getLicenseNumber().trim())
                .issueDate(licenseRequest.getIssueDate())
                .expiryDate(licenseRequest.getExpiryDate())
                .issuingState(licenseRequest.getIssuingState().trim())
                .documentUrl(
                        licenseRequest.getDocumentUrl() != null
                                ? licenseRequest.getDocumentUrl().trim()
                                : null
                )
                .licenseType(null)
                .verificationStatus(licenseStatus)
                .build();

        // Create VolunteerProfile with DrivingLicense
        VolunteerProfile profile = VolunteerProfile.builder()
                .user(user)
                .dob(request.getDob())
                .gender(request.getGender())
                .alternativePhone(
                        request.getAlternativePhone() != null
                                ? request.getAlternativePhone().trim()
                                : null
                )
                .availabilityStatus(request.getAvailabilityStatus())
                .vehicleType(request.getVehicleType())
                .address(address)
                .drivingLicense(drivingLicense)
                .build();

        VolunteerProfile saved = volunteerProfileRepository.save(profile);
        log.info("Volunteer profile created for user: {}", email);

        return mapToResponse(saved, "Volunteer profile created successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public VolunteerProfileResponse getProfile(String email) {
        User user = getUserByEmail(email);

        VolunteerProfile profile = volunteerProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Volunteer profile not found"));

        return mapToResponse(profile, "Volunteer profile fetched successfully");
    }

    @Override
    @Transactional
    public VolunteerProfileResponse updateProfile(String email, VolunteerProfileUpdateRequest request) {
        User user = getUserByEmail(email);

        VolunteerProfile profile = volunteerProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Volunteer profile not found"));

        validateAge(request.getDob());
        validateVehicleEligibility(request.getDob(), request.getVehicleType());

        // Update User fields
        user.setFirstName(request.getFirstName().trim());
        user.setLastName(request.getLastName().trim());
        user.setPhone(request.getPhone().trim());

        // Update Volunteer-specific fields
        profile.setDob(request.getDob());
        profile.setGender(request.getGender());
        profile.setAlternativePhone(
                request.getAlternativePhone() != null
                        ? request.getAlternativePhone().trim()
                        : null
        );
        profile.setAvailabilityStatus(request.getAvailabilityStatus());
        profile.setVehicleType(request.getVehicleType());

        // Update Address fields
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

        // Update Driving License
        DrivingLicense license = profile.getDrivingLicense();
        if (license == null) {
            throw new ResourceNotFoundException("No driving licence found for this volunteer profile");
        }

        DrivingLicenseRequest licenseRequest = request.getDrivingLicense();
        validateLicenseDates(licenseRequest.getIssueDate(), licenseRequest.getExpiryDate());

        // Detect if licence information changed (triggers re-verification)
        boolean licenseChanged =
                !license.getLicenseNumber().equalsIgnoreCase(licenseRequest.getLicenseNumber().trim())
                || !license.getIssueDate().equals(licenseRequest.getIssueDate())
                || !license.getExpiryDate().equals(licenseRequest.getExpiryDate())
                || !license.getIssuingState().equalsIgnoreCase(licenseRequest.getIssuingState().trim())
                || !Objects.equals(
                        license.getDocumentUrl(),
                        licenseRequest.getDocumentUrl() != null
                                ? licenseRequest.getDocumentUrl().trim()
                                : null
                );

        // Update licence fields
        license.setLicenseNumber(licenseRequest.getLicenseNumber().trim());
        license.setIssueDate(licenseRequest.getIssueDate());
        license.setExpiryDate(licenseRequest.getExpiryDate());
        license.setIssuingState(licenseRequest.getIssuingState().trim());
        license.setDocumentUrl(
                licenseRequest.getDocumentUrl() != null
                        ? licenseRequest.getDocumentUrl().trim()
                        : null
        );

        // Reset verification if licence details changed
        if (licenseChanged) {
            if (licenseRequest.getExpiryDate().isBefore(LocalDate.now())) {
                license.setVerificationStatus(LicenseVerificationStatus.EXPIRED);
            } else {
                license.setVerificationStatus(LicenseVerificationStatus.PENDING);
            }
            license.setLicenseType(null);
            log.info("Driving licence details changed for volunteer: {} — verification reset", email);
        }

        VolunteerProfile saved = volunteerProfileRepository.save(profile);
        log.info("Volunteer profile updated for user: {}", email);

        return mapToResponse(saved, "Volunteer profile updated successfully");
    }

    @Override
    @Transactional
    public void deleteProfile(String email) {
        User user = getUserByEmail(email);

        VolunteerProfile profile = volunteerProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Volunteer profile not found"));

        volunteerProfileRepository.delete(profile);
        log.info("Volunteer profile deleted for user: {}", email);
    }

    // ═══════════════════════════════════════════════════════════
    // VOLUNTEER → OWN DRIVING LICENCE
    // ═══════════════════════════════════════════════════════════

    @Override
    @Transactional
    public DrivingLicenseResponse submitLicense(String email, DrivingLicenseRequest request) {
        User user = getUserByEmail(email);

        VolunteerProfile profile = volunteerProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Volunteer profile not found"));

        validateLicenseDates(request.getIssueDate(), request.getExpiryDate());

        DrivingLicense license = profile.getDrivingLicense();
        if (license == null) {
            LicenseVerificationStatus licenseStatus;
            if (request.getExpiryDate().isBefore(LocalDate.now())) {
                licenseStatus = LicenseVerificationStatus.EXPIRED;
            } else {
                licenseStatus = LicenseVerificationStatus.PENDING;
            }

            license = DrivingLicense.builder()
                    .licenseNumber(request.getLicenseNumber().trim())
                    .issueDate(request.getIssueDate())
                    .expiryDate(request.getExpiryDate())
                    .issuingState(request.getIssuingState().trim())
                    .documentUrl(
                            request.getDocumentUrl() != null
                                    ? request.getDocumentUrl().trim()
                                    : null
                    )
                    .licenseType(null)
                    .verificationStatus(licenseStatus)
                    .build();
        } else {
            license.setLicenseNumber(request.getLicenseNumber().trim());
            license.setIssueDate(request.getIssueDate());
            license.setExpiryDate(request.getExpiryDate());
            license.setIssuingState(request.getIssuingState().trim());
            license.setDocumentUrl(
                    request.getDocumentUrl() != null
                            ? request.getDocumentUrl().trim()
                            : null
            );

            if (request.getExpiryDate().isBefore(LocalDate.now())) {
                license.setVerificationStatus(LicenseVerificationStatus.EXPIRED);
            } else {
                license.setVerificationStatus(LicenseVerificationStatus.PENDING);
            }
            license.setLicenseType(null);
        }

        DrivingLicense saved = drivingLicenseRepository.save(license);
        log.info("Driving licence submitted for volunteer: {}", email);

        return mapToLicenseResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public DrivingLicenseResponse getOwnLicense(String email) {
        User user = getUserByEmail(email);

        VolunteerProfile profile = volunteerProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Volunteer profile not found"));

        DrivingLicense license = profile.getDrivingLicense();
        if (license == null) {
            throw new ResourceNotFoundException("No driving licence found for this volunteer");
        }

        return mapToLicenseResponse(license);
    }

    // ═══════════════════════════════════════════════════════════
    // ADMIN → VOLUNTEER MANAGEMENT
    // ═══════════════════════════════════════════════════════════

    @Override
    @Transactional(readOnly = true)
    public List<VolunteerProfileResponse> getAllVolunteerProfiles() {
        return volunteerProfileRepository.findAll().stream()
                .map(p -> mapToResponse(p, null))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<VolunteerProfileResponse> getPendingVolunteerProfiles() {
        return volunteerProfileRepository.findAll().stream()
                .filter(p -> p.getDrivingLicense() != null)
                .filter(p -> p.getDrivingLicense().getVerificationStatus()
                        == LicenseVerificationStatus.PENDING)
                .map(p -> mapToResponse(p, null))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public VolunteerProfileResponse getVolunteerProfileById(Long profileId) {
        VolunteerProfile profile = volunteerProfileRepository.findById(profileId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Volunteer profile not found with id: " + profileId));
        return mapToResponse(profile, null);
    }

    @Override
    @Transactional
    public DrivingLicenseResponse verifyLicense(Long profileId, DrivingLicenseVerifyRequest request) {
        VolunteerProfile profile = volunteerProfileRepository.findById(profileId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Volunteer profile not found with id: " + profileId));

        if (profile.getDrivingLicense() == null) {
            throw new ResourceNotFoundException("No driving licence found for this volunteer");
        }

        DrivingLicense license = profile.getDrivingLicense();

        // Check licence is not expired
        if (license.getExpiryDate().isBefore(LocalDate.now())) {
            license.setVerificationStatus(LicenseVerificationStatus.EXPIRED);
            drivingLicenseRepository.save(license);
            throw new IllegalArgumentException("Driving licence has expired");
        }

        // Check volunteer age when verifying COMMERCIAL licence
        if (request.getLicenseType() == LicenseType.COMMERCIAL) {
            int age = Period.between(profile.getDob(), LocalDate.now()).getYears();
            if (age < MIN_COMMERCIAL_AGE) {
                throw new IllegalArgumentException(
                        "Volunteer must be at least 20 years old to have a commercial licence");
            }
        }

        license.setLicenseType(request.getLicenseType());
        license.setVerificationStatus(LicenseVerificationStatus.VERIFIED);
        DrivingLicense saved = drivingLicenseRepository.save(license);

        log.info("Driving licence verified for volunteer profile: {} with type: {}",
                profileId, request.getLicenseType());

        return mapToLicenseResponse(saved);
    }

    @Override
    @Transactional
    public DrivingLicenseResponse rejectLicense(Long profileId) {
        VolunteerProfile profile = volunteerProfileRepository.findById(profileId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Volunteer profile not found with id: " + profileId));

        if (profile.getDrivingLicense() == null) {
            throw new ResourceNotFoundException("No driving licence found for this volunteer");
        }

        DrivingLicense license = profile.getDrivingLicense();
        license.setVerificationStatus(LicenseVerificationStatus.REJECTED);
        DrivingLicense saved = drivingLicenseRepository.save(license);

        log.info("Driving licence rejected for volunteer profile: {}", profileId);

        return mapToLicenseResponse(saved);
    }

    // ═══════════════════════════════════════════════════════════
    // VALIDATION HELPERS
    // ═══════════════════════════════════════════════════════════

    private void validateAge(LocalDate dob) {
        int age = Period.between(dob, LocalDate.now()).getYears();
        if (age < MIN_VOLUNTEER_AGE) {
            throw new IllegalArgumentException("Volunteer must be at least 18 years old");
        }
    }

    private void validateVehicleEligibility(LocalDate dob, VehicleType vehicleType) {
        if (vehicleType == VehicleType.COMMERCIAL) {
            int age = Period.between(dob, LocalDate.now()).getYears();
            if (age < MIN_COMMERCIAL_AGE) {
                throw new IllegalArgumentException(
                        "Volunteer must be at least 20 years old to select a commercial vehicle");
            }
        }
    }

    private void validateLicenseDates(LocalDate issueDate, LocalDate expiryDate) {
        if (issueDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Issue date cannot be in the future");
        }
        if (expiryDate.isBefore(issueDate) || expiryDate.isEqual(issueDate)) {
            throw new IllegalArgumentException("Expiry date must be after issue date");
        }
    }

    // ═══════════════════════════════════════════════════════════
    // INTERNAL HELPERS
    // ═══════════════════════════════════════════════════════════

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with email: " + email));
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

    private VolunteerProfileResponse mapToResponse(VolunteerProfile profile, String message) {
        User user = profile.getUser();
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

        DrivingLicenseResponse licenseResponse = null;
        if (profile.getDrivingLicense() != null) {
            licenseResponse = mapToLicenseResponse(profile.getDrivingLicense());
        }

        return VolunteerProfileResponse.builder()
                .id(profile.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .dob(profile.getDob())
                .gender(profile.getGender())
                .alternativePhone(profile.getAlternativePhone())
                .availabilityStatus(profile.getAvailabilityStatus())
                .vehicleType(profile.getVehicleType())
                .address(addressResponse)
                .drivingLicense(licenseResponse)
                .message(message)
                .build();
    }

    private DrivingLicenseResponse mapToLicenseResponse(DrivingLicense license) {
        return DrivingLicenseResponse.builder()
                .id(license.getId())
                .licenseNumber(license.getLicenseNumber())
                .issueDate(license.getIssueDate())
                .expiryDate(license.getExpiryDate())
                .issuingState(license.getIssuingState())
                .documentUrl(license.getDocumentUrl())
                .licenseType(license.getLicenseType())
                .verificationStatus(license.getVerificationStatus())
                .build();
    }
}

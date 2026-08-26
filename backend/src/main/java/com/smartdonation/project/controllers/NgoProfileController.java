package com.smartdonation.project.controllers;

import com.smartdonation.project.common.exception.DuplicateResourceException;
import com.smartdonation.project.common.exception.ResourceNotFoundException;
import com.smartdonation.project.dto.request.NgoProfileRequest;
import com.smartdonation.project.dto.response.NgoProfileResponse;
import com.smartdonation.project.dto.response.NgoPublicResponse;
import com.smartdonation.project.dto.request.update.NgoProfileUpdateRequest;
import com.smartdonation.project.enums.VerificationStatus;
import com.smartdonation.project.service.NgoProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class NgoProfileController {

    private final NgoProfileService ngoProfileService;

    // ═══════════════════════════════════════════════════════════
    // NGO → OWN PROFILE
    // ═══════════════════════════════════════════════════════════

    @PostMapping("/api/v1/ngo/profile/create")
    public ResponseEntity<NgoProfileResponse> createProfile(
            Authentication authentication,
            @Valid @RequestBody NgoProfileRequest request)
            throws DuplicateResourceException {
        String email = authentication.getName();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ngoProfileService.createProfile(email, request));
    }

    @GetMapping("/api/v1/ngo/profile/get")
    public ResponseEntity<NgoProfileResponse> getProfile(Authentication authentication)
            throws ResourceNotFoundException {
        String email = authentication.getName();
        return ResponseEntity.ok(ngoProfileService.getProfile(email));
    }

    @PutMapping("/api/v1/ngo/profile/update")
    public ResponseEntity<NgoProfileResponse> updateProfile(
            Authentication authentication,
            @Valid @RequestBody NgoProfileUpdateRequest request)
            throws ResourceNotFoundException {
        String email = authentication.getName();
        return ResponseEntity.ok(ngoProfileService.updateProfile(email, request));
    }

    @DeleteMapping("/api/v1/ngo/profile/delete")
    public ResponseEntity<Void> deleteProfile(Authentication authentication)
            throws ResourceNotFoundException {
        String email = authentication.getName();
        ngoProfileService.deleteProfile(email);
        return ResponseEntity.noContent().build();
    }

    // ═══════════════════════════════════════════════════════════
    // ADMIN → NGO MANAGEMENT
    // ═══════════════════════════════════════════════════════════

    @GetMapping("/api/v1/admin/ngos")
    public ResponseEntity<List<NgoProfileResponse>> getAllNgos() {
        return ResponseEntity.ok(ngoProfileService.getAllNgoProfiles());
    }

    @GetMapping("/api/v1/admin/ngos/pending")
    public ResponseEntity<List<NgoProfileResponse>> getPendingNgos() {
        return ResponseEntity.ok(ngoProfileService.getPendingNgoProfiles());
    }

    @GetMapping("/api/v1/admin/ngos/{id}")
    public ResponseEntity<NgoProfileResponse> getNgoById(@PathVariable Long id)
            throws ResourceNotFoundException {
        return ResponseEntity.ok(ngoProfileService.getNgoProfileById(id));
    }

    @PatchMapping("/api/v1/admin/ngos/{id}/verify")
    public ResponseEntity<NgoProfileResponse> verifyNgo(@PathVariable Long id)
            throws ResourceNotFoundException {
        return ResponseEntity.ok(ngoProfileService.updateVerificationStatus(id, VerificationStatus.VERIFIED));
    }

    @PatchMapping("/api/v1/admin/ngos/{id}/reject")
    public ResponseEntity<NgoProfileResponse> rejectNgo(@PathVariable Long id)
            throws ResourceNotFoundException {
        return ResponseEntity.ok(ngoProfileService.updateVerificationStatus(id, VerificationStatus.REJECTED));
    }

    // ═══════════════════════════════════════════════════════════
    // DONOR → VIEW VERIFIED NGOs
    // ═══════════════════════════════════════════════════════════

    @GetMapping("/api/v1/ngos")
    public ResponseEntity<List<NgoPublicResponse>> getVerifiedNgos() {
        return ResponseEntity.ok(ngoProfileService.getVerifiedNgos());
    }

    @GetMapping("/api/v1/ngos/{id}")
    public ResponseEntity<NgoPublicResponse> getVerifiedNgoById(@PathVariable Long id)
            throws ResourceNotFoundException {
        return ResponseEntity.ok(ngoProfileService.getVerifiedNgoById(id));
    }
}

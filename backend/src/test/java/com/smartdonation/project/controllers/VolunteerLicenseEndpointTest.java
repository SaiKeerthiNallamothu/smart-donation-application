package com.smartdonation.project.controllers;

import com.smartdonation.project.common.exception.ResourceNotFoundException;
import com.smartdonation.project.dto.request.DrivingLicenseRequest;
import com.smartdonation.project.dto.response.DrivingLicenseResponse;
import com.smartdonation.project.entities.User;
import com.smartdonation.project.enums.LicenseVerificationStatus;
import com.smartdonation.project.enums.Role;
import com.smartdonation.project.security.JwtUtil;
import com.smartdonation.project.service.VolunteerProfileService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for Volunteer → own driving licence endpoints.
 *
 * <p>Uses {@code @SpringBootTest} so the full security filter chain is active.
 * Real JWT tokens are generated via {@link JwtUtil} and passed in the
 * {@code Authorization} header.
 */
@SpringBootTest
@AutoConfigureMockMvc
class VolunteerLicenseEndpointTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @MockitoBean
    private VolunteerProfileService volunteerProfileService;

    private String volunteerToken;
    private String adminToken;
    private String donorToken;
    private String ngoToken;

    @BeforeEach
    void setUp() {
        volunteerToken = tokenFor("volunteer@example.com", Role.VOLUNTEER);
        adminToken = tokenFor("admin@example.com", Role.ADMIN);
        donorToken = tokenFor("donor@example.com", Role.DONOR);
        ngoToken = tokenFor("ngo@example.com", Role.NGO);
    }

    // ═══════════════════════════════════════════════════════════
    // POST /api/v1/volunteer/license/submit
    // ═══════════════════════════════════════════════════════════

    @Test
    void submitLicense_WithVolunteerRole_ReturnsCreated() throws Exception {
        DrivingLicenseRequest request = buildLicenseRequest();
        DrivingLicenseResponse response = buildLicenseResponse(1L, "DL-12345");

        when(volunteerProfileService.submitLicense(any(String.class), any(DrivingLicenseRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/volunteer/license/submit")
                        .header("Authorization", "Bearer " + volunteerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.licenseNumber").value("DL-12345"))
                .andExpect(jsonPath("$.issuingState").value("Maharashtra"))
                .andExpect(jsonPath("$.verificationStatus").value("PENDING"));
    }

    @Test
    void submitLicense_MissingRequiredField_ReturnsBadRequest() throws Exception {
        String invalidRequest = """
                {
                    "licenseNumber": "DL-12345"
                }
                """;

        mockMvc.perform(post("/api/v1/volunteer/license/submit")
                        .header("Authorization", "Bearer " + volunteerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    void submitLicense_WithAdminRole_ReturnsForbidden() throws Exception {
        DrivingLicenseRequest request = buildLicenseRequest();

        mockMvc.perform(post("/api/v1/volunteer/license/submit")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void submitLicense_WithDonorRole_ReturnsForbidden() throws Exception {
        DrivingLicenseRequest request = buildLicenseRequest();

        mockMvc.perform(post("/api/v1/volunteer/license/submit")
                        .header("Authorization", "Bearer " + donorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void submitLicense_WithNgoRole_ReturnsForbidden() throws Exception {
        DrivingLicenseRequest request = buildLicenseRequest();

        mockMvc.perform(post("/api/v1/volunteer/license/submit")
                        .header("Authorization", "Bearer " + ngoToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void submitLicense_Unauthenticated_ReturnsUnauthorized() throws Exception {
        DrivingLicenseRequest request = buildLicenseRequest();

        mockMvc.perform(post("/api/v1/volunteer/license/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    // ═══════════════════════════════════════════════════════════
    // GET /api/v1/volunteer/license
    // ═══════════════════════════════════════════════════════════

    @Test
    void getOwnLicense_WithVolunteerRole_ReturnsOk() throws Exception {
        DrivingLicenseResponse response = buildLicenseResponse(5L, "DL-67890");

        when(volunteerProfileService.getOwnLicense(any(String.class)))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/volunteer/license")
                        .header("Authorization", "Bearer " + volunteerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.licenseNumber").value("DL-67890"))
                .andExpect(jsonPath("$.issuingState").value("Maharashtra"))
                .andExpect(jsonPath("$.verificationStatus").value("PENDING"));
    }

    @Test
    void getOwnLicense_NoLicenseFound_ReturnsNotFound() throws Exception {
        when(volunteerProfileService.getOwnLicense(any(String.class)))
                .thenThrow(new ResourceNotFoundException("No driving licence found for this volunteer"));

        mockMvc.perform(get("/api/v1/volunteer/license")
                        .header("Authorization", "Bearer " + volunteerToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("No driving licence found for this volunteer"));
    }

    @Test
    void getOwnLicense_WithAdminRole_ReturnsForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/volunteer/license")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void getOwnLicense_WithDonorRole_ReturnsForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/volunteer/license")
                        .header("Authorization", "Bearer " + donorToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void getOwnLicense_WithNgoRole_ReturnsForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/volunteer/license")
                        .header("Authorization", "Bearer " + ngoToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void getOwnLicense_Unauthenticated_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/volunteer/license"))
                .andExpect(status().isUnauthorized());
    }

    // ═══════════════════════════════════════════════════════════
    // VOLUNTEER PROFILE ENDPOINTS — role enforcement
    // ═══════════════════════════════════════════════════════════

    @Test
    void volunteerProfile_Create_WithAdminRole_ReturnsForbidden() throws Exception {
        mockMvc.perform(post("/api/v1/volunteer/profile/create")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void volunteerProfile_Get_WithDonorRole_ReturnsForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/volunteer/profile/get")
                        .header("Authorization", "Bearer " + donorToken))
                .andExpect(status().isForbidden());
    }

    // ═══════════════════════════════════════════════════════════
    // HELPERS
    // ═══════════════════════════════════════════════════════════

    private String tokenFor(String email, Role role) {
        User user = User.builder()
                .email(email)
                .role(role)
                .build();
        return jwtUtil.generateAccessToken(user);
    }

    private DrivingLicenseRequest buildLicenseRequest() {
        return DrivingLicenseRequest.builder()
                .licenseNumber("DL-12345")
                .issueDate(LocalDate.of(2022, 6, 1))
                .expiryDate(LocalDate.of(2032, 5, 31))
                .issuingState("Maharashtra")
                .documentUrl("https://example.com/license.pdf")
                .build();
    }

    private DrivingLicenseResponse buildLicenseResponse(Long id, String licenseNumber) {
        return DrivingLicenseResponse.builder()
                .id(id)
                .licenseNumber(licenseNumber)
                .issueDate(LocalDate.of(2022, 6, 1))
                .expiryDate(LocalDate.of(2032, 5, 31))
                .issuingState("Maharashtra")
                .documentUrl("https://example.com/license.pdf")
                .verificationStatus(LicenseVerificationStatus.PENDING)
                .build();
    }
}

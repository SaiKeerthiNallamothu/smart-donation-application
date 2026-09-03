package com.smartdonation.project.controllers;

import com.smartdonation.project.common.exception.ResourceNotFoundException;
import com.smartdonation.project.dto.response.AddressResponse;
import com.smartdonation.project.dto.response.DonorProfileResponse;
import com.smartdonation.project.dto.response.UserResponseDto;
import com.smartdonation.project.entities.User;
import com.smartdonation.project.enums.Gender;
import com.smartdonation.project.enums.Role;
import com.smartdonation.project.security.JwtUtil;
import com.smartdonation.project.service.DonorProfileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for ADMIN → Donor endpoints.
 *
 * <p>Uses {@code @SpringBootTest} so the full security filter chain is active.
 * Real JWT tokens are generated via {@link JwtUtil} and passed in the
 * {@code Authorization} header. This verifies both happy-path responses and
 * role-based access control end-to-end.
 */
@SpringBootTest
@AutoConfigureMockMvc
class AdminDonorEndpointTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @MockitoBean
    private DonorProfileService donorProfileService;

    private String adminToken;
    private String donorToken;
    private String volunteerToken;
    private String ngoToken;

    @BeforeEach
    void setUp() {
        adminToken = tokenFor("admin@example.com", Role.ADMIN);
        donorToken = tokenFor("donor@example.com", Role.DONOR);
        volunteerToken = tokenFor("volunteer@example.com", Role.VOLUNTEER);
        ngoToken = tokenFor("ngo@example.com", Role.NGO);
    }

    // ═══════════════════════════════════════════════════════════
    // GET /api/v1/admin/donors
    // ═══════════════════════════════════════════════════════════

    @Test
    void getAllDonors_WithAdminRole_ReturnsOk() throws Exception {
        DonorProfileResponse donor1 = buildDonorResponse(1L, "Alice", "Smith");
        DonorProfileResponse donor2 = buildDonorResponse(2L, "Bob", "Jones");

        when(donorProfileService.getAllDonorProfiles()).thenReturn(List.of(donor1, donor2));

        mockMvc.perform(get("/api/v1/admin/donors")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].user.firstName").value("Alice"))
                .andExpect(jsonPath("$[1].user.firstName").value("Bob"));
    }

    @Test
    void getAllDonors_EmptyList_ReturnsOk() throws Exception {
        when(donorProfileService.getAllDonorProfiles()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/admin/donors")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getAllDonors_WithDonorRole_ReturnsForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/admin/donors")
                        .header("Authorization", "Bearer " + donorToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAllDonors_WithVolunteerRole_ReturnsForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/admin/donors")
                        .header("Authorization", "Bearer " + volunteerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAllDonors_WithNgoRole_ReturnsForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/admin/donors")
                        .header("Authorization", "Bearer " + ngoToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAllDonors_Unauthenticated_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/admin/donors"))
                .andExpect(status().isUnauthorized());
    }

    // ═══════════════════════════════════════════════════════════
    // GET /api/v1/admin/donors/{id}
    // ═══════════════════════════════════════════════════════════

    @Test
    void getDonorById_WithAdminRole_ReturnsOk() throws Exception {
        DonorProfileResponse donor = buildDonorResponse(10L, "Charlie", "Brown");

        when(donorProfileService.getDonorProfileById(10L)).thenReturn(donor);

        mockMvc.perform(get("/api/v1/admin/donors/10")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.profileId").value(10))
                .andExpect(jsonPath("$.user.firstName").value("Charlie"))
                .andExpect(jsonPath("$.user.lastName").value("Brown"))
                .andExpect(jsonPath("$.user.email").value("charlie@example.com"));
    }

    @Test
    void getDonorById_NotFound_ReturnsNotFound() throws Exception {
        when(donorProfileService.getDonorProfileById(999L))
                .thenThrow(new ResourceNotFoundException("Donor profile not found with id: 999"));

        mockMvc.perform(get("/api/v1/admin/donors/999")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Donor profile not found with id: 999"));
    }

    @Test
    void getDonorById_WithDonorRole_ReturnsForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/admin/donors/1")
                        .header("Authorization", "Bearer " + donorToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void getDonorById_WithVolunteerRole_ReturnsForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/admin/donors/1")
                        .header("Authorization", "Bearer " + volunteerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void getDonorById_Unauthenticated_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/admin/donors/1"))
                .andExpect(status().isUnauthorized());
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

    private DonorProfileResponse buildDonorResponse(Long id, String firstName, String lastName) {
        AddressResponse address = AddressResponse.builder()
                .id(1L)
                .addressLine1("123 Main St")
                .city("Mumbai")
                .state("Maharashtra")
                .pincode("400001")
                .country("India")
                .build();

        UserResponseDto userResponse = UserResponseDto.builder()
                .id(id)
                .firstName(firstName)
                .lastName(lastName)
                .email(firstName.toLowerCase() + "@example.com")
                .phone("9876543210")
                .role(Role.DONOR)
                .build();

        return DonorProfileResponse.builder()
                .profileId(id)
                .dob(LocalDate.of(1995, 5, 15))
                .gender(Gender.MALE)
                .user(userResponse)
                .address(address)
                .build();
    }
}

package com.smartdonation.project.repository;

import com.smartdonation.project.entities.NgoProfile;
import com.smartdonation.project.enums.VerificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NgoProfileRepository extends JpaRepository<NgoProfile, Long> {

    Optional<NgoProfile> findByUserId(Long userId);

    boolean existsByUserId(Long userId);

    List<NgoProfile> findByVerificationStatus(VerificationStatus status);

    Optional<NgoProfile> findByIdAndVerificationStatus(Long id, VerificationStatus status);
}

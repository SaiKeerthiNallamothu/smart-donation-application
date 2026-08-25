package com.smartdonation.project.repository;

import com.smartdonation.project.entities.VolunteerProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VolunteerProfileRepository extends JpaRepository<VolunteerProfile, Long> {

    Optional<VolunteerProfile> findByUserId(Long userId);

    boolean existsByUserId(Long userId);
}

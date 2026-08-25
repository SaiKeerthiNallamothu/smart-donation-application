package com.smartdonation.project.repository;

import com.smartdonation.project.entities.DrivingLicense;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DrivingLicenseRepository extends JpaRepository<DrivingLicense, Long> {
}

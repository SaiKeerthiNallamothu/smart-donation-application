package com.smartdonation.project.enums;

/** Status tracking the full lifecycle of a donation. */
public enum DonationStatus {
    PENDING,
    ACCEPTED,
    REJECTED,
    PICKUP_SCHEDULED,
    PICKED_UP,
    DELIVERED,
    COMPLETED,
    CANCELLED
}

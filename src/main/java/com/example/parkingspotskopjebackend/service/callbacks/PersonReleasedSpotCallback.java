package com.example.parkingspotskopjebackend.service.callbacks;

public interface PersonReleasedSpotCallback {
    void onFirstPersonFound(String userId, String deviceToken);
    void onCancelled(String errorMessage);
}

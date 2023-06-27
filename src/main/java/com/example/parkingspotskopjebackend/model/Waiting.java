package com.example.parkingspotskopjebackend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class Waiting {
    @Id
    @GeneratedValue
    private String id;
    private String parkingId;
    private String userId;
    private String timestamp;
}

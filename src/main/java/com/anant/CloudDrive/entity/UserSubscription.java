package com.anant.CloudDrive.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class UserSubscription {

    @Id
    String email;

    String Tier;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTier() {
        return Tier;
    }
    public void setTier(String tier) {
        Tier = tier;
    }
}

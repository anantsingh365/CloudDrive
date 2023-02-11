package com.anant.CloudDrive.repository;

import com.anant.CloudDrive.entity.UserSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserSubscriptionRepo extends JpaRepository<UserSubscription, String>{
}

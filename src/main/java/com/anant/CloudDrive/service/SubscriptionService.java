package com.anant.CloudDrive.service;

import com.anant.CloudDrive.entity.UserSubscription;
import com.anant.CloudDrive.repository.UserSubscriptionRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SubscriptionService {

    @Autowired
    UserSubscriptionRepo userSubscriptionRepo;

    public String getTier(String userName){
        var userOptional = userSubscriptionRepo.findById(userName);
        if(userOptional.isPresent()){
            var userEntity =  userOptional.get();
            return userEntity.getTier();
        }
        return "Basic Tier";
    }

    public void setTier(String userName, String Tier){
        var userSubscription = new UserSubscription();
        userSubscription.setTier(Tier);
        userSubscription.setEmail(userName);
        var user = userSubscriptionRepo.save(userSubscription);
    }
}

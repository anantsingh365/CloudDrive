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

    public String setTier(String userName, String Tier){
        var userSubscription = new UserSubscription();
        userSubscription.setEmail(userName);
        userSubscription.setTier(Tier);
        var user = userSubscriptionRepo.save(userSubscription);
        return user.getTier();
    }
}

package com.anant.CloudDrive.controller;
import com.anant.CloudDrive.repository.UserSubscriptionRepo;
import com.anant.CloudDrive.service.SubscriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
public class Subscriptions {

    @Autowired
    SubscriptionService subscriptionService;

    @PostMapping("/subscriptions/buy")
    @ResponseBody
    public String subscribeToPremium(@RequestParam Map<String, String> body){
        subscriptionService.setTier("TestUserName", body.get("Tier"));
        String tier = subscriptionService.getTier("TestUserName");
        System.out.println("TestUserName has " + tier + " subscription");
        return tier +" purchased!!";
    }

    @GetMapping("/subscriptions")
    public String SubscriptionPage(){
        return "SubscribePage";
    }

}

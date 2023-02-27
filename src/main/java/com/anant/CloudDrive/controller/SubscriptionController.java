package com.anant.CloudDrive.controller;
import com.anant.CloudDrive.Utils.CommonUtils;
import com.anant.CloudDrive.repository.UserSubscriptionRepo;
import com.anant.CloudDrive.service.SubscriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
public class SubscriptionController {

    @Autowired
    SubscriptionService subscriptionService;

    @PostMapping("/subscriptions/buy")
    @ResponseBody
    public String subscribeToPremium(@RequestParam Map<String, String> body){
        String currentUser = CommonUtils.getUserData(CommonUtils.signedInUser.GET_USERNAME);
        var tierPurchased = subscriptionService.setTier(currentUser, body.get("Tier"));
        System.out.println(currentUser+ tierPurchased + " subscription");
        return tierPurchased +" purchased!!";
    }

    @GetMapping("/subscriptions")
    public String SubscriptionPage(){
        return "SubscribePage";
    }

}

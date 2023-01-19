package com.anant.CloudDrive.controller;

import com.anant.CloudDrive.GetApplicationContext;
import com.anant.CloudDrive.UserUploads.UserUploadSession;
import com.anant.CloudDrive.UserUploads.UserUploadSessions;
import com.anant.CloudDrive.dto.UserDto;
import com.anant.CloudDrive.entity.User;
import com.anant.CloudDrive.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.util.Map;

@Controller
@SessionAttributes("SessionClass")
public class Home {

    @Autowired
    private UserUploadSessions userUploadSessions;

    @Autowired
    private UserService userService;

    @Autowired
    private UserUploadSession userUploadEntries;

    @GetMapping("/UserHome")
    public String UserHome(HttpSession session){
        System.out.println(session.getId());
        return "UserHome";
    }

    @GetMapping("/register")
    public String registerPage(Model model){
        UserDto user = new UserDto();
        model.addAttribute("user", user);
        return "register";
    }

    @GetMapping("/login")
    public String loginForm(Model model) {
        model.addAttribute("SessionClass",new SessionClass());
        return "login";
    }

    @PostMapping("/register/save")
    public String registration(@Valid @ModelAttribute("user") UserDto user,
                               BindingResult result,
                               Model model){
        User existing = userService.findByEmail(user.getEmail());
        if (existing != null) {
            result.rejectValue("email", null, "There is already an account registered with that email");
        }
        if (result.hasErrors()) {
            model.addAttribute("user", user);
            return "register";
        }
        userService.saveUser(user);
        return "redirect:/register?success";
    }

    @GetMapping("/uploadId")
    @ResponseBody
    public String uploadId(@RequestHeader Map<String,String> headers){
        String keyName = headers.get("filenmae");
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        return  userUploadSessions.getUploadId(userName, keyName);
    }

    @PostMapping("/uploadFile")
    @ResponseBody
    public synchronized ResponseEntity<String> bytePrint(InputStream is, HttpServletRequest req, @RequestHeader Map<String, String> headers) throws IOException, InterruptedException {

        System.out.println("receiving data for userID "+ SecurityContextHolder.getContext().getAuthentication().getName());

        headers.forEach( (key,value) -> System.out.println(key + ": "+value));

        String userId = headers.get("user-id");

        if(userId == null){
            System.out.println("Sending Bad Request");
            return  ResponseEntity.badRequest().body("User Id Missing");
        }


//        UserUpload userUpload = userUploadEntries.getUserUploadEntry(SecurityContextHolder.getContext().getAuthentication().getName());
//        if(userUpload == null){
//
//        }

        File file = new File("file");
        OutputStream os = new FileOutputStream(file, true);

        System.out.println(is.transferTo(os) + " bytes appended to the file");

        is.close();
        os.close();

        System.out.println("################### A PART HAS BEEN WRITTEN ###############");
        return ResponseEntity.ok().body("dataReceived");
    }
}

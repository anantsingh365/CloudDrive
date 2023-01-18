package com.anant.CloudDrive.controller;

import com.anant.CloudDrive.dto.UserDto;
import com.anant.CloudDrive.entity.User;
import com.anant.CloudDrive.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.apache.catalina.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.net.http.HttpRequest;

@Controller
@SessionAttributes("SessionClass")
public class Home {

    @Autowired
    private UserService userService;

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

    @PostMapping("/testingByteData")
    @ResponseBody
    public synchronized String  bytePrint(InputStream is) throws IOException, InterruptedException {

        System.out.println("receiving data for userID "+ SecurityContextHolder.getContext().getAuthentication().getName());
        File file = new File("file");
        OutputStream os = new FileOutputStream(file, true);

        System.out.println(is.transferTo(os) + " bytes appended to the file");

        is.close();
        os.close();

        System.out.println("################### A PART HAS BEEN WRITTEN ###############");
        return "dataReceived";


    }

}

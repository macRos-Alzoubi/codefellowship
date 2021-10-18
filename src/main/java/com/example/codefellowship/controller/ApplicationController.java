package com.example.codefellowship.controller;

import com.example.codefellowship.model.ApplicationUser;
import com.example.codefellowship.model.DTO.AppUserDTO;
import com.example.codefellowship.repository.ApplicationUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.view.RedirectView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

@Controller
public class ApplicationController {

    @Autowired
    public ApplicationUserRepository applicationUserRepository;

    @Autowired
    public BCryptPasswordEncoder passwordEncoder;

    @GetMapping("/signup")
    public String getSignUpPage() {
        System.out.println("in signup route");
        return "signup";
    }

    @GetMapping("/login")
    public String getLoginPage() {
        return "login";
    }

    @GetMapping("/profile")
    public String getProfile(Model model) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        ApplicationUser applicationUser = applicationUserRepository.findApplicationUserByUsername(userDetails.getUsername()).orElse(null);

        if (applicationUser == null)
            return "error";

        model.addAttribute("user", applicationUser);
        return "profile";
    }

    @GetMapping("/users/{id}")
    public String getUserProfile(Model model, @PathVariable String id) {
        ApplicationUser applicationUser = applicationUserRepository.findApplicationUserById(Long.parseLong(id)).orElse(null);

        if (applicationUser == null)
            throw new UsernameNotFoundException("There is no user with " + id + "Id number!");
        model.addAttribute("user", applicationUser);
        return "profile";
    }


    @PostMapping("/signup")
    public RedirectView signupUser(@ModelAttribute AppUserDTO appUserDTO) {

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

        Date date = null;
        try {
            date = formatter.parse(appUserDTO.getDateOfBirth());
            date = new Date(date.getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }

        ApplicationUser newApplicationUser = new ApplicationUser(appUserDTO.getUsername(),
                passwordEncoder.encode(appUserDTO.getPassword()),
                appUserDTO.getFirstName(), appUserDTO.getLastName(),
                appUserDTO.getBio(), date);

        newApplicationUser = applicationUserRepository.save(newApplicationUser);
        Authentication authentication = new UsernamePasswordAuthenticationToken(newApplicationUser, null, new ArrayList<>());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        return new RedirectView("profile");
    }
}

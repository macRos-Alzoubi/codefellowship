package com.example.codefellowship.controller;

import com.example.codefellowship.model.ApplicationUser;
import com.example.codefellowship.model.Comment;
import com.example.codefellowship.model.DTO.AppUserDTO;
import com.example.codefellowship.model.Post;
import com.example.codefellowship.repository.ApplicationUserRepository;
import com.example.codefellowship.repository.CommentRepository;
import com.example.codefellowship.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.security.Principal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Controller
public class ApplicationController {

    @Autowired
    public ApplicationUserRepository applicationUserRepository;

    @Autowired
    public PostRepository postRepository;

    @Autowired
    public CommentRepository commentRepository;

    @Autowired
    public BCryptPasswordEncoder passwordEncoder;

    @GetMapping("/signup")
    public String getSignUpPage() {
        return "signup";
    }

    @GetMapping("/login")
    public String getLoginPage() {
        return "login";
    }

    @GetMapping("/error")
    public String getErrorPage() {
        return "error";
    }

    @GetMapping("/profile")
    public String getProfile(Model model) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        ApplicationUser applicationUser = applicationUserRepository.findApplicationUserByUsername(userDetails.getUsername()).orElse(null);

        if (applicationUser == null)
            return "error";

        model.addAttribute("user", applicationUser);
        model.addAttribute("loggedInUser", applicationUser);
        return "profile";
    }

    @GetMapping("/users/{id}")
    public String getUserProfile(Model model, @PathVariable String id, Principal principal) {
        ApplicationUser loggedInUser = applicationUserRepository.findApplicationUserByUsername(principal.getName()).orElse(null);
        ApplicationUser applicationUser = applicationUserRepository.findApplicationUserById(Long.parseLong(id)).orElse(null);

        if (applicationUser == null)
            throw new UsernameNotFoundException("There is no user with " + id + "Id number!");
        else if (loggedInUser == null)
            return "error";
        model.addAttribute("loggedInUser", loggedInUser);
        model.addAttribute("user", applicationUser);
        return "profile";
    }

    @GetMapping("/users")
    public String getAllUsers(Model model, Principal principal) {
        ApplicationUser loggedInUser = applicationUserRepository.findApplicationUserByUsername(principal.getName()).orElse(null);
        List<ApplicationUser> applicationUsers = applicationUserRepository.findAll();

        if (loggedInUser == null)
            return "error";

        model.addAttribute("loggedInUser", loggedInUser);
        model.addAttribute("users", applicationUsers);

        return "users";
    }

    @GetMapping("/feed")
    public String getAllPosts(Model model, Principal p) {
        //UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        ApplicationUser applicationUser = applicationUserRepository.findApplicationUserByUsername(p.getName()).orElse(null);

        if (applicationUser == null)
            return "error";

        model.addAttribute("users", applicationUser.getFollowing());
        model.addAttribute("loggedInUser", applicationUser);
        return "feed";
    }

    @PostMapping("/posts")
    public RedirectView addNewPost(@RequestParam String postBody) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        ApplicationUser applicationUser = applicationUserRepository.findApplicationUserByUsername(userDetails.getUsername()).orElse(null);

        if (applicationUser == null) {
            return new RedirectView("error");
        }


        Post newPost = new Post(postBody, applicationUser);
        postRepository.save(newPost);
        return new RedirectView("profile");
    }

    @PostMapping("comments/{postId}")
    public RedirectView addNewComment(@RequestParam String commentContent, @PathVariable long postId) {

        Post post = postRepository.findById(postId).orElse(null);

        if (post == null)
            return new RedirectView("/error");

        Comment comment = new Comment(commentContent, post);
        commentRepository.save(comment);
        return new RedirectView("/feed");
    }

    @PostMapping("/users/follow/{profileOwnerId}/{loggedInUserId}")
    public RedirectView addFollowingNadFollower(@PathVariable long profileOwnerId, @PathVariable long loggedInUserId) {
        System.out.println("***************** In Follow **********************");
        ApplicationUser profileOwnerUser = applicationUserRepository.findApplicationUserById(profileOwnerId).orElse(null);
        ApplicationUser loggedInUser = applicationUserRepository.findApplicationUserById(loggedInUserId).orElse(null);

        System.out.println(profileOwnerId + " -ids " + loggedInUserId);
        System.out.println(profileOwnerUser + " -users " + loggedInUser);
        if (profileOwnerUser == null || loggedInUser == null)
            return new RedirectView("/error");

        profileOwnerUser.getFollowers().add(loggedInUser);
        loggedInUser.getFollowing().add(profileOwnerUser);

        applicationUserRepository.save(profileOwnerUser);
        applicationUserRepository.save(loggedInUser);

        return new RedirectView("/users/" + profileOwnerId);
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

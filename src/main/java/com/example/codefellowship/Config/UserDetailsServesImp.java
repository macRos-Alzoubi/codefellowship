package com.example.codefellowship.Config;

import com.example.codefellowship.model.ApplicationUser;
import com.example.codefellowship.repository.ApplicationUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class UserDetailsServesImp implements UserDetailsService {

    @Autowired
    ApplicationUserRepository applicationUserRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        ApplicationUser applicationUser = applicationUserRepository.findApplicationUserByUsername(username).orElse(null);

        if (applicationUser == null)
            throw new UsernameNotFoundException((username + " not found"));

        Authentication authentication = new UsernamePasswordAuthenticationToken(applicationUser, null, new ArrayList<>());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        return applicationUser;
    }
}

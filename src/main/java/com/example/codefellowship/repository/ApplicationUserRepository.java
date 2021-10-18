package com.example.codefellowship.repository;

import com.example.codefellowship.model.ApplicationUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ApplicationUserRepository extends JpaRepository<ApplicationUser, Long> {
    Optional<ApplicationUser> findApplicationUserByUsername(String username);
    Optional<ApplicationUser> findApplicationUserById(Long id);
}

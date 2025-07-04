package com.mit.project.repositories;

import com.mit.project.entities.UserDetails;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserDetailsRepository extends JpaRepository<UserDetails, String> {
  UserDetails findByEmailId(String emailId);
}

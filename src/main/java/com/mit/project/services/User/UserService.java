package com.mit.project.services;

import com.mit.project.entities.UserDetails;
import com.mit.project.repositories.UserDetailsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserDetailsRepository userDetailsRepository;

  public List<UserDetails> findAllUsers() {
    return userDetailsRepository.findAll();
  }

  public Optional<UserDetails> findUserByEmail(String emailId) {
    return userDetailsRepository.findById(emailId);
  }

  public UserDetails saveUser(UserDetails userDetails) {
    return userDetailsRepository.save(userDetails);
  }

  public void deleteUserByEmail(String emailId) {
    userDetailsRepository.deleteById(emailId);
  }

  public UserDetails updateUser(UserDetails userDetails) {
    return userDetailsRepository.save(userDetails);
  }
}

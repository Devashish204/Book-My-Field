package com.mit.project.entities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.Assert;

@AllArgsConstructor
@Getter
@Builder
public class BMFUser implements UserDetails {

  private String username;
  private String password;
  private String firstName;
  private String lastName;
  private List<String> userRoles;

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    List<GrantedAuthority> authorities = new ArrayList(userRoles.size());

    for (String role : userRoles) {
      Assert.isTrue(
          !role.startsWith("ROLE_"),
          () -> role + " cannot start with ROLE_ (it is automatically added)");
      authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
    }

    return authorities;
  }

  @Override
  public String getPassword() {
    return password;
  }

  @Override
  public String getUsername() {
    return username;
  }

  @Override
  public boolean isAccountNonLocked() {
    return false;
  }
}

package com.cms.security;

import com.cms.model.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@AllArgsConstructor
public class UserPrincipal implements UserDetails {
    @Getter
    private final String id;
    private final String email;
    @Getter
    private final String name;
    private final String password;
    private final Collection<? extends GrantedAuthority> authorities;

//    public UserPrincipal(String id, String email, String name, String password,
//                        Collection<? extends GrantedAuthority> authorities) {
//        this.id = id;
//        this.email = email;
//        this.name = name;
//        this.password = password;
//        this.authorities = authorities;
//    }
    
    public static UserPrincipal create(User user) {
        Collection<GrantedAuthority> authorities = Collections.singletonList(
            new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
        );
        
        return new UserPrincipal(
            user.getId(),
            user.getEmail(),
            user.getName(),
            user.getPassword(),
            authorities
        );
    }

    @Override
    public String getUsername() {
        return email;
    }
    
    @Override
    public String getPassword() {
        return password;
    }
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }
    
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }
    
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    
    @Override
    public boolean isEnabled() {
        return true;
    }
}
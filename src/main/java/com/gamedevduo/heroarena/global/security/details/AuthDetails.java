package com.gamedevduo.heroarena.global.security.details;

import com.gamedevduo.heroarena.global.security.jwt.dto.UserCredential;
import lombok.AllArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;


@AllArgsConstructor
public class AuthDetails implements UserDetails {
    private final UserCredential credential;
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList();
    }
    @Override
    public String getPassword() {
        return credential.encodedPassword();
    }

    @Override
    public String getUsername() {
        return credential.email();
    }
}

package com.itjima_server.security;

import java.util.Collection;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;

@Getter
public class CustomUserDetails extends User {

    private final long id;
    private final String email;

    public CustomUserDetails(long id, String email, String password,
            Collection<? extends GrantedAuthority> authorities) {
        super(email, password, authorities);
        this.id = id;
        this.email = email;
    }
}

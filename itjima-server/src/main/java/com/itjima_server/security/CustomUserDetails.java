package com.itjima_server.security;

import java.util.Collection;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

public class CustomUserDetails extends User {

    private final long id;

    public CustomUserDetails(long id, String email, String password,
            Collection<? extends GrantedAuthority> authorities) {
        super(email, password, authorities);
        this.id = id;
    }

    public long getId() {
        return id;
    }
}

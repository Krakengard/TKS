package org.example.rental.security;

import org.example.rental.model.User;
import org.example.rental.service.UserManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    @Lazy
    private UserManager userManager;

    @Autowired
    private JwtService jwtService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("=== CustomUserDetailsService.loadUserByUsername ===");
        System.out.println("Username: " + username);

        Optional<User> userOptional = userManager.getUserByLoginExact(username);

        if (userOptional.isEmpty()) {
            System.out.println("User NOT found: " + username);
            throw new UsernameNotFoundException("User not found with login: " + username);
        }

        User user = userOptional.get();
        System.out.println("User found: " + user.getLogin());
        System.out.println("User password present: " + (user.getPassword() != null));
        System.out.println("User active: " + user.isActive());

        if (!user.isActive()) {
            System.out.println("User account is deactivated");
            throw new UsernameNotFoundException("User account is deactivated");
        }

        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();
        System.out.println("User authorities from User.getAuthorities(): " + authorities);

        for (GrantedAuthority authority : authorities) {
            System.out.println("Authority: " + authority.getAuthority());
        }

        if (user.getPassword() == null) {
            System.out.println("ERROR: User password is NULL!");
            throw new UsernameNotFoundException("User password is null");
        }

        return new org.springframework.security.core.userdetails.User(
                user.getLogin(),
                user.getPassword(),
                authorities
        );
    }
}
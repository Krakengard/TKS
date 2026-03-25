package org.example.rental.security;

/*
import org.example.rental.domain.model.User;
import org.example.rental.port.in.UserUseCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
*/

import org.example.rental.domain.model.Administrator;
import org.example.rental.domain.model.Customer;
import org.example.rental.domain.model.ResourceManager;
import org.example.rental.domain.model.User;
import org.example.rental.port.in.UserUseCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    @Lazy
    private UserUseCase userUseCase;

  /*  @Autowired
    private JwtService jwtService;*/

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("=== CustomUserDetailsService.loadUserByUsername ===");
        System.out.println("Username: " + username);

        Optional<User> userOptional = userUseCase.getUserByLogin(username);

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

       /* Collection<? extends GrantedAuthority> authorities = user.getAuthorities();
        System.out.println("User authorities from User.getAuthorities(): " + authorities);

        for (GrantedAuthority authority : authorities) {
            System.out.println("Authority: " + authority.getAuthority());
        }
*/
        if (user.getPassword() == null) {
            System.out.println("ERROR: User password is NULL!");
            throw new UsernameNotFoundException("User password is null");
        }

       /* return new org.springframework.security.core.userdetails.User(
                user.getLogin(),
                user.getPassword(),
                authorities
        );*/

        List<GrantedAuthority> authorities = new ArrayList<>();

        if (user instanceof Administrator) {
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        } else if (user instanceof ResourceManager) {
            authorities.add(new SimpleGrantedAuthority("ROLE_MANAGER"));
        } else if (user instanceof Customer) {
            authorities.add(new SimpleGrantedAuthority("ROLE_CUSTOMER"));
        } else {
            authorities.add(new SimpleGrantedAuthority("ROLE_USER")); // domyślna
        }

        for (GrantedAuthority authority : authorities) {
            System.out.println("Assigned Authority: " + authority.getAuthority());
        }

        // Zwracamy obiekt UserDetails ze Spring Security
        return new org.springframework.security.core.userdetails.User(
                user.getLogin(),
                user.getPassword(),
                authorities
        );
    }
}
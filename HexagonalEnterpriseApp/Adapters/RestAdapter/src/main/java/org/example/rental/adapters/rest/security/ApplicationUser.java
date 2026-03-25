package org.example.rental.security;


import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.List;
import org.example.rental.domain.model.Administrator;
import org.example.rental.domain.model.Customer;
import org.example.rental.domain.model.ResourceManager;
import org.example.rental.domain.model.User;

public class ApplicationUser implements UserDetails {

    private final User user;

    public ApplicationUser(User user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (user instanceof Administrator) return List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));
        if (user instanceof ResourceManager) return List.of(new SimpleGrantedAuthority("ROLE_MANAGER"));
        if (user instanceof Customer) return List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER"));
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getPassword() {
        return user.getPassword(); // сюда можно хранить пароль из отдельного поля
    }

    @Override
    public String getUsername() {
        return user.getLogin();
    }

    @Override
    public boolean isAccountNonExpired() { return true; }
    @Override
    public boolean isAccountNonLocked() { return true; }
    @Override
    public boolean isCredentialsNonExpired() { return true; }
    @Override
    public boolean isEnabled() { return user.isActive(); }
}

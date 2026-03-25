package org.example.rental.adapters.rest.controller;

import org.example.rental.domain.exception.ResourceNotFoundException;
import org.example.rental.domain.exception.UnauthorizedException;
import org.example.rental.domain.model.User;
import org.example.rental.domain.model.Administrator;
import org.example.rental.domain.model.ResourceManager;
import org.example.rental.domain.model.Customer;
import org.example.rental.port.in.UserUseCase;
import org.example.rental.security.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserUseCase userUseCase;

    @Autowired
    private JwtService jwtService;

    /*@PostMapping("/login")
    public Map<String, Object> login(@RequestBody LoginRequest loginRequest) {
        User user = userUseCase.getUserByLogin(loginRequest.getLogin())
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

        if (!user.isActive()) {
            throw new UnauthorizedException("Account is deactivated. Contact administrator.");
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getLogin(), loginRequest.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        User authenticatedUser = userUseCase.getUserByLogin(loginRequest.getLogin())
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

        String jwt = jwtService.generateToken(authenticatedUser);

        Map<String, Object> response = new HashMap<>();
        response.put("token", jwt);
        response.put("user", createUserResponse(authenticatedUser));
        return response;
    }*/

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody LoginRequest loginRequest) {

        // 1. Sprawdzenie, czy użytkownik istnieje w bazie i czy jest aktywny
        User user = userUseCase.getUserByLogin(loginRequest.getLogin())
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

        if (!user.isActive()) {
            throw new UnauthorizedException("Account is deactivated. Contact administrator.");
        }

        // 2. Uwierzytelnienie w Spring Security (to pod spodem wywoła CustomUserDetailsService)
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getLogin(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 3. Pobranie zautoryzowanego obiektu UserDetails ze Springa (NAPRAWA BŁĘDU)
        org.springframework.security.core.userdetails.UserDetails springUserDetails =
                (org.springframework.security.core.userdetails.UserDetails) authentication.getPrincipal();

        // 4. Generowanie tokenu na podstawie UserDetails
        String jwt = jwtService.generateToken(springUserDetails);

        // 5. Zwrócenie odpowiedzi z tokenem i zmapowanym modelem domenowym
        Map<String, Object> response = new HashMap<>();
        response.put("token", jwt);
        response.put("user", createUserResponse(user)); // Używamy domenowego usera do budowy odpowiedzi
        return response;
    }

    @PostMapping("/logout")
    public String logout() {
        SecurityContextHolder.clearContext();
        return "Logged out successfully";
    }

    @PostMapping("/change-password")
    public ResponseEntity<String> changePassword(@RequestBody ChangePasswordRequest request,
                                                  Authentication authentication) {
        String username = authentication.getName();
        User user = userUseCase.getUserByLogin(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        userUseCase.changePassword(user.getId(), request.getCurrentPassword(), request.getNewPassword());
        return ResponseEntity.ok("Hasło zmienione pomyślnie");
    }

    @GetMapping("/me")
    public Map<String, Object> getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userUseCase.getUserByLogin(username)
                .orElseThrow(() -> new UnauthorizedException("User not found"));
        return createUserResponse(user);
    }

    private Map<String, Object> createUserResponse(User user) {
        Map<String, Object> r = new HashMap<>();
        r.put("id", user.getId());
        r.put("login", user.getLogin());
        r.put("name", user.getName());
        r.put("email", user.getEmail());
        r.put("active", user.isActive());

        if (user instanceof Administrator a) {
            r.put("type", "administrator");
            r.put("department", a.getDepartment());
        } else if (user instanceof ResourceManager m) {
            r.put("type", "resourceManager");
            r.put("managedResourceType", m.getManagedResourceType());
        } else if (user instanceof Customer c) {
            r.put("type", "customer");
            r.put("phoneNumber", c.getPhoneNumber());
            r.put("address", c.getAddress());
        }
        return r;
    }

    public static class LoginRequest {
        private String login;
        private String password;
        public String getLogin() { return login; }
        public void setLogin(String login) { this.login = login; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class ChangePasswordRequest {
        private String currentPassword;
        private String newPassword;
        public String getCurrentPassword() { return currentPassword; }
        public void setCurrentPassword(String p) { this.currentPassword = p; }
        public String getNewPassword() { return newPassword; }
        public void setNewPassword(String p) { this.newPassword = p; }
    }
}

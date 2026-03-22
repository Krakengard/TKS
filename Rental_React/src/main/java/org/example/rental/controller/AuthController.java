package org.example.rental.controller;

import org.example.rental.exception.BadRequestException;
import org.example.rental.exception.ResourceNotFoundException;
import org.example.rental.exception.UnauthorizedException;
import org.example.rental.model.User;
import org.example.rental.security.JwtService;
import org.example.rental.service.UserManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserManager userManager;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody LoginRequest loginRequest) {
        Optional<User> userOptional = userManager.getUserByLoginExact(loginRequest.getLogin());
        if (userOptional.isEmpty()) {
            throw new UnauthorizedException("Invalid credentials");
        }

        User user = userOptional.get();
        if (!user.isActive()) {
            throw new UnauthorizedException("Account is deactivated. Contact administrator.");
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getLogin(),
                        loginRequest.getPassword()
                )
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        Optional<User> authenticatedOptional = userManager.getUserByLoginExact(loginRequest.getLogin());
        if (authenticatedOptional.isEmpty()) {
            throw new UnauthorizedException("Invalid credentials");
        }

        User authenticatedUser = authenticatedOptional.get();
        if (!authenticatedUser.isActive()) {
            throw new UnauthorizedException("Account is deactivated");
        }

        String jwt = jwtService.generateToken(authenticatedUser);

        Map<String, Object> response = new HashMap<>();
        response.put("token", jwt);
        response.put("user", createUserResponse(authenticatedUser));
        return response;
    }

    @PostMapping("/logout")
    public String logout() {
        SecurityContextHolder.clearContext();
        return "Logged out successfully";
    }

    @PostMapping("/change-password")
    public ResponseEntity<String> changePassword(
            @RequestBody ChangePasswordRequest request,
            Authentication authentication) {

        String username = authentication.getName();
        User user = userManager.getUserByLoginExact(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        userManager.changePassword(user.getId(), request.getCurrentPassword(), request.getNewPassword());

        return ResponseEntity.ok("Hasło zmienione pomyślnie");
    }


    @GetMapping("/me")
    public Map<String, Object> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        User user = userManager.getUserByLoginExact(username)
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        return createUserResponse(user);
    }

    @GetMapping("/debug/{login}")
    public Map<String, Object> debugUser(@PathVariable String login) {
        User user = userManager.getUserByLoginExact(login)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with login: " + login));

        Map<String, Object> debugInfo = new HashMap<>();
        debugInfo.put("login", user.getLogin());
        debugInfo.put("passwordHash", user.getPassword() != null ? "[HASHED]" : "NULL");
        debugInfo.put("passwordLength", user.getPassword() != null ? user.getPassword().length() : 0);
        debugInfo.put("active", user.isActive());
        debugInfo.put("type", user.getClass().getSimpleName());

        String[] testPasswords = {"admin123", "manager123", "password123", "wrong"};
        Map<String, Boolean> passwordTests = new HashMap<>();
        for (String testPassword : testPasswords) {
            passwordTests.put(testPassword, user.getPassword() != null &&
                    passwordEncoder.matches(testPassword, user.getPassword()));
        }
        debugInfo.put("passwordTests", passwordTests);
        return debugInfo;
    }

    @PostMapping("/fix-passwords")
    public String fixUserPasswords() {
        var allUsers = userManager.getAllUsersWithPasswords();
        int fixedCount = 0;

        for (User user : allUsers) {
            if (user.getPassword() == null) {
                String defaultPassword;
                if (user.getLogin().equals("admin")) defaultPassword = "admin123";
                else if (user.getLogin().equals("manager")) defaultPassword = "manager123";
                else defaultPassword = "password123";

                user.setPassword(passwordEncoder.encode(defaultPassword));
                userManager.updateUser(user.getId(), user);
                fixedCount++;
            }
        }
        return "Fixed " + fixedCount + " user passwords";
    }



    private Map<String, Object> createUserResponse(User user) {
        Map<String, Object> userResponse = new HashMap<>();
        userResponse.put("id", user.getId());
        userResponse.put("login", user.getLogin());
        userResponse.put("name", user.getName());
        userResponse.put("email", user.getEmail());
        userResponse.put("active", user.isActive());

        if (user instanceof org.example.rental.model.Administrator) {
            userResponse.put("type", "administrator");
            userResponse.put("department", ((org.example.rental.model.Administrator) user).getDepartment());
        } else if (user instanceof org.example.rental.model.ResourceManager) {
            userResponse.put("type", "resourceManager");
            userResponse.put("managedResourceType", ((org.example.rental.model.ResourceManager) user).getManagedResourceType());
        } else if (user instanceof org.example.rental.model.Customer) {
            userResponse.put("type", "customer");
            userResponse.put("phoneNumber", ((org.example.rental.model.Customer) user).getPhoneNumber());
            userResponse.put("address", ((org.example.rental.model.Customer) user).getAddress());
        }
        return userResponse;
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
        public void setCurrentPassword(String currentPassword) { this.currentPassword = currentPassword; }
        public String getNewPassword() { return newPassword; }
        public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    }
}

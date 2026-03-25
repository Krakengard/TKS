package org.example.rental.adapters.rest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.rental.domain.exception.*;
import org.example.rental.domain.model.*;
import org.example.rental.port.in.UserUseCase;
import org.example.rental.security.JwsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserUseCase userUseCase;

    @Autowired
    private JwsService jwsService;

    @Autowired
    private ObjectMapper objectMapper;

    @GetMapping("/{id}/verification-token")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER') or (#id == authentication.principal.userId)")
    public Map<String, String> getVerificationToken(@PathVariable UUID id) {
        userUseCase.getUserById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        Map<String, String> response = new HashMap<>();
        response.put("verificationToken", jwsService.createVerificationToken(id.toString()));
        response.put("userId", id.toString());
        return response;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody String rawJson) {
        try {
            User user = objectMapper.readValue(rawJson, User.class);

            if (userUseCase.getUserByLogin(user.getLogin()).isPresent()) {
                throw new ConflictException("User with login '" + user.getLogin() + "' already exists");
            }

            User created = userUseCase.createUser(user);
            Map<String, Object> response = Map.of(
                    "user", createSafeUserCopy(created),
                    "message", "User registered successfully");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            throw new BadRequestException(e.getMessage());
        } catch (ConflictException e) {
            throw e;
        } catch (Exception e) {
            throw new ConflictException("Registration failed: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER') or (#id == authentication.principal.userId)")
    public Map<String, Object> getUserById(
            @PathVariable UUID id,
            @RequestHeader(value = "X-Object-Signature", required = false) String signature,
            @RequestHeader(value = "X-Verification-Token", required = false) String verificationToken) {

        if (verificationToken != null && !jwsService.verifyUserIdToken(verificationToken, id.toString())) {
            throw new UnauthorizedException("Invalid user verification token");
        }

        if (signature != null) {
            Map<String, Object> expectedData = new HashMap<>();
            expectedData.put("id", id.toString());
            if (!jwsService.verifySignature(signature, expectedData)) {
                throw new BadRequestException("Invalid object signature");
            }
        }

        User user = userUseCase.getUserById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        User safeUser = createSafeUserCopy(user);

        Map<String, Object> signatureData = buildSignatureData(safeUser);
        Map<String, Object> response = new HashMap<>();
        response.put("user", safeUser);
        response.put("signature", jwsService.createSignature(signatureData));
        response.put("verificationToken", jwsService.createVerificationToken(id.toString()));
        return response;
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER') or (#id == authentication.principal.userId)")
    public Map<String, Object> updateUser(
            @PathVariable UUID id,
            @RequestBody User userDetails,
            @RequestHeader("X-Object-Signature") String signature,
            @RequestHeader("X-Verification-Token") String verificationToken) {

        if (!jwsService.verifyUserIdToken(verificationToken, id.toString())) {
            throw new UnauthorizedException("Invalid user verification token");
        }

        Map<String, Object> expectedData = new HashMap<>();
        expectedData.put("id", id.toString());
        expectedData.put("login", userDetails.getLogin());
        if (!jwsService.verifySignature(signature, expectedData)) {
            throw new BadRequestException("Invalid object signature");
        }

        User updated = userUseCase.updateUser(id, userDetails);
        User safeUser = createSafeUserCopy(updated);

        Map<String, Object> response = new HashMap<>();
        response.put("user", safeUser);
        response.put("signature", jwsService.createSignature(buildSignatureData(safeUser)));
        response.put("verificationToken", jwsService.createVerificationToken(id.toString()));
        return response;
    }

    @GetMapping("/customers")
    public List<User> getAllCustomers() {
        return userUseCase.getAllUsers().stream()
                .filter(u -> u instanceof Customer)
                .map(this::createSafeUserCopy)
                .toList();
    }

    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public List<User> getAllUsersForAdmin() {
        return userUseCase.getAllUsers().stream()
                .map(this::createSafeUserCopy)
                .toList();
    }

    @PostMapping("/{id}/activate")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public void activateUser(@PathVariable UUID id) {
        try {
            userUseCase.activateUser(id);
        } catch (IllegalArgumentException e) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }
    }

    @PostMapping("/{id}/deactivate")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public void deactivateUser(@PathVariable UUID id) {
        try {
            userUseCase.deactivateUser(id);
        } catch (IllegalArgumentException e) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }
    }

    @GetMapping("/search/exact")
    public User getUserByLoginExact(@RequestParam String login) {
        return userUseCase.getUserByLogin(login)
                .map(this::createSafeUserCopy)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with login: " + login));
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public List<User> getUsersByLoginContaining(@RequestParam String loginPart) {
        return userUseCase.getUsersByLoginContaining(loginPart).stream()
                .map(this::createSafeUserCopy)
                .toList();
    }

    private User createSafeUserCopy(User user) {
        User safeCopy;
        if (user instanceof Customer c) {
            Customer copy = new Customer(c.getLogin(), c.getName(), c.getEmail(),
                    c.getPhoneNumber(), c.getAddress());
            copy.setId(c.getId());
            copy.setActive(c.isActive());
            safeCopy = copy;
        } else if (user instanceof Administrator a) {
            Administrator copy = new Administrator(a.getLogin(), a.getName(),
                    a.getEmail(), a.getDepartment());
            copy.setId(a.getId());
            copy.setActive(a.isActive());
            safeCopy = copy;
        } else if (user instanceof ResourceManager m) {
            ResourceManager copy = new ResourceManager(m.getLogin(), m.getName(),
                    m.getEmail(), m.getManagedResourceType());
            copy.setId(m.getId());
            copy.setActive(m.isActive());
            safeCopy = copy;
        } else {
            throw new IllegalArgumentException("Unknown user type: " + user.getClass().getSimpleName());
        }
        return safeCopy;
    }

    private Map<String, Object> buildSignatureData(User user) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", user.getId().toString());
        data.put("login", user.getLogin());
        data.put("type", getUserType(user));
        return data;
    }

    private String getUserType(User user) {
        if (user instanceof Administrator) return "administrator";
        if (user instanceof ResourceManager) return "resourceManager";
        if (user instanceof Customer) return "customer";
        return "user";
    }
}

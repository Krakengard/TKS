package org.example.rental.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.rental.exception.*;
import org.example.rental.model.*;
import org.example.rental.security.JwsService;
import org.example.rental.service.UserManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserManager userManager;

    @Autowired
    private JwsService jwsService;

    @Autowired
    private ObjectMapper objectMapper;

    @GetMapping("/{id}/verification-token")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER') or (#id == authentication.principal.userId)")
    public Map<String, String> getVerificationToken(@PathVariable UUID id) {
        User user = userManager.getUserById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        String verificationToken = jwsService.createVerificationToken(id.toString());

        Map<String, String> response = new HashMap<>();
        response.put("verificationToken", verificationToken);
        response.put("userId", id.toString());

        return response;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody String rawJson) {
        try {
            User user = objectMapper.readValue(rawJson, User.class);

            if (userManager.getUserByLoginExact(user.getLogin()).isPresent()) {
                throw new ConflictException("User with login '" + user.getLogin() + "' already exists");
            }

            User createdUser = userManager.createUser(user);
            User responseUser = createSafeUserCopy(createdUser);

            Map<String, Object> response = Map.of(
                    "user", responseUser,
                    "message", "User registered successfully"
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            throw new BadRequestException(e.getMessage());
        } catch (Exception e) {
            throw new ConflictException("Registration failed: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER') or (#id == authentication.principal.userId)")
    public Map<String, Object> getUserById(@PathVariable UUID id,
                                           @RequestHeader(value = "X-Object-Signature", required = false) String signature,
                                           @RequestHeader(value = "X-Verification-Token", required = false) String verificationToken) {


        if (verificationToken != null) {
            if (!jwsService.verifyUserIdToken(verificationToken, id.toString())) {
                throw new UnauthorizedException("Invalid user verification token");
            }
        }

        if (signature != null) {
            Map<String, Object> expectedData = new HashMap<>();
            expectedData.put("id", id.toString());

            if (!jwsService.verifySignature(signature, expectedData)) {
                throw new BadRequestException("Invalid object signature");
            }
        }

        User user = userManager.getUserById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        User safeUser = createSafeUserCopy(user);

        Map<String, Object> response = new HashMap<>();
        response.put("user", safeUser);

        Map<String, Object> signatureData = new HashMap<>();
        signatureData.put("id", safeUser.getId().toString());
        signatureData.put("login", safeUser.getLogin());
        signatureData.put("type", getUserType(safeUser));

        response.put("signature", jwsService.createSignature(signatureData));
        response.put("verificationToken", jwsService.createVerificationToken(id.toString()));

        return response;
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER') or (#id == authentication.principal.userId)")
    public Map<String, Object> updateUser(@PathVariable UUID id,
                                          @RequestBody User userDetails,
                                          @RequestHeader(value = "X-Object-Signature", required = true) String signature,
                                          @RequestHeader(value = "X-Verification-Token", required = true) String verificationToken) {

        if (!jwsService.verifyUserIdToken(verificationToken, id.toString())) {
            throw new UnauthorizedException("Invalid user verification token");
        }

        Map<String, Object> expectedData = new HashMap<>();
        expectedData.put("id", id.toString());
        expectedData.put("login", userDetails.getLogin());

        if (!jwsService.verifySignature(signature, expectedData)) {
            throw new BadRequestException("Invalid object signature");
        }

        User updatedUser = userManager.updateUser(id, userDetails);
        User safeUser = createSafeUserCopy(updatedUser);

        Map<String, Object> signatureData = new HashMap<>();
        signatureData.put("id", safeUser.getId().toString());
        signatureData.put("login", safeUser.getLogin());
        signatureData.put("type", getUserType(safeUser));

        Map<String, Object> response = new HashMap<>();
        response.put("user", safeUser);
        response.put("signature", jwsService.createSignature(signatureData));
        response.put("verificationToken", jwsService.createVerificationToken(id.toString()));

        return response;
    }

    @GetMapping("/customers")
    public List<User> getAllCustomers() {
        List<User> users = userManager.getAllUsers();
        return users.stream()
                .filter(u -> u instanceof Customer)
                .map(this::createSafeUserCopy)
                .toList();
    }

    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public List<User> getAllUsersForAdmin() {
        return userManager.getAllUsers().stream()
                .map(this::createSafeUserCopy)
                .toList();
    }

    @PostMapping("/{id}/activate")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public void activateUser(@PathVariable UUID id) {
        try {
            userManager.activateUser(id);
        } catch (IllegalArgumentException e) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }
    }

    @PostMapping("/{id}/deactivate")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public void deactivateUser(@PathVariable UUID id) {
        try {
            userManager.deactivateUser(id);
        } catch (IllegalArgumentException e) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }
    }

    @GetMapping("/search/exact")
    public User getUserByLoginExact(@RequestParam String login) {
        return userManager.getUserByLoginExactSafe(login)
                .map(this::createSafeUserCopy)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with login: " + login));
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public List<User> getUsersByLoginContaining(@RequestParam String loginPart) {
        List<User> users = userManager.getUsersByLoginContaining(loginPart);
        return users.stream()
                .map(this::createSafeUserCopy)
                .toList();
    }

    private User createSafeUserCopy(User user) {
        User safeCopy;

        if (user instanceof Customer) {
            Customer customer = (Customer) user;
            Customer copy = new Customer(customer.getLogin(), customer.getName(),
                    customer.getEmail(), customer.getPhoneNumber(),
                    customer.getAddress());
            copy.setId(customer.getId());
            copy.setActive(customer.isActive());
            safeCopy = copy;
        } else if (user instanceof Administrator) {
            Administrator admin = (Administrator) user;
            Administrator copy = new Administrator(admin.getLogin(), admin.getName(),
                    admin.getEmail(), admin.getDepartment());
            copy.setId(admin.getId());
            copy.setActive(admin.isActive());
            safeCopy = copy;
        } else if (user instanceof ResourceManager) {
            ResourceManager manager = (ResourceManager) user;
            ResourceManager copy = new ResourceManager(manager.getLogin(), manager.getName(),
                    manager.getEmail(), manager.getManagedResourceType());
            copy.setId(manager.getId());
            copy.setActive(manager.isActive());
            safeCopy = copy;
        } else {
            safeCopy = new User() {};
            safeCopy.setId(user.getId());
            safeCopy.setLogin(user.getLogin());
            safeCopy.setName(user.getName());
            safeCopy.setEmail(user.getEmail());
            safeCopy.setActive(user.isActive());
        }

        return safeCopy;
    }

    private String getUserType(User user) {
        if (user instanceof Administrator) return "administrator";
        if (user instanceof ResourceManager) return "resourceManager";
        if (user instanceof Customer) return "customer";
        return "user";
    }
}
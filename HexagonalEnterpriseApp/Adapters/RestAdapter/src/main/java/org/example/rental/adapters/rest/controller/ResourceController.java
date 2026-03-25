package org.example.rental.adapters.rest.controller;

import org.example.rental.domain.exception.BadRequestException;
import org.example.rental.domain.exception.ConflictException;
import org.example.rental.domain.exception.ResourceNotFoundException;
import org.example.rental.domain.model.Resource;
import org.example.rental.port.in.ResourceUseCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/resources")
public class ResourceController {

    @Autowired
    private ResourceUseCase resourceUseCase;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public Resource createResource(@RequestBody Resource resource) {
        try {
            return resourceUseCase.createResource(resource);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException(e.getMessage());
        }
    }

    @GetMapping("/available")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN') or hasRole('MANAGER')")
    public List<Resource> getAvailableResources() {
        return resourceUseCase.getAllResources();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN') or hasRole('MANAGER')")
    public Resource getResourceById(@PathVariable UUID id) {
        return resourceUseCase.getResourceById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found with id: " + id));
    }

    @GetMapping
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN') or hasRole('MANAGER')")
    public List<Resource> getAllResources() {
        return resourceUseCase.getAllResources();
    }

    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public List<Resource> getAllResourcesForAdmin() {
        return resourceUseCase.getAllResources();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public Resource updateResource(@PathVariable UUID id, @RequestBody Resource resourceDetails) {
        try {
            return resourceUseCase.updateResource(id, resourceDetails);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public void deleteResource(@PathVariable UUID id) {
        try {
            resourceUseCase.deleteResource(id);
        } catch (IllegalArgumentException e) {
            throw new ResourceNotFoundException("Resource not found with id: " + id);
        } catch (IllegalStateException e) {
            throw new ConflictException(e.getMessage());
        }
    }
}

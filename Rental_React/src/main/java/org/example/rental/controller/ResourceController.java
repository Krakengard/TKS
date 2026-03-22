package org.example.rental.controller;

import org.example.rental.exception.BadRequestException;
import org.example.rental.exception.ConflictException;
import org.example.rental.exception.ResourceNotFoundException;
import org.example.rental.model.Resource;
import org.example.rental.service.ResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/resources")
public class ResourceController {

    @Autowired
    private ResourceService resourceService;

    @PostMapping
    public Resource createResource(@RequestBody Resource resource) {
        try {
            return resourceService.createResource(resource);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException(e.getMessage());
        }
    }

    @GetMapping("/available")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN') or hasRole('MANAGER')")
    public List<Resource> getAvailableResources() {
        return resourceService.getAllResources();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN') or hasRole('MANAGER')")
    public Resource getResourceById(@PathVariable UUID id) {
        return resourceService.getResourceById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found with id: " + id));
    }

    @GetMapping
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN') or hasRole('MANAGER')")
    public List<Resource> getAllResources() {
        return resourceService.getAllResources();
    }

    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public List<Resource> getAllResourcesForAdmin() {
        return resourceService.getAllResources();
    }


    @PutMapping("/{id}")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN') or hasRole('MANAGER')")
    public Resource updateResource(@PathVariable UUID id, @RequestBody Resource resourceDetails) {
        try {
            return resourceService.updateResource(id, resourceDetails);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN') or hasRole('MANAGER')")
    public void deleteResource(@PathVariable UUID id) {
        try {
            resourceService.deleteResource(id);
        } catch (IllegalArgumentException e) {
            throw new ResourceNotFoundException("Resource not found with id: " + id);
        } catch (IllegalStateException e) {
            throw new ConflictException(e.getMessage());
        }
    }
}

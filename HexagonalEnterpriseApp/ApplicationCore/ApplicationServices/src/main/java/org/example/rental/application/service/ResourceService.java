package org.example.rental.application.service;

import org.example.rental.domain.model.Resource;
import org.example.rental.ports.output.ResourceRepositoryPort;
import org.example.rental.ports.output.AllocationRepositoryPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ResourceService {

    @Autowired
    private ResourceRepositoryPort resourceRepository;

    @Autowired
    private AllocationRepositoryPort allocationRepository;

    public Resource createResource(Resource resource) {
        validateResource(resource);
        return resourceRepository.save(resource);
    }

    public Optional<Resource> getResourceById(UUID id) {
        return resourceRepository.findById(id);
    }

    public List<Resource> getAllResources() {
        return resourceRepository.findAll();
    }

    public Resource updateResource(UUID id, Resource resourceDetails) {
        Resource existingResource = resourceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Resource not found with id: " + id));

        existingResource.setName(resourceDetails.getName());
        existingResource.setDescription(resourceDetails.getDescription());
        existingResource.setType(resourceDetails.getType());
        existingResource.setPricePerHour(resourceDetails.getPricePerHour());

        validateResource(existingResource);
        return resourceRepository.save(existingResource);
    }

    public void deleteResource(UUID id) {
        if (allocationRepository.hasActiveAllocationsForResource(id)) {
            throw new IllegalStateException("Cannot delete resource with active allocations");
        }
        resourceRepository.delete(id);
    }

    private void validateResource(Resource resource) {
        if (resource.getName() == null || resource.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Resource name is required");
        }
        if (resource.getType() == null || resource.getType().trim().isEmpty()) {
            throw new IllegalArgumentException("Resource type is required");
        }
        if (resource.getPricePerHour() < 0) {
            throw new IllegalArgumentException("Price per hour cannot be negative");
        }
    }
}
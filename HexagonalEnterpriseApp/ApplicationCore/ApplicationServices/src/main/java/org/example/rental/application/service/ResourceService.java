package org.example.rental.application.service;

import org.example.rental.domain.exception.BadRequestException;
import org.example.rental.domain.exception.ConflictException;
import org.example.rental.domain.exception.ResourceNotFoundException;
import org.example.rental.domain.model.Resource;
import org.example.rental.port.in.ResourceUseCase;
import org.example.rental.port.out.AllocationRepository;
import org.example.rental.port.out.ResourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ResourceService implements ResourceUseCase {

    @Autowired
    private ResourceRepository resourceRepository;

    @Autowired
    private AllocationRepository allocationRepository;

    @Override
    public Resource createResource(Resource resource) {
        validateResource(resource);
        return resourceRepository.save(resource);
    }

    @Override
    public Optional<Resource> getResourceById(UUID id) {
        return resourceRepository.findById(id);
    }

    @Override
    public List<Resource> getAllResources() {
        return resourceRepository.findAll();
    }

    @Override
    public Resource updateResource(UUID id, Resource resourceDetails) {
        Resource existing = resourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found with id: " + id));

        existing.setName(resourceDetails.getName());
        existing.setDescription(resourceDetails.getDescription());
        existing.setType(resourceDetails.getType());
        existing.setPricePerHour(resourceDetails.getPricePerHour());

        validateResource(existing);
        return resourceRepository.save(existing);
    }

    @Override
    public void deleteResource(UUID id) {
        if (!resourceRepository.findById(id).isPresent()) {
            throw new ResourceNotFoundException("Resource not found with id: " + id);
        }
        if (allocationRepository.hasActiveAllocationsForResource(id)) {
            throw new ConflictException("Cannot delete resource with active allocations");
        }
        resourceRepository.delete(id);
    }

    private void validateResource(Resource resource) {
        if (resource.getName() == null || resource.getName().trim().isEmpty()) {
            throw new BadRequestException("Resource name is required");
        }
        if (resource.getType() == null || resource.getType().trim().isEmpty()) {
            throw new BadRequestException("Resource type is required");
        }
        if (resource.getPricePerHour() < 0) {
            throw new BadRequestException("Price per hour cannot be negative");
        }
    }
}

package org.example.rental.port.in;

import org.example.rental.domain.model.Resource;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface ResourceUseCase {

    Resource createResource(Resource resource);

    Optional<Resource> getResourceById(UUID id);

    List<Resource> getAllResources();

    Resource updateResource(UUID id, Resource resourceDetails);

    void deleteResource(UUID id);
}

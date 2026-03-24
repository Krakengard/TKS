package org.example.rental.ports.output;

import org.example.rental.domain.model.Resource;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ResourceRepositoryPort {

    Resource save(Resource resource);

    Optional<Resource> findById(UUID id);

    List<Resource> findAll();

    void delete(UUID id);

    boolean exists(UUID id);
}
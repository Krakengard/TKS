package org.example.rental.port.out;

import org.example.rental.domain.model.Resource;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface ResourceRepository {

    Resource save(Resource resource);

    Optional<Resource> findById(UUID id);

    List<Resource> findAll();

    void delete(UUID id);
}

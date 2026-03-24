package org.example.rental.adapters.repository.jpa;

import org.example.rental.domain.model.Resource;
import org.example.rental.port.out.ResourceRepository;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class JpaResourceRepository implements ResourceRepository {

    private final Map<UUID, Resource> resources = new ConcurrentHashMap<>();

    @Override
    public Resource save(Resource resource) {
        resources.put(resource.getId(), resource);
        return resource;
    }

    @Override
    public Optional<Resource> findById(UUID id) {
        return Optional.ofNullable(resources.get(id));
    }

    @Override
    public List<Resource> findAll() {
        return new ArrayList<>(resources.values());
    }

    @Override
    public void delete(UUID id) {
        resources.remove(id);
    }
}

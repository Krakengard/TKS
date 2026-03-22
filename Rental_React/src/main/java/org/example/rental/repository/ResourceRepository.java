package org.example.rental.repository;

import org.example.rental.model.Resource;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class ResourceRepository {
    private final Map<UUID, Resource> resources = new ConcurrentHashMap<>();

    public Resource save(Resource resource) {
        resources.put(resource.getId(), resource);
        return resource;
    }

    public Optional<Resource> findById(UUID id) {
        return Optional.ofNullable(resources.get(id));
    }

    public List<Resource> findAll() {
        return new ArrayList<>(resources.values());
    }

    public void delete(UUID id) {
        resources.remove(id);
    }

    public boolean exists(UUID id) {
        return resources.containsKey(id);
    }
}
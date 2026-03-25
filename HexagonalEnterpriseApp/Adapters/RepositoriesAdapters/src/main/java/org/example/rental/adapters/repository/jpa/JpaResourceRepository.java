package org.example.rental.adapters.repository.jpa;

import org.example.rental.adapters.repository.entity.ResourceEnt;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class JpaResourceRepository {

    private final Map<UUID, ResourceEnt> resources = new ConcurrentHashMap<>();


    public ResourceEnt save(ResourceEnt resource) {

        resources.put(resource.getId(), resource);
        return resource;
    }


    public Optional<ResourceEnt> findById(UUID id) {
        return Optional.ofNullable(resources.get(id));
    }


    public List<ResourceEnt> findAll() {
        return new ArrayList<>(resources.values());
    }


    public void delete(UUID id) {
        resources.remove(id);
    }


    public boolean exists(UUID id) {
        return resources.containsKey(id);
    }
}

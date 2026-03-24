package org.example.rental.adapters.repository.adapter;

import org.example.rental.adapters.repository.entity.ResourceEnt;
import org.example.rental.ports.output.ResourceRepositoryPort;
import org.example.rental.domain.model.Resource;

import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class ResourceRepositoryAdapter implements ResourceRepositoryPort {

    private final Map<UUID, ResourceEnt> resources = new ConcurrentHashMap<>();


    @Override
    public Resource save(Resource resource) {

        ResourceEnt entity = toEntity(resource);

        resources.put(entity.getId(), entity);

        return toDomain(entity);
    }


    @Override
    public Optional<Resource> findById(UUID id) {

        ResourceEnt entity = resources.get(id);

        if (entity == null) {
            return Optional.empty();
        }

        return Optional.of(toDomain(entity));
    }


    @Override
    public List<Resource> findAll() {

        List<Resource> result = new ArrayList<>();

        for (ResourceEnt ent : resources.values()) {
            result.add(toDomain(ent));
        }

        return result;
    }


    @Override
    public void delete(UUID id) {
        resources.remove(id);
    }


    @Override
    public boolean exists(UUID id) {
        return resources.containsKey(id);
    }


    private ResourceEnt toEntity(Resource resource) {

        ResourceEnt entity = new ResourceEnt();

        entity.setId(resource.getId());
        entity.setName(resource.getName());
        entity.setDescription(resource.getDescription());
        entity.setType(resource.getType());
        entity.setPricePerHour(resource.getPricePerHour());

        return entity;
    }


    private Resource toDomain(ResourceEnt entity) {

        Resource resource = new Resource();

        resource.setId(entity.getId());
        resource.setName(entity.getName());
        resource.setDescription(entity.getDescription());
        resource.setType(entity.getType());
        resource.setPricePerHour(entity.getPricePerHour());

        return resource;
    }
}
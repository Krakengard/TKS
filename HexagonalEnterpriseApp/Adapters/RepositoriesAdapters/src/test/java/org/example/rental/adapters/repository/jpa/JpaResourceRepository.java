package org.example.rental.adapters.repository.jpa;

import org.example.rental.domain.model.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JpaResourceRepositoryTest {

    private JpaResourceRepository resourceRepository;

    @BeforeEach
    void setUp() {
        resourceRepository = new JpaResourceRepository();
    }

    @Test
    void shouldSaveAndRemoveResource() {
        // Arrange
        Resource resource = new Resource("Laptop", "Dell", "Sprzęt IT", 50.0);
        UUID id = UUID.randomUUID();
        resource.setId(id);

        // Act
        resourceRepository.save(resource); //

        // Assert Save
        Optional<Resource> found = resourceRepository.findById(id); //
        assertTrue(found.isPresent());
        assertEquals("Laptop", found.get().getName());

        // Act Delete
        resourceRepository.delete(id); //

        // Assert Delete
        assertFalse(resourceRepository.findById(id).isPresent());
    }

    @Test
    void shouldReturnAllResources() {
        // Arrange
        saveResource("R1");
        saveResource("R2");

        // Act
        List<Resource> all = resourceRepository.findAll(); //

        // Assert
        assertEquals(2, all.size());
    }

    private void saveResource(String name) {
        Resource r = new Resource(name, "Desc", "Type", 10.0);
        r.setId(UUID.randomUUID());
        resourceRepository.save(r);
    }
}
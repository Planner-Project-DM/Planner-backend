package net.dysky.planner.group;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

interface GroupRepository extends JpaRepository<Group, UUID> {
    Optional<Group> findByName(String name);
}

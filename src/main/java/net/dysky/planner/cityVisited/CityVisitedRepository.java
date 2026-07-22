package net.dysky.planner.cityVisited;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

interface CityVisitedRepository extends JpaRepository<CityVisited, UUID> {
    boolean existsByCity(String city);
}

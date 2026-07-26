package net.dysky.planner.tripItem;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

interface TripItemRepository extends JpaRepository<TripItem, UUID> {
    List<TripItem> findAllByAddress_City(String city);

    TripItem findByName(String name);
}

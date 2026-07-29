package net.dysky.planner.tripItem;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface TripItemRepository extends JpaRepository<TripItem, UUID> {
    List<TripItem> findAllByAddress_City(String addressCity);

    List<TripItem> findAllByAddress_CityAndCategory(String addressCity, TripItemCategory category);

    Optional<TripItem> findByName(String name);
}

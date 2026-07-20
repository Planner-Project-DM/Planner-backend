package net.dysky.planner.hotel;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

interface HotelRepository extends JpaRepository<Hotel, UUID> {
    List<Hotel> findByAddress_City(String addressCity);

    Hotel findByName(String name);
}

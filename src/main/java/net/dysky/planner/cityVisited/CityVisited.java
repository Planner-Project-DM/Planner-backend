package net.dysky.planner.cityVisited;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "city_visited")
public class CityVisited {

    @Id
    @GeneratedValue
    private UUID id;

    private String city;
    private LocalDateTime visitedAt = LocalDateTime.now();
}

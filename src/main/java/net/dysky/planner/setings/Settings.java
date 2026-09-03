package net.dysky.planner.setings;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "settings")
public class Settings {

    @Id
    @GeneratedValue
    private UUID id;

    private String currency;

}
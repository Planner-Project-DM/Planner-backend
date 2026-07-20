package net.dysky.planner.hotel;

import jakarta.persistence.Embeddable;

import java.math.BigDecimal;

@Embeddable
public class Location {
    BigDecimal latitude;
    BigDecimal longitude;
}

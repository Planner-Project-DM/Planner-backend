package net.dysky.planner.address;

import jakarta.persistence.Embeddable;

@Embeddable
public class Address {
    private String country;
    private String city;
    private String street;
    private String postalCode;
    private String houseNumber;
    private String apartmentNumber;
}

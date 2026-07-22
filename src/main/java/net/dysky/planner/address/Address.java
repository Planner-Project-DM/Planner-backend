package net.dysky.planner.address;

import jakarta.persistence.Embeddable;
import lombok.*;

@Getter
@Setter
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Address {
    private String country;
    private String city;
    private String street;
    private String postalCode;
    private String houseNumber;
}

package net.dysky.planner.hotel;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TagsDTO(
        String name,

        @JsonProperty("addr:street")
        String street,

        @JsonProperty("addr:housenumber")
        String houseNumber,

        @JsonProperty("addr:postcode")
        String postalCode,

        @JsonProperty("addr:city")
        String city,

        @JsonProperty("addr:country")
        String country,

        String email,

        String phone,

        String stars,

        String website

) {
}

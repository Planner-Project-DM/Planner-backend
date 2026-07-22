package net.dysky.planner.hotel;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Embeddable
public class Location {
    @Column(precision = 11, scale = 8)
    BigDecimal latitude;

    @Column(precision = 11, scale = 8)
    BigDecimal longitude;
}

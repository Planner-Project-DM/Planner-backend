package net.dysky.planner.group;

import java.util.UUID;

public record CreateGroupDTO(
    String name,
    String email,
    UUID tripId
) {
}

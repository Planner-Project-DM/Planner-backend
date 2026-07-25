package net.dysky.planner.friendship;

import java.util.UUID;

public record FriendshipDTO(
        UUID id,
        String email,
        String name,
        String surname
) {
}

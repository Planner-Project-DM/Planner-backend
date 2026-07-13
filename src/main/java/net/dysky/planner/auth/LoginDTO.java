package net.dysky.planner.auth;

public record LoginDTO(
        String email,
        String password
) {
}

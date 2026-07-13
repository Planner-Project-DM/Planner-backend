package net.dysky.planner.auth;

public record RegisterDTO(
        String firstname,
        String lastName,
        String email,
        String password
) {
}

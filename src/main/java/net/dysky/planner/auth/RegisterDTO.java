package net.dysky.planner.auth;

public record RegisterDTO(
        String firstName,
        String lastName,
        String email,
        String password,
        String phoneNumber
) {
}

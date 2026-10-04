package net.dysky.planner.user;

public record UserUpdateDTO(
        String firstName,
        String lastName,
        String email,
        String phoneNumber,
        boolean isActive
) {
}

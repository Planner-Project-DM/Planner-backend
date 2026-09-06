package net.dysky.planner.usersetings;

public record CreateSettingsDTO(
    String currency,
    Double budgetLimit,
    String language
) {
}

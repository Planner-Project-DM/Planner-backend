package net.dysky.planner.setings;

public record CreateSettingsDTO(
    String currency,
    Double budgetLimit
) {
}

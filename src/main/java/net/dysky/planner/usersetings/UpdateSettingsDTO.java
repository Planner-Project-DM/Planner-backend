package net.dysky.planner.usersetings;

public record UpdateSettingsDTO(
        String currency,
        Double budgetLimit,
        String language,
        Boolean isNotificationsEnabled
) {
}

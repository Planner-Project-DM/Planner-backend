package net.dysky.planner.usersettings;

public record UpdateSettingsDTO(
        String currency,
        Double budgetLimit,
        String language,
        Boolean isNotificationsEnabled
) {
}

package net.dysky.planner.usersettings;

public record CreateSettingsDTO(
    String currency,
    Double budgetLimit,
    String language,
    boolean isNotificationsEnabled
) {
}

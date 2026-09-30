package net.dysky.planner.usersettings;

public record UpdateSettingsDTO(
        String currency,
        Double budgetLimit,
        String language,
        Boolean isNotificationsEnabled,
        String notificationChannel,
        Boolean notifyFriendshipRequest,
        Boolean notifyFriendshipRemoved,
        Boolean notifyScheduleItemAdded,
        Boolean notifyScheduleItemUpdated,
        Boolean notifyScheduleItemDeleted,
        Boolean notifyGroupMemberAdded,
        Boolean notifyGroupMemberRemoved,
        Boolean notifyFundItemCostUpdated
) {
}
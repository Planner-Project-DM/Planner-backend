package net.dysky.planner.usersettings;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import net.dysky.planner.notification.NotificationChannel;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserSettingsService {

    private final UserSettingsRepository userSettingsRepository;

    public UserSettings findByUserId(UUID id) {
        return userSettingsRepository.findByUser_Id(id).orElseThrow(
                () -> new IllegalArgumentException("User settings not found"));
    }

    public UserSettings createSettings(CreateSettingsDTO createSettingsDTO) {
        UserSettings settings = new UserSettings();

        settings.setCurrency(Currency.valueOf(createSettingsDTO.currency()));
        settings.setBudgetLimit(createSettingsDTO.budgetLimit());
        settings.setLanguage(Language.valueOf(createSettingsDTO.language()));
        settings.setNotificationEnabled(createSettingsDTO.isNotificationsEnabled());

        return settings;
    }

    public UserSettings createDefaultSettings() {
       return new UserSettings();
    }

    @Transactional
    public UserSettings updateSettings(UserSettings settings, UpdateSettingsDTO updateSettingsDTO) {

        if (updateSettingsDTO.currency() != null) {
            settings.setCurrency(Currency.valueOf(updateSettingsDTO.currency()));
        }

        if (updateSettingsDTO.budgetLimit() != null) {
            settings.setBudgetLimit(updateSettingsDTO.budgetLimit());
        }

        if (updateSettingsDTO.language() != null) {
            settings.setLanguage(Language.valueOf(updateSettingsDTO.language()));
        }

        if (updateSettingsDTO.isNotificationsEnabled() != null) {
            settings.setNotificationEnabled(updateSettingsDTO.isNotificationsEnabled());
        }

        if (updateSettingsDTO.notificationChannel() != null) {
            settings.setNotificationChannel(NotificationChannel.valueOf(updateSettingsDTO.notificationChannel()));
        }

        if (updateSettingsDTO.notifyFriendshipRequest() != null) {
            settings.setNotifyFriendshipRequest(updateSettingsDTO.notifyFriendshipRequest());
        }

        if (updateSettingsDTO.notifyFriendshipRemoved() != null) {
            settings.setNotifyFriendshipRemoved(updateSettingsDTO.notifyFriendshipRemoved());
        }

        if (updateSettingsDTO.notifyScheduleItemAdded() != null) {
            settings.setNotifyScheduleItemAdded(updateSettingsDTO.notifyScheduleItemAdded());
        }

        if (updateSettingsDTO.notifyScheduleItemUpdated() != null) {
            settings.setNotifyScheduleItemUpdated(updateSettingsDTO.notifyScheduleItemUpdated());
        }

        if (updateSettingsDTO.notifyScheduleItemDeleted() != null) {
            settings.setNotifyScheduleItemDeleted(updateSettingsDTO.notifyScheduleItemDeleted());
        }

        if (updateSettingsDTO.notifyGroupMemberAdded() != null) {
            settings.setNotifyGroupMemberAdded(updateSettingsDTO.notifyGroupMemberAdded());
        }

        if (updateSettingsDTO.notifyGroupMemberRemoved() != null) {
            settings.setNotifyGroupMemberRemoved(updateSettingsDTO.notifyGroupMemberRemoved());
        }

        if (updateSettingsDTO.notifyFundItemCostUpdated() != null) {
            settings.setNotifyFundItemCostUpdated(updateSettingsDTO.notifyFundItemCostUpdated());
        }

        return settings;
    }

}

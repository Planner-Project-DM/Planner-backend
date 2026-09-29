package net.dysky.planner.usersettings;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
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
        UserSettings settings = new UserSettings();

        return settings;
    }

    @Transactional
    public UserSettings updateSettings(UserSettings settings, UpdateSettingsDTO updateSettingsDTO) {

        if(updateSettingsDTO.currency() != null) {
            settings.setCurrency(Currency.valueOf(updateSettingsDTO.currency()));
        }

        if(updateSettingsDTO.budgetLimit() != null) {
            settings.setBudgetLimit(updateSettingsDTO.budgetLimit());
        }

        if(updateSettingsDTO.language() != null) {
            settings.setLanguage(Language.valueOf(updateSettingsDTO.language()));
        }

        if(updateSettingsDTO.isNotificationsEnabled() != null) {
            settings.setNotificationEnabled(updateSettingsDTO.isNotificationsEnabled());
        }

        return settings;
    }

}

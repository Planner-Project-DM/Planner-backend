package net.dysky.planner.usersetings;

import org.springframework.stereotype.Service;

@Service
public class UserSettingsService {

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

        settings.setCurrency(Currency.PLN);
        settings.setBudgetLimit(0.0);
        settings.setLanguage(Language.PL);
        settings.setNotificationEnabled(true);

        return settings;
    }

}

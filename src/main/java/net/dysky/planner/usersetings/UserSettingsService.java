package net.dysky.planner.usersetings;

import org.springframework.stereotype.Service;

@Service
public class UserSettingsService {

    public UserSettings createSettings(CreateSettingsDTO createSettingsDTO) {
        UserSettings settings = new UserSettings();

        settings.setCurrency(createSettingsDTO.currency());
        settings.setBudgetLimit(createSettingsDTO.budgetLimit());
        settings.setLanguage(createSettingsDTO.language());

        return settings;
    }

    public UserSettings createDefaultSettings() {
        UserSettings settings = new UserSettings();

        settings.setCurrency("PLN");
        settings.setBudgetLimit(0.0);
        settings.setLanguage("PL");

        return settings;
    }

}

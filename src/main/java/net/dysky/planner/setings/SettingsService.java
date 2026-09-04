package net.dysky.planner.setings;

import org.springframework.stereotype.Service;

@Service
public class SettingsService {

    public Settings createSettings(CreateSettingsDTO createSettingsDTO) {
        Settings settings = new Settings();

        settings.setCurrency(createSettingsDTO.currency());
        settings.setBudgetLimit(createSettingsDTO.budgetLimit());

        return settings;
    }

    public Settings createDefaultSettings() {
        Settings settings = new Settings();

        settings.setCurrency("PLN");
        settings.setBudgetLimit(0.0);

        return settings;
    }

}

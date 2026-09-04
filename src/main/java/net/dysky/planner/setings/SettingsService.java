package net.dysky.planner.setings;

import org.springframework.stereotype.Service;

@Service
public class SettingsService {

    public Settings createSettings(CreateSettingsDTO createSettingsDTO) {
        Settings settings = new Settings();

        settings.setCurrency(createSettingsDTO.currency());

        return settings;
    }

    public Settings createDefaultSettings() {
        Settings settings = new Settings();

        settings.setCurrency("PLN");

        return settings;
    }

}

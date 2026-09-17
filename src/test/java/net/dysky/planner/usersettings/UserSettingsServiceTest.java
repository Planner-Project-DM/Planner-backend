package net.dysky.planner.usersettings;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@ExtendWith(MockitoExtension.class)
class UserSettingsServiceTest {

    @InjectMocks
    private UserSettingsService userSettingsService;

    @Test
    @DisplayName("createSettings: shouldMapAllFields_whenDtoIsValid")
    void createSettings_shouldMapAllFields_whenDtoIsValid() {
        // given
        CreateSettingsDTO dto = new CreateSettingsDTO(
                "EUR",
                1500.0,
                "EN",
                false
        );

        // when
        UserSettings result = userSettingsService.createSettings(dto);

        // then
        assertAll(
                () -> assertThat(result).isNotNull(),
                () -> assertThat(result.getCurrency()).isEqualTo(Currency.EUR),
                () -> assertThat(result.getBudgetLimit()).isEqualTo(1500.0),
                () -> assertThat(result.getLanguage()).isEqualTo(Language.EN),
                () -> assertThat(result.isNotificationEnabled()).isFalse()
        );
    }

    @Test
    @DisplayName("createDefaultSettings: shouldSetDefaultValues")
    void createDefaultSettings_shouldSetDefaultValues() {
        // when
        UserSettings result = userSettingsService.createDefaultSettings();

        // then
        assertAll(
                () -> assertThat(result).isNotNull(),
                () -> assertThat(result.getCurrency()).isEqualTo(Currency.PLN),
                () -> assertThat(result.getBudgetLimit()).isEqualTo(0.0),
                () -> assertThat(result.getLanguage()).isEqualTo(Language.PL),
                () -> assertThat(result.isNotificationEnabled()).isTrue()
        );
    }

    @Test
    @DisplayName("updateSettings: shouldUpdateAllFields_whenAllDtoFieldsAreProvided (Branch: TRUE)")
    void updateSettings_shouldUpdateAllFields_whenAllDtoFieldsAreProvided() {
        // given
        UserSettings settings = new UserSettings();
        settings.setCurrency(Currency.PLN);
        settings.setBudgetLimit(100.0);
        settings.setLanguage(Language.PL);
        settings.setNotificationEnabled(true);

        UpdateSettingsDTO updateDto = new UpdateSettingsDTO(
                "USD",
                3000.0,
                "EN",
                false
        );

        // when
        UserSettings updated = userSettingsService.updateSettings(settings, updateDto);

        assertAll(
                () -> assertThat(updated.getCurrency()).isEqualTo(Currency.USD),
                () -> assertThat(updated.getBudgetLimit()).isEqualTo(3000.0),
                () -> assertThat(updated.getLanguage()).isEqualTo(Language.EN),
                () -> assertThat(updated.isNotificationEnabled()).isFalse()
        );
    }

    @Test
    @DisplayName("updateSettings: shouldNotModifyFields_whenAllDtoFieldsAreNull (Branch: FALSE)")
    void updateSettings_shouldNotModifyFields_whenAllDtoFieldsAreNull() {
        // given
        UserSettings settings = new UserSettings();
        settings.setCurrency(Currency.PLN);
        settings.setBudgetLimit(250.0);
        settings.setLanguage(Language.PL);
        settings.setNotificationEnabled(true);

        UpdateSettingsDTO emptyDto = new UpdateSettingsDTO(null, null, null, null);

        // when
        UserSettings updated = userSettingsService.updateSettings(settings, emptyDto);

        // then
        assertAll(
                () -> assertThat(updated.getCurrency()).isEqualTo(Currency.PLN),
                () -> assertThat(updated.getBudgetLimit()).isEqualTo(250.0),
                () -> assertThat(updated.getLanguage()).isEqualTo(Language.PL),
                () -> assertThat(updated.isNotificationEnabled()).isTrue()
        );
    }

    @Test
    @DisplayName("updateSettings: shouldUpdateOnlySelectedFields_whenPartialDtoProvided")
    void updateSettings_shouldUpdateOnlySelectedFields_whenPartialDtoProvided() {
        // given
        UserSettings settings = new UserSettings();
        settings.setCurrency(Currency.PLN);
        settings.setBudgetLimit(100.0);
        settings.setLanguage(Language.PL);
        settings.setNotificationEnabled(true);

        UpdateSettingsDTO partialDto = new UpdateSettingsDTO("EUR", null, null, false);

        // when
        UserSettings updated = userSettingsService.updateSettings(settings, partialDto);

        // then
        assertAll(
                () -> assertThat(updated.getCurrency()).isEqualTo(Currency.EUR),
                () -> assertThat(updated.getBudgetLimit()).isEqualTo(100.0),
                () -> assertThat(updated.getLanguage()).isEqualTo(Language.PL),
                () -> assertThat(updated.isNotificationEnabled()).isFalse()
        );
    }
}
package net.dysky.planner.usersettings;

import net.dysky.planner.notification.NotificationChannel;
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
                () -> assertThat(result.isNotificationEnabled()).isTrue(),
                () -> assertThat(result.getNotificationChannel()).isEqualTo(NotificationChannel.PUSH),
                () -> assertThat(result.isNotifyFriendshipRequest()).isTrue(),
                () -> assertThat(result.isNotifyFriendshipRemoved()).isTrue(),
                () -> assertThat(result.isNotifyScheduleItemAdded()).isTrue(),
                () -> assertThat(result.isNotifyScheduleItemUpdated()).isTrue(),
                () -> assertThat(result.isNotifyScheduleItemDeleted()).isTrue(),
                () -> assertThat(result.isNotifyGroupMemberAdded()).isTrue(),
                () -> assertThat(result.isNotifyGroupMemberRemoved()).isTrue(),
                () -> assertThat(result.isNotifyFundItemCostUpdated()).isTrue()
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
        settings.setNotificationChannel(NotificationChannel.PUSH);
        settings.setNotifyFriendshipRequest(true);
        settings.setNotifyFriendshipRemoved(true);
        settings.setNotifyScheduleItemAdded(true);
        settings.setNotifyScheduleItemUpdated(true);
        settings.setNotifyScheduleItemDeleted(true);
        settings.setNotifyGroupMemberAdded(true);
        settings.setNotifyGroupMemberRemoved(true);
        settings.setNotifyFundItemCostUpdated(true);

        UpdateSettingsDTO updateDto = new UpdateSettingsDTO(
                "USD",
                3000.0,
                "EN",
                false,
                NotificationChannel.PUSH.name(),
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false
        );

        // when
        UserSettings updated = userSettingsService.updateSettings(settings, updateDto);

        // then
        assertAll(
                () -> assertThat(updated.getCurrency()).isEqualTo(Currency.USD),
                () -> assertThat(updated.getBudgetLimit()).isEqualTo(3000.0),
                () -> assertThat(updated.getLanguage()).isEqualTo(Language.EN),
                () -> assertThat(updated.isNotificationEnabled()).isFalse(),
                () -> assertThat(updated.getNotificationChannel()).isEqualTo(NotificationChannel.valueOf(updateDto.notificationChannel())),
                () -> assertThat(updated.isNotifyFriendshipRequest()).isFalse(),
                () -> assertThat(updated.isNotifyFriendshipRemoved()).isFalse(),
                () -> assertThat(updated.isNotifyScheduleItemAdded()).isFalse(),
                () -> assertThat(updated.isNotifyScheduleItemUpdated()).isFalse(),
                () -> assertThat(updated.isNotifyScheduleItemDeleted()).isFalse(),
                () -> assertThat(updated.isNotifyGroupMemberAdded()).isFalse(),
                () -> assertThat(updated.isNotifyGroupMemberRemoved()).isFalse(),
                () -> assertThat(updated.isNotifyFundItemCostUpdated()).isFalse()
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
        settings.setNotificationChannel(NotificationChannel.PUSH);
        settings.setNotifyFriendshipRequest(true);
        settings.setNotifyFriendshipRemoved(true);
        settings.setNotifyScheduleItemAdded(true);
        settings.setNotifyScheduleItemUpdated(true);
        settings.setNotifyScheduleItemDeleted(true);
        settings.setNotifyGroupMemberAdded(true);
        settings.setNotifyGroupMemberRemoved(true);
        settings.setNotifyFundItemCostUpdated(true);

        UpdateSettingsDTO emptyDto = new UpdateSettingsDTO(
                null, null, null, null, null, null, null, null, null, null, null, null, null
        );

        // when
        UserSettings updated = userSettingsService.updateSettings(settings, emptyDto);

        // then
        assertAll(
                () -> assertThat(updated.getCurrency()).isEqualTo(Currency.PLN),
                () -> assertThat(updated.getBudgetLimit()).isEqualTo(250.0),
                () -> assertThat(updated.getLanguage()).isEqualTo(Language.PL),
                () -> assertThat(updated.isNotificationEnabled()).isTrue(),
                () -> assertThat(updated.getNotificationChannel()).isEqualTo(NotificationChannel.PUSH),
                () -> assertThat(updated.isNotifyFriendshipRequest()).isTrue(),
                () -> assertThat(updated.isNotifyFriendshipRemoved()).isTrue(),
                () -> assertThat(updated.isNotifyScheduleItemAdded()).isTrue(),
                () -> assertThat(updated.isNotifyScheduleItemUpdated()).isTrue(),
                () -> assertThat(updated.isNotifyScheduleItemDeleted()).isTrue(),
                () -> assertThat(updated.isNotifyGroupMemberAdded()).isTrue(),
                () -> assertThat(updated.isNotifyGroupMemberRemoved()).isTrue(),
                () -> assertThat(updated.isNotifyFundItemCostUpdated()).isTrue()
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
        settings.setNotifyFriendshipRequest(true);
        settings.setNotifyScheduleItemAdded(true);

        UpdateSettingsDTO partialDto = new UpdateSettingsDTO(
                "EUR",
                null,
                null,
                false,
                null,
                null,
                null,
                false,
                null,
                null,
                null,
                null,
                null
        );

        // when
        UserSettings updated = userSettingsService.updateSettings(settings, partialDto);

        // then
        assertAll(
                () -> assertThat(updated.getCurrency()).isEqualTo(Currency.EUR),
                () -> assertThat(updated.isNotificationEnabled()).isFalse(),
                () -> assertThat(updated.isNotifyScheduleItemAdded()).isFalse(),

                () -> assertThat(updated.getBudgetLimit()).isEqualTo(100.0),
                () -> assertThat(updated.getLanguage()).isEqualTo(Language.PL),
                () -> assertThat(updated.isNotifyFriendshipRequest()).isTrue()
        );
    }

    @Test
    @DisplayName("updateSettings: shouldThrowIllegalArgumentException_whenCurrencyIsInvalid")
    void updateSettings_shouldThrowIllegalArgumentException_whenCurrencyIsInvalid() {
        // given
        UserSettings settings = new UserSettings();
        UpdateSettingsDTO invalidDto = new UpdateSettingsDTO(
                "XYZ_UNKNOWN", null, null, null, null, null, null, null, null, null, null, null, null
        );

        // when & then
        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> userSettingsService.updateSettings(settings, invalidDto)
        );
    }

    @Test
    @DisplayName("updateSettings: shouldThrowIllegalArgumentException_whenLanguageIsInvalid")
    void updateSettings_shouldThrowIllegalArgumentException_whenLanguageIsInvalid() {
        // given
        UserSettings settings = new UserSettings();
        UpdateSettingsDTO invalidDto = new UpdateSettingsDTO(
                null, null, "INVALID_LANG", null, null, null, null, null, null, null, null, null, null
        );

        // when & then
        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> userSettingsService.updateSettings(settings, invalidDto)
        );
    }

    @Test
    @DisplayName("updateSettings: shouldThrowIllegalArgumentException_whenNotificationChannelIsInvalid")
    void updateSettings_shouldThrowIllegalArgumentException_whenNotificationChannelIsInvalid() {
        // given
        UserSettings settings = new UserSettings();
        UpdateSettingsDTO invalidDto = new UpdateSettingsDTO(
                null, null, null, null, "PIGEON_CARRIER", null, null, null, null, null, null, null, null
        );

        // when & then
        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> userSettingsService.updateSettings(settings, invalidDto)
        );
    }

    @Test
    @DisplayName("updateSettings: shouldToggleBooleanFlagsFromFalseToTrue")
    void updateSettings_shouldToggleBooleanFlagsFromFalseToTrue() {
        // given
        UserSettings settings = new UserSettings();
        settings.setNotificationEnabled(false);
        settings.setNotifyFriendshipRequest(false);
        settings.setNotifyFriendshipRemoved(false);
        settings.setNotifyScheduleItemAdded(false);
        settings.setNotifyScheduleItemUpdated(false);
        settings.setNotifyScheduleItemDeleted(false);
        settings.setNotifyGroupMemberAdded(false);
        settings.setNotifyGroupMemberRemoved(false);
        settings.setNotifyFundItemCostUpdated(false);

        UpdateSettingsDTO enableAllDto = new UpdateSettingsDTO(
                null, null, null, true, null,
                true, true, true, true, true, true, true, true
        );

        // when
        UserSettings updated = userSettingsService.updateSettings(settings, enableAllDto);

        // then
        assertAll(
                () -> assertThat(updated.isNotificationEnabled()).isTrue(),
                () -> assertThat(updated.isNotifyFriendshipRequest()).isTrue(),
                () -> assertThat(updated.isNotifyFriendshipRemoved()).isTrue(),
                () -> assertThat(updated.isNotifyScheduleItemAdded()).isTrue(),
                () -> assertThat(updated.isNotifyScheduleItemUpdated()).isTrue(),
                () -> assertThat(updated.isNotifyScheduleItemDeleted()).isTrue(),
                () -> assertThat(updated.isNotifyGroupMemberAdded()).isTrue(),
                () -> assertThat(updated.isNotifyGroupMemberRemoved()).isTrue(),
                () -> assertThat(updated.isNotifyFundItemCostUpdated()).isTrue()
        );
    }

    @Test
    @DisplayName("updateSettings: shouldUpdateBudgetLimitToZero_whenZeroProvided")
    void updateSettings_shouldUpdateBudgetLimitToZero_whenZeroProvided() {
        // given
        UserSettings settings = new UserSettings();
        settings.setBudgetLimit(5000.0);

        UpdateSettingsDTO resetBudgetDto = new UpdateSettingsDTO(
                null, 0.0, null, null, null, null, null, null, null, null, null, null, null
        );

        // when
        UserSettings updated = userSettingsService.updateSettings(settings, resetBudgetDto);

        // then
        assertThat(updated.getBudgetLimit()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("updateSettings: shouldOnlyUpdateNotificationChannel")
    void updateSettings_shouldOnlyUpdateNotificationChannel() {
        // given
        UserSettings settings = new UserSettings();
        settings.setCurrency(Currency.PLN);
        settings.setNotificationChannel(NotificationChannel.PUSH);

        UpdateSettingsDTO channelOnlyDto = new UpdateSettingsDTO(
                null, null, null, null, NotificationChannel.PUSH.name(), null, null, null, null, null, null, null, null
        );

        // when
        UserSettings updated = userSettingsService.updateSettings(settings, channelOnlyDto);

        // then
        assertAll(
                () -> assertThat(updated.getNotificationChannel()).isEqualTo(NotificationChannel.PUSH),
                () -> assertThat(updated.getCurrency()).isEqualTo(Currency.PLN)
        );
    }
}
package net.dysky.planner.trip;

import net.dysky.planner.group.Group;
import net.dysky.planner.groupUser.GroupRole;
import net.dysky.planner.tripitem.TripItem;
import net.dysky.planner.user.UserRole;
import net.dysky.planner.user.User;
import net.dysky.planner.usersettings.Currency;
import net.dysky.planner.usersettings.Language;
import net.dysky.planner.usersettings.UserSettings;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record TripDetailsDTO(
        UUID id,
        String name,
        String destination,
        TripStatus status,
        Double budget,
        Double actualCost,
        LocalDate startDate,
        LocalDate endDate,
        TripGroupDTO tripGroup,
        List<TripItineraryDTO> tripItineraries
) {
    public record TripGroupDTO(
            UUID id,
            String name,
            List<GroupUserDTO> groupUsers
    ) {}

    public record SimpleGroupDTO(
            UUID id,
            String name
    ) {}

    public record GroupUserDTO(
            Double balance,
            GroupRole role,
            SimpleGroupDTO group,
            UserDTO user
    ) {}

    public record UserDTO(
            String email,
            String firstName,
            String lastName,
            String phoneNumber,
            UserRole role,
            Boolean isActive,
            UserSettingsDTO settings
    ) {}

    public record UserSettingsDTO(
            Double budgetLimit,
            Currency currency,
            Language language,
            Boolean notificationEnabled
    ) {}

    public record TripItineraryDTO(
            Double price,
            TripItem tripItem
    ) {}

    public static TripDetailsDTO fromEntity(Trip trip) {
        TripGroupDTO groupDto = null;

        if (trip.getTripGroup() != null) {
            Group group = trip.getTripGroup();
            List<GroupUserDTO> groupUserDtos = null;

            if (group.getGroupUsers() != null) {
                groupUserDtos = group.getGroupUsers().stream()
                        .map(gu -> {
                            SimpleGroupDTO simpleGroup = new SimpleGroupDTO(group.getId(), group.getName());
                            UserDTO userDto = null;

                            if (gu.getUser() != null) {
                                User u = gu.getUser();
                                UserSettingsDTO settingsDto = null;

                                if (u.getSettings() != null) {
                                    UserSettings s = u.getSettings();
                                    settingsDto = new UserSettingsDTO(
                                            s.getBudgetLimit(),
                                            s.getCurrency(),
                                            s.getLanguage(),
                                            s.isNotificationEnabled()
                                    );
                                }

                                userDto = new UserDTO(
                                        u.getEmail(),
                                        u.getFirstName(),
                                        u.getLastName(),
                                        u.getPhoneNumber(),
                                        u.getRole(),
                                        u.getIsActive(),
                                        settingsDto
                                );
                            }

                            return new GroupUserDTO(
                                    gu.getBalance(),
                                    gu.getRole(),
                                    simpleGroup,
                                    userDto
                            );
                        })
                        .toList();
            }

            groupDto = new TripGroupDTO(
                    group.getId(),
                    group.getName(),
                    groupUserDtos
            );
        }

        List<TripItineraryDTO> itineraryDtos = null;
        if (trip.getTripItineraries() != null) {
            itineraryDtos = trip.getTripItineraries().stream()
                    .map(it -> new TripItineraryDTO(it.getPrice(), it.getTripItem()))
                    .toList();
        }

        return new TripDetailsDTO(
                trip.getId(),
                trip.getName(),
                trip.getDestination(),
                trip.getStatus(),
                trip.getBudget(),
                trip.getActualCost(),
                trip.getStartDate(),
                trip.getEndDate(),
                groupDto,
                itineraryDtos
        );
    }
}
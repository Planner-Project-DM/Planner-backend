package net.dysky.planner.trip;

import lombok.RequiredArgsConstructor;
import net.dysky.planner.groupUser.GroupUserService;
import net.dysky.planner.user.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("tripSecurity")
@RequiredArgsConstructor
public class TripSecurityEvaluator {

    private final GroupUserService groupUserService;

    private final TripService tripService;

    public boolean hasRoleInTrip(UUID tripId, Authentication authentication) {
        if(authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        Trip trip = tripService.getTripById(tripId);
        User user = (User) authentication.getPrincipal();

        if(user == null) {
            throw new UsernameNotFoundException("User not found");
        }

        return groupUserService.isUserOwnerOrAdminOfGroup(trip.getTripGroup(), user.getEmail());
    }
}

package net.dysky.planner.trip;

import lombok.RequiredArgsConstructor;
import net.dysky.planner.exception.TripNotFoundException;
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

    private final TripRepository tripRepository;

    public boolean hasRoleInTrip(UUID id, Authentication authentication) {
        if(authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        Trip trip = tripRepository.findById(id).orElse(null);

        if (trip == null) {
            return true;
        }

        String email = authentication.getName();

        if (email == null) {
            throw new UsernameNotFoundException("User not found");
        }

        return groupUserService.isUserOwnerOrAdminOfGroup(trip.getTripGroup(), email);
    }
}

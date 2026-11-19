package net.dysky.planner.trip;

import lombok.RequiredArgsConstructor;
import net.dysky.planner.exception.TripNotFoundException;
import net.dysky.planner.groupUser.GroupUserService;
import net.dysky.planner.user.User;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("tripSecurity")
@RequiredArgsConstructor
public class TripSecurityEvaluator {

    private final GroupUserService groupUserService;

    private final TripRepository tripRepository;

    /**
     * Owner or admin of the trip's group.
     */
    public boolean hasRoleInTrip(UUID id, Authentication authentication) {
        String email = resolveEmail(authentication);
        if (email == null) {
            return false;
        }

        Trip trip = findTrip(id);

        return groupUserService.isUserOwnerOrAdminOfGroup(trip.getTripGroup(), email);
    }

    /**
     * Any member of the trip's group (member, admin or owner).
     */
    public boolean isMemberOfTrip(UUID id, Authentication authentication) {
        String email = resolveEmail(authentication);
        if (email == null) {
            return false;
        }

        Trip trip = findTrip(id);

        return groupUserService.isUserMemberOfGroup(trip.getTripGroup(), email);
    }

    private Trip findTrip(UUID id) {
        return tripRepository.findById(id)
                .orElseThrow(() -> new TripNotFoundException("Trip not found"));
    }

    private String resolveEmail(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        // The JWT filter stores the User entity as principal, so getName() would return User#toString().
        if (authentication.getPrincipal() instanceof User user) {
            return user.getEmail();
        }

        return authentication.getName();
    }
}

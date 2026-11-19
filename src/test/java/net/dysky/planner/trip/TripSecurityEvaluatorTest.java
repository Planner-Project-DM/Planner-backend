package net.dysky.planner.trip;

import net.dysky.planner.exception.TripNotFoundException;
import net.dysky.planner.group.Group;
import net.dysky.planner.groupUser.GroupUserService;
import net.dysky.planner.user.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TripSecurityEvaluatorTest {

    @Mock
    private GroupUserService groupUserService;

    @Mock
    private TripRepository tripRepository;

    @InjectMocks
    private TripSecurityEvaluator evaluator;

    private Authentication authWithUserPrincipal(String email) {
        User user = new User();
        user.setEmail(email);
        return new UsernamePasswordAuthenticationToken(user, null, List.of());
    }

    private Trip tripWithGroup(UUID id, Group group) {
        Trip trip = new Trip();
        trip.setId(id);
        trip.setTripGroup(group);
        return trip;
    }

    @Test
    void isMemberOfTrip_shouldUseEmailFromUserPrincipal() {
        UUID id = UUID.randomUUID();
        Group group = new Group();
        when(tripRepository.findById(id)).thenReturn(Optional.of(tripWithGroup(id, group)));
        when(groupUserService.isUserMemberOfGroup(group, "a@example.com")).thenReturn(true);

        assertTrue(evaluator.isMemberOfTrip(id, authWithUserPrincipal("a@example.com")));
    }

    @Test
    void isMemberOfTrip_shouldReturnFalse_whenUserNotInGroup() {
        UUID id = UUID.randomUUID();
        Group group = new Group();
        when(tripRepository.findById(id)).thenReturn(Optional.of(tripWithGroup(id, group)));
        when(groupUserService.isUserMemberOfGroup(group, "a@example.com")).thenReturn(false);

        assertFalse(evaluator.isMemberOfTrip(id, authWithUserPrincipal("a@example.com")));
    }

    @Test
    void hasRoleInTrip_shouldReturnFalse_whenUserIsOnlyMember() {
        UUID id = UUID.randomUUID();
        Group group = new Group();
        when(tripRepository.findById(id)).thenReturn(Optional.of(tripWithGroup(id, group)));
        when(groupUserService.isUserOwnerOrAdminOfGroup(group, "a@example.com")).thenReturn(false);

        assertFalse(evaluator.hasRoleInTrip(id, authWithUserPrincipal("a@example.com")));
    }

    @Test
    void shouldThrowTripNotFound_whenTripDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(tripRepository.findById(id)).thenReturn(Optional.empty());

        Authentication auth = authWithUserPrincipal("a@example.com");

        assertThrows(TripNotFoundException.class, () -> evaluator.hasRoleInTrip(id, auth));
        assertThrows(TripNotFoundException.class, () -> evaluator.isMemberOfTrip(id, auth));
    }

    @Test
    void shouldReturnFalse_whenAuthenticationIsNullOrNotAuthenticated() {
        UUID id = UUID.randomUUID();
        Authentication unauthenticated = new UsernamePasswordAuthenticationToken("x", "y");

        assertFalse(evaluator.isMemberOfTrip(id, null));
        assertFalse(evaluator.hasRoleInTrip(id, unauthenticated));
        verifyNoInteractions(tripRepository, groupUserService);
    }
}

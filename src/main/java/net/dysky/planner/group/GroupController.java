package net.dysky.planner.group;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import net.dysky.planner.auth.JwtService;
import net.dysky.planner.response.ResponseDTO;
import net.dysky.planner.trip.Trip;
import net.dysky.planner.trip.TripService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/trips/{id}/group")
class GroupController {

    private final TripService tripService;
    private final GroupService groupService;
    private final JwtService jwtService;

    @PostMapping("/members")
    public ResponseEntity<ResponseDTO> addToGroup(@PathVariable UUID id, @RequestBody AddToGroupDTO addToGroupDTO, HttpServletRequest request) {
        Group group = tripService.getTripById(id).getTripGroup();
        String email = jwtService.extractEmail(request);

        groupService.addToGroup(group, addToGroupDTO, email, id);

        return ResponseEntity.ok(
                new ResponseDTO(
                        LocalDateTime.now(),
                        200,
                        "User added to group successfully",
                        "/api/trips/" + id + "/group/members",
                        null
                )
        );
    }

    @PutMapping("/members")
    public ResponseEntity<ResponseDTO> updateGroupMember(@PathVariable UUID id, @RequestBody List<UpdateGroupMemberDTO> updateGroupMemberDTO) {
        Trip trip = tripService.getTripById(id);

        groupService.updateGroupMember(trip, trip.getTripGroup(), updateGroupMemberDTO);

        return ResponseEntity.ok(
                new ResponseDTO(
                        LocalDateTime.now(),
                        200,
                        "Group information updated successfully",
                        "/api/trips/" + id + "/group/members",
                        null
                )
        );
    }

    @PutMapping
    public ResponseEntity<ResponseDTO> updateGroupName(@PathVariable UUID id, @RequestBody CreateGroupDTO createGroupDTO) {
        Trip trip = tripService.getTripById(id);

        groupService.updateGroupName(trip,createGroupDTO);

        return ResponseEntity.ok(
                new ResponseDTO(
                        LocalDateTime.now(),
                        200,
                        "Group name updated successfully",
                        "/api/trips/" + id + "/group",
                        null
                )
        );
    }

    @DeleteMapping("/members")
    public ResponseEntity<ResponseDTO> removeFromGroup(@PathVariable UUID id, @RequestBody RemoveFromGroupDTO removeFromGroupDTO, HttpServletRequest request) {
        Group group = tripService.getTripById(id).getTripGroup();
        String email = jwtService.extractEmail(request);

        groupService.deleteFromGroup(group, removeFromGroupDTO, email, id);

        return ResponseEntity.ok(
                new ResponseDTO(
                        LocalDateTime.now(),
                        200,
                        "User removed from group successfully",
                        "/api/trips/" + id + "/group/members",
                        null
                )
        );
    }

}

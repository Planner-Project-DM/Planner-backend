package net.dysky.planner.group;

import lombok.RequiredArgsConstructor;
import net.dysky.planner.response.ResponseDTO;
import net.dysky.planner.trip.TripService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/trips/{id}/group")
class GroupController {

    private final TripService tripService;
    private final GroupService groupService;

    @PostMapping("/members")
    public ResponseEntity<ResponseDTO> addToGroup(@PathVariable UUID id, @RequestBody AddToGroupDTO addToGroupDTO) {
        Group group = tripService.getTripById(id).getTripGroup();

        groupService.addToGroup(group, addToGroupDTO);

        return ResponseEntity.ok(new ResponseDTO(LocalDateTime.now(), 200, "/api/groups/add", "User added to group successfully", null));
    }

    @DeleteMapping("/members")
    public ResponseEntity<ResponseDTO> removeFromGroup(@PathVariable UUID id, @RequestBody RemoveFromGroupDTO removeFromGroupDTO) {
        Group group = tripService.getTripById(id).getTripGroup();

        groupService.deleteFromGroup(group, removeFromGroupDTO);

        return ResponseEntity.ok(new ResponseDTO(LocalDateTime.now(), 200, "/api/groups/remove", "User removed from group successfully", null));
    }

}

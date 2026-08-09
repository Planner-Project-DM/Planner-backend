package net.dysky.planner.group;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import net.dysky.planner.auth.JwtService;
import net.dysky.planner.response.ResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/trips/{id}/group")
class GroupController {

    private final GroupService groupService;

    private final JwtService jwtService;

    @PostMapping("/create")
    public ResponseEntity<ResponseDTO> createGroup(@PathVariable UUID id, @RequestBody CreateGroupDTO createGroupDTO, HttpServletRequest request) {
        String email = jwtService.extractEmail(request);

        Group group = groupService.createGroup(id, createGroupDTO, email);

        return ResponseEntity.ok(new ResponseDTO(LocalDateTime.now(), 200, "/api/groups/create", "Group created successfully", group));
    }

    @PostMapping("/add")
    public ResponseEntity<ResponseDTO> addToGroup(@PathVariable UUID id, @RequestBody AddToGroupDTO addToGroupDTO, HttpServletRequest request) {
        String email = jwtService.extractEmail(request);

        groupService.addToGroup(id, addToGroupDTO, email);

        return ResponseEntity.ok(new ResponseDTO(LocalDateTime.now(), 200, "/api/groups/add", "User added to group successfully", null));
    }

    @DeleteMapping("/remove-user")
    public ResponseEntity<ResponseDTO> removeFromGroup(@PathVariable UUID id, @RequestBody RemoveFromGroupDTO removeFromGroupDTO, HttpServletRequest request) {
        String email = jwtService.extractEmail(request);

        groupService.deleteFromGroup(id, removeFromGroupDTO, email);

        return ResponseEntity.ok(new ResponseDTO(LocalDateTime.now(), 200, "/api/groups/remove", "User removed from group successfully", null));
    }

}

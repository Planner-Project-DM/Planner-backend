package net.dysky.planner.group;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import net.dysky.planner.auth.JwtService;
import net.dysky.planner.response.ResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/groups")
class GroupController {

    private final GroupService groupService;

    private final JwtService jwtService;

    @PostMapping("/create")
    public ResponseEntity<ResponseDTO> createGroup(@RequestBody CreateGroupDTO createGroupDTO, HttpServletRequest request) {
        String email = jwtService.extractEmail(request);

        Group group = groupService.createGroup(createGroupDTO, email);

        return ResponseEntity.ok(new ResponseDTO(LocalDateTime.now(), 200, "/api/groups/create", "Group created successfully", group));
    }

    @PostMapping("/add")
    public ResponseEntity<ResponseDTO> addToGroup(@RequestBody AddToGroupDTO addToGroupDTO, HttpServletRequest request) {
        String email = jwtService.extractEmail(request);

        groupService.addToGroup(addToGroupDTO, email);

        return ResponseEntity.ok(new ResponseDTO(LocalDateTime.now(), 200, "/api/groups/add", "User added to group successfully", null));
    }
}

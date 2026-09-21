package app.controller;

import app.entity.User;
import app.entity.Workspace;
import app.entity.WorkspaceMember;
import app.entity.Channel;
import app.entity.ChannelMember;
import app.repository.UserRepository;
import app.repository.WorkspaceMemberRepository;
import app.repository.WorkspaceRepository;
import app.repository.ChannelRepository;
import app.repository.ChannelMemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/workspaces")
@RequiredArgsConstructor
public class WorkspaceController {

    private final WorkspaceRepository workspaceRepository;
    private final UserRepository userRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final ChannelRepository channelRepository;
    private final ChannelMemberRepository channelMemberRepository;

    // GET - Retrieve workspaces for a user
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<UserWorkspaceResponse>> getUserWorkspaces(@PathVariable Long userId) {
        List<UserWorkspaceResponse> workspaces = workspaceMemberRepository.findByUserId(userId)
                .stream()
                .map(member -> new UserWorkspaceResponse(
                        member.getWorkspace().getId(),
                        member.getWorkspace().getName(),
                        member.getEngagementScore()
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(workspaces);
    }

    @Data
    public static class UserWorkspaceResponse {
        private final Long id;
        private final String name;
        private final Integer engagementScore;
    }

    // POST - Create a workspace and automatically add the user as a member
    @PostMapping("/user/{userId}")
    public ResponseEntity<Workspace> createWorkspaceForUser(@PathVariable Long userId, @RequestBody Workspace workspace) {
        return userRepository.findById(userId).map(user -> {
            workspace.setOwner(user);
            Workspace savedWorkspace = workspaceRepository.save(workspace);
            WorkspaceMember member = WorkspaceMember.builder()
                    .workspace(savedWorkspace)
                    .user(user)
                    .rankPosition(1)
                    .build();
            workspaceMemberRepository.save(member);

            // Create # general channel
            Channel generalChannel = Channel.builder()
                    .name("general")
                    .workspace(savedWorkspace)
                    .build();
            Channel savedChannel = channelRepository.save(generalChannel);

            // Add user to # general channel
            ChannelMember cm = ChannelMember.builder()
                    .channel(savedChannel)
                    .user(user)
                    .build();
            channelMemberRepository.save(cm);

            return ResponseEntity.ok(savedWorkspace);
        }).orElse(ResponseEntity.notFound().build());
    }

    // GET - Retrieve workspace rankings
    @GetMapping("/{workspaceId}/rankings")
    public ResponseEntity<List<RankingResponse>> getWorkspaceRankings(@PathVariable Long workspaceId) {
        List<RankingResponse> rankings = workspaceMemberRepository.findByWorkspaceIdOrderByRankPositionAsc(workspaceId)
                .stream()
                .sorted((m1, m2) -> Integer.compare(m2.getEngagementScore(), m1.getEngagementScore()))
                .map(m -> new RankingResponse(m.getUser().getId(), m.getUser().getName(), m.getEngagementScore()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(rankings);
    }

    @Data
    public static class RankingResponse {
        private final Long userId;
        private final String name;
        private final Integer score;
    }

    // GET - Retrieve workspace
    @GetMapping("/{id}")
    public ResponseEntity<Workspace> getWorkspace(@PathVariable Long id) {
        return workspaceRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST - Create workspace
    @PostMapping
    public ResponseEntity<Workspace> createWorkspace(@RequestBody Workspace workspace) {
        Workspace saved = workspaceRepository.save(workspace);
        return ResponseEntity.ok(saved);
    }

    // PUT - Replace workspace completely
    @PutMapping("/{id}")
    public ResponseEntity<Workspace> replaceWorkspace(@PathVariable Long id, @RequestBody Workspace workspace) {
        return workspaceRepository.findById(id)
                .map(existing -> {
                    existing.setName(workspace.getName());
                    return ResponseEntity.ok(workspaceRepository.save(existing));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // PATCH - Partially update workspace
    @PatchMapping("/{id}")
    public ResponseEntity<Workspace> updateWorkspaceName(@PathVariable Long id, @RequestBody Map<String, String> updates) {
        return workspaceRepository.findById(id)
                .map(existing -> {
                    if (updates.containsKey("name")) {
                        existing.setName(updates.get("name"));
                    }
                    return ResponseEntity.ok(workspaceRepository.save(existing));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // DELETE - Remove workspace
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWorkspace(@PathVariable Long id) {
        if (workspaceRepository.existsById(id)) {
            workspaceRepository.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    // HEAD - Check if workspace exists
    @RequestMapping(value = "/{id}", method = RequestMethod.HEAD)
    public ResponseEntity<Void> checkWorkspaceExists(@PathVariable Long id) {
        if (workspaceRepository.existsById(id)) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    private String generateJoinCode(Long workspaceId, long timeWindow) {
        String raw = workspaceId + "-SecretSalt-" + timeWindow;
        int code = Math.abs(raw.hashCode()) % 1000000;
        return String.format("%06d", code);
    }

    // GET - Retrieve current join code (Admin only)
    @GetMapping("/{workspaceId}/join-code")
    public ResponseEntity<Map<String, String>> getJoinCode(@PathVariable Long workspaceId, @RequestParam Long userId) {
        return workspaceRepository.findById(workspaceId).map(workspace -> {
            if (!workspace.getOwner().getId().equals(userId)) {
                return ResponseEntity.status(403).<Map<String, String>>build();
            }
            long timeWindow = System.currentTimeMillis() / 20000;
            String code = generateJoinCode(workspaceId, timeWindow);
            return ResponseEntity.ok(Map.of("code", code));
        }).orElse(ResponseEntity.notFound().build());
    }

    // POST - Join workspace using ID and Code
    @PostMapping("/{workspaceId}/join")
    public ResponseEntity<?> joinWorkspace(@PathVariable Long workspaceId, @RequestBody JoinRequest request) {
        return workspaceRepository.findById(workspaceId).map(workspace -> {
            // Check join code
            long currentWindow = System.currentTimeMillis() / 20000;
            boolean valid = false;
            for (int i = -1; i <= 1; i++) {
                if (generateJoinCode(workspaceId, currentWindow + i).equals(request.getCode())) {
                    valid = true;
                    break;
                }
            }
            if (!valid) {
                return ResponseEntity.status(400).body("Invalid or expired join code");
            }

            // Get User
            User user = userRepository.findById(request.getUserId()).orElse(null);
            if (user == null) {
                return ResponseEntity.status(404).body("User not found");
            }

            // Check company matches
            if (user.getCompany() == null || workspace.getOwner().getCompany() == null ||
                !user.getCompany().equalsIgnoreCase(workspace.getOwner().getCompany())) {
                return ResponseEntity.status(403).body("You must belong to the same company to join this workspace");
            }

            // Check if already a member
            if (workspaceMemberRepository.findByWorkspaceId(workspaceId).stream()
                    .anyMatch(m -> m.getUser().getId().equals(user.getId()))) {
                return ResponseEntity.status(400).body("Already a member of this workspace");
            }

            // Add to workspace
            WorkspaceMember member = WorkspaceMember.builder()
                    .workspace(workspace)
                    .user(user)
                    .rankPosition(workspaceMemberRepository.findByWorkspaceId(workspaceId).size() + 1)
                    .build();
            workspaceMemberRepository.save(member);

            // Add to #general channel
            channelRepository.findByWorkspaceId(workspaceId).stream()
                    .filter(c -> "general".equalsIgnoreCase(c.getName()))
                    .findFirst()
                    .ifPresent(generalChannel -> {
                        ChannelMember cm = ChannelMember.builder()
                                .channel(generalChannel)
                                .user(user)
                                .build();
                        channelMemberRepository.save(cm);
                    });

            return ResponseEntity.ok(workspace);
        }).orElse(ResponseEntity.status(404).body("Workspace not found"));
    }

    @Data
    public static class JoinRequest {
        private Long userId;
        private String code;
    }
}

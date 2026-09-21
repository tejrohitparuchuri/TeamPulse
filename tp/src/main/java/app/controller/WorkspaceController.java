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
    public ResponseEntity<List<Workspace>> getUserWorkspaces(@PathVariable Long userId) {
        List<Workspace> workspaces = workspaceMemberRepository.findByUserId(userId)
                .stream()
                .map(WorkspaceMember::getWorkspace)
                .collect(Collectors.toList());
        return ResponseEntity.ok(workspaces);
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
}

package app.controller;

import app.entity.Channel;
import app.entity.ChannelMember;
import app.entity.User;
import app.entity.Workspace;
import app.repository.ChannelRepository;
import app.repository.ChannelMemberRepository;
import app.repository.UserRepository;
import app.repository.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/channels")
@RequiredArgsConstructor
public class ChannelController {

    private final ChannelRepository channelRepository;
    private final ChannelMemberRepository channelMemberRepository;
    private final WorkspaceRepository workspaceRepository;
    private final UserRepository userRepository;

    @GetMapping("/workspace/{workspaceId}")
    public ResponseEntity<List<Channel>> getWorkspaceChannels(@PathVariable Long workspaceId, @RequestParam(required = false) Long userId) {
        List<Channel> channels = channelRepository.findByWorkspaceId(workspaceId);
        
        // If a userId is provided, we filter the channels to only those the user is a member of (unless they are the owner)
        if (userId != null) {
            Workspace workspace = workspaceRepository.findById(workspaceId).orElse(null);
            if (workspace != null && !workspace.getOwner().getId().equals(userId)) {
                List<Long> memberChannelIds = channelMemberRepository.findByUserId(userId).stream()
                        .map(cm -> cm.getChannel().getId())
                        .collect(Collectors.toList());
                channels = channels.stream()
                        .filter(c -> memberChannelIds.contains(c.getId()))
                        .collect(Collectors.toList());
            }
        }
        
        return ResponseEntity.ok(channels);
    }

    // POST - Create channel (Admin only)
    @PostMapping("/workspace/{workspaceId}")
    public ResponseEntity<?> createChannel(@PathVariable Long workspaceId, @RequestBody CreateChannelRequest request) {
        return workspaceRepository.findById(workspaceId).map(workspace -> {
            if (!workspace.getOwner().getId().equals(request.getAdminId())) {
                return ResponseEntity.status(403).body("Only workspace owner can create channels");
            }

            Channel channel = Channel.builder()
                    .name(request.getName())
                    .workspace(workspace)
                    .build();
            Channel savedChannel = channelRepository.save(channel);

            // Add owner to channel automatically
            ChannelMember cm = ChannelMember.builder()
                    .channel(savedChannel)
                    .user(workspace.getOwner())
                    .build();
            channelMemberRepository.save(cm);

            return ResponseEntity.ok(savedChannel);
        }).orElse(ResponseEntity.status(404).body("Workspace not found"));
    }

    // POST - Add user to channel (Admin only)
    @PostMapping("/{channelId}/members")
    public ResponseEntity<?> addMemberToChannel(@PathVariable Long channelId, @RequestBody AddMemberRequest request) {
        return channelRepository.findById(channelId).map(channel -> {
            Workspace workspace = channel.getWorkspace();
            if (!workspace.getOwner().getId().equals(request.getAdminId())) {
                return ResponseEntity.status(403).body("Only workspace owner can add members to channels");
            }

            User user = userRepository.findById(request.getUserId()).orElse(null);
            if (user == null) return ResponseEntity.status(404).body("User not found");

            // Check if already in channel
            boolean alreadyMember = channelMemberRepository.findByChannelId(channelId).stream()
                    .anyMatch(cm -> cm.getUser().getId().equals(user.getId()));
            
            if (alreadyMember) {
                return ResponseEntity.status(400).body("User is already in this channel");
            }

            ChannelMember cm = ChannelMember.builder()
                    .channel(channel)
                    .user(user)
                    .build();
            channelMemberRepository.save(cm);

            return ResponseEntity.ok("User added to channel successfully");
        }).orElse(ResponseEntity.status(404).body("Channel not found"));
    }

    // GET - Members of a channel
    @GetMapping("/{channelId}/members")
    public ResponseEntity<List<User>> getChannelMembers(@PathVariable Long channelId) {
        List<User> users = channelMemberRepository.findByChannelId(channelId).stream()
                .map(ChannelMember::getUser)
                .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }

    @Data
    public static class CreateChannelRequest {
        private String name;
        private Long adminId;
    }

    @Data
    public static class AddMemberRequest {
        private Long userId;
        private Long adminId;
    }
}

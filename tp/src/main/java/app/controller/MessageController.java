package app.controller;

import app.entity.Channel;
import app.entity.Message;
import app.entity.User;
import app.repository.ChannelRepository;
import app.repository.MessageRepository;
import app.repository.UserRepository;
import app.repository.WorkspaceMemberRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageRepository messageRepository;
    private final ChannelRepository channelRepository;
    private final UserRepository userRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;

    @GetMapping("/channel/{channelId}")
    public ResponseEntity<List<MessageResponse>> getChannelMessages(@PathVariable Long channelId) {
        List<MessageResponse> responses = messageRepository.findByChannelIdOrderByTimestampAsc(channelId)
                .stream()
                .map(msg -> new MessageResponse(
                        msg.getId(),
                        msg.getContent(),
                        msg.getUser().getId(),
                        msg.getUser().getName(),
                        msg.getTimestamp()
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @PostMapping
    public ResponseEntity<MessageResponse> sendMessage(@RequestBody MessageRequest request) {
        return channelRepository.findById(request.getChannelId()).flatMap(channel ->
                userRepository.findById(request.getUserId()).map(user -> {
                    Message message = Message.builder()
                            .content(request.getContent())
                            .channel(channel)
                            .user(user)
                            .timestamp(LocalDateTime.now())
                            .build();
                    Message saved = messageRepository.save(message);

                    // Increase engagement score
                    workspaceMemberRepository.findByWorkspaceIdAndUserId(channel.getWorkspace().getId(), user.getId())
                            .ifPresent(wm -> {
                                wm.setEngagementScore(wm.getEngagementScore() + 10);
                                workspaceMemberRepository.save(wm);
                            });

                    return ResponseEntity.ok(new MessageResponse(
                            saved.getId(),
                            saved.getContent(),
                            saved.getUser().getId(),
                            saved.getUser().getName(),
                            saved.getTimestamp()
                    ));
                })
        ).orElse(ResponseEntity.badRequest().build());
    }

    @Data
    public static class MessageRequest {
        private String content;
        private Long channelId;
        private Long userId;
    }

    @Data
    public static class MessageResponse {
        private final Long id;
        private final String content;
        private final Long userId;
        private final String userName;
        private final LocalDateTime timestamp;
    }
}

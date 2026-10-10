package app.controller;

import app.entity.Draft;
import app.repository.ChannelRepository;
import app.repository.DraftRepository;
import app.repository.UserRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/drafts")
@RequiredArgsConstructor
public class DraftController {

    private final DraftRepository draftRepository;
    private final UserRepository userRepository;
    private final ChannelRepository channelRepository;

    @PostMapping
    public ResponseEntity<Void> saveDraft(@RequestBody DraftRequest request) {
        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            draftRepository.findByUserIdAndChannelId(request.getUserId(), request.getChannelId())
                    .ifPresent(draftRepository::delete);
            return ResponseEntity.ok().build();
        }

        return userRepository.findById(request.getUserId()).flatMap(user ->
                channelRepository.findById(request.getChannelId()).map(channel -> {
                    Draft draft = draftRepository.findByUserIdAndChannelId(request.getUserId(), request.getChannelId())
                            .orElse(Draft.builder().user(user).channel(channel).build());
                    draft.setContent(request.getContent());
                    draft.setUpdatedAt(LocalDateTime.now());
                    draftRepository.save(draft);
                    return ResponseEntity.ok().<Void>build();
                })
        ).orElse(ResponseEntity.badRequest().build());
    }

    @GetMapping("/channel/{channelId}")
    public ResponseEntity<DraftResponse> getDraft(@PathVariable Long channelId, @RequestParam Long userId) {
        return draftRepository.findByUserIdAndChannelId(userId, channelId)
                .map(draft -> ResponseEntity.ok(new DraftResponse(draft.getContent())))
                .orElse(ResponseEntity.ok(new DraftResponse("")));
    }

    @DeleteMapping("/channel/{channelId}")
    public ResponseEntity<Void> clearDraft(@PathVariable Long channelId, @RequestParam Long userId) {
        draftRepository.findByUserIdAndChannelId(userId, channelId).ifPresent(draftRepository::delete);
        return ResponseEntity.ok().build();
    }

    @Data
    public static class DraftRequest {
        private Long userId;
        private Long channelId;
        private String content;
    }

    @Data
    public static class DraftResponse {
        private final String content;
    }
}

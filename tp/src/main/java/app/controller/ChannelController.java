package app.controller;

import app.entity.Channel;
import app.repository.ChannelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/channels")
@RequiredArgsConstructor
public class ChannelController {

    private final ChannelRepository channelRepository;

    @GetMapping("/workspace/{workspaceId}")
    public ResponseEntity<List<Channel>> getWorkspaceChannels(@PathVariable Long workspaceId) {
        List<Channel> channels = channelRepository.findByWorkspaceId(workspaceId);
        return ResponseEntity.ok(channels);
    }
}

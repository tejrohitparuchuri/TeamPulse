package app.controller;

import app.entity.User;
import app.entity.WorkspaceMember;
import app.repository.UserRepository;
import app.repository.WorkspaceRepository;
import app.repository.WorkspaceMemberRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import app.entity.Channel;
import app.entity.ChannelMember;
import app.repository.ChannelRepository;
import app.repository.ChannelMemberRepository;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final ChannelRepository channelRepository;
    private final ChannelMemberRepository channelMemberRepository;

    @GetMapping("/check-username")
    public ResponseEntity<Boolean> checkUsername(@RequestParam String username) {
        return ResponseEntity.ok(userRepository.existsByUsername(username));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            return ResponseEntity.badRequest().body("Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().body("Email already exists");
        }

        User user = User.builder()
                .email(request.getEmail())
                .username(request.getUsername())
                .password(request.getPassword()) // In a real app, hash this!
                .name(request.getName())
                .age(request.getAge())
                .gender(request.getGender())
                .company(request.getCompany())
                .position(request.getPosition())
                .build();

        User savedUser = userRepository.save(user);

        // Auto-join company workspace if it exists
        if (request.getCompany() != null && !request.getCompany().isEmpty()) {
            workspaceRepository.findByName(request.getCompany()).ifPresent(workspace -> {
                WorkspaceMember member = WorkspaceMember.builder()
                        .workspace(workspace)
                        .user(savedUser)
                        .rankPosition(1)
                        .build();
                workspaceMemberRepository.save(member);

                channelRepository.findByWorkspaceId(workspace.getId()).stream()
                        .findFirst()
                        .ifPresent(channel -> {
                            ChannelMember cm = ChannelMember.builder()
                                    .channel(channel)
                                    .user(savedUser)
                                    .build();
                            channelMemberRepository.save(cm);
                        });
            });
        }

        return ResponseEntity.ok("Registration successful");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        Optional<User> userOpt = userRepository.findByUsername(request.getUsername());
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            // In a real app, use password encoder
            if (user.getPassword().equals(request.getPassword())) {
                return ResponseEntity.ok(new LoginResponse(user.getId(), user.getName()));
            }
        }
        return ResponseEntity.status(401).body("Invalid username or password");
    }

    @Data
    public static class LoginResponse {
        private final Long userId;
        private final String name;
    }

    @Data
    public static class RegisterRequest {
        private String email;
        private String username;
        private String password;
        private String name;
        private Integer age;
        private String gender;
        private String company;
        private String position;
    }

    @Data
    public static class LoginRequest {
        private String username;
        private String password;
    }
}

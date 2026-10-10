package app.controller;

import app.entity.User;
import app.repository.UserRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    @GetMapping("/{id}")
    public ResponseEntity<UserProfileDto> getUserProfile(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(user -> ResponseEntity.ok(new UserProfileDto(user)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<UserProfileDto> updateUserStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return userRepository.findById(id).map(user -> {
            if (body.containsKey("status")) {
                user.setStatus(body.get("status"));
                userRepository.save(user);
            }
            return ResponseEntity.ok(new UserProfileDto(user));
        }).orElse(ResponseEntity.notFound().build());
    }

    @Data
    public static class UserProfileDto {
        private Long id;
        private String email;
        private String username;
        private String name;
        private Integer age;
        private String gender;
        private String company;
        private String position;
        private String status;
        private String profilePicture;

        public UserProfileDto(User user) {
            this.id = user.getId();
            this.email = user.getEmail();
            this.username = user.getUsername();
            this.name = user.getName();
            this.age = user.getAge();
            this.gender = user.getGender();
            this.company = user.getCompany();
            this.position = user.getPosition();
            this.status = user.getStatus();
            this.profilePicture = user.getProfilePicture();
        }
    }
}

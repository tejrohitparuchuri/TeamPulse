package app.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "engagement_events")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EngagementEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "workspace_member_id", nullable = false)
    private WorkspaceMember workspaceMember;

    @Column(nullable = false)
    private String eventType; // e.g. MESSAGE_SENT, REPLY, REACTION, HUDDLE

    @Column(nullable = false)
    private Integer weight;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
}

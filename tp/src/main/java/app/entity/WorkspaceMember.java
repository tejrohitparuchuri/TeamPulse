package app.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "workspace_members")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkspaceMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspace workspace;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "engagement_score", nullable = false)
    @Builder.Default
    private Integer engagementScore = 0;

    @Column(name = "rank_position")
    private Integer rankPosition;
}

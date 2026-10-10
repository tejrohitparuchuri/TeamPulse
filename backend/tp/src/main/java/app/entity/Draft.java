package app.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "drafts", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "channel_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Draft {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String content;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "channel_id", nullable = false)
    private Channel channel;

    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();
}

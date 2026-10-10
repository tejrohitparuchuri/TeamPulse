package app.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "channel_members")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChannelMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "channel_id", nullable = false)
    private Channel channel;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}

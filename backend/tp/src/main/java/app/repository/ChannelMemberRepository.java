package app.repository;

import app.entity.ChannelMember;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ChannelMemberRepository extends JpaRepository<ChannelMember, Long> {
    List<ChannelMember> findByUserId(Long userId);
    List<ChannelMember> findByChannelId(Long channelId);
}

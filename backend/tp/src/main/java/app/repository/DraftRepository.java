package app.repository;

import app.entity.Draft;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DraftRepository extends JpaRepository<Draft, Long> {
    Optional<Draft> findByUserIdAndChannelId(Long userId, Long channelId);
}

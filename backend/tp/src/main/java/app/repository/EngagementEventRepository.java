package app.repository;

import app.entity.EngagementEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EngagementEventRepository extends JpaRepository<EngagementEvent, Long> {
    List<EngagementEvent> findByWorkspaceMemberId(Long workspaceMemberId);
}

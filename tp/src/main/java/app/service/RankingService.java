package app.service;

import app.entity.EngagementEvent;
import app.entity.WorkspaceMember;
import app.repository.EngagementEventRepository;
import app.repository.WorkspaceMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RankingService {

    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final EngagementEventRepository engagementEventRepository;

    @Transactional
    public void recordEvent(Long workspaceId, Long userId, String eventType, Integer weight) {
        WorkspaceMember member = workspaceMemberRepository.findByWorkspaceIdAndUserId(workspaceId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Workspace member not found"));

        // Record the event
        EngagementEvent event = EngagementEvent.builder()
                .workspaceMember(member)
                .eventType(eventType)
                .weight(weight)
                .build();
        engagementEventRepository.save(event);

        // Update score
        member.setEngagementScore(member.getEngagementScore() + weight);
        workspaceMemberRepository.save(member);

        // Recalculate ranks
        recalculateRanks(workspaceId);
    }

    private void recalculateRanks(Long workspaceId) {
        List<WorkspaceMember> members = workspaceMemberRepository.findByWorkspaceIdOrderByRankPositionAsc(workspaceId);
        
        // Sort by score descending
        members.sort(Comparator.comparing(WorkspaceMember::getEngagementScore).reversed());

        int rank = 1;
        for (WorkspaceMember m : members) {
            m.setRankPosition(rank++);
        }
        
        workspaceMemberRepository.saveAll(members);
    }
}

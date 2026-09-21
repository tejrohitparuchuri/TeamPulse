package app.component;

import app.entity.Role;
import app.entity.User;
import app.entity.Workspace;
import app.entity.WorkspaceMember;
import app.entity.Channel;
import app.entity.ChannelMember;
import app.repository.UserRepository;
import app.repository.WorkspaceMemberRepository;
import app.repository.WorkspaceRepository;
import app.repository.ChannelRepository;
import app.repository.ChannelMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final ChannelRepository channelRepository;
    private final ChannelMemberRepository channelMemberRepository;

    @Override
    public void run(String... args) throws Exception {
        seedCompany("company1", "1234561", "Company 1");
        seedCompany("company2", "1234562", "Company 2");
        seedCompany("company4", "1234564", "Company 4");
        seedCompany("company5", "1234565", "Company 5");
        seedCompany("company6", "1234566", "Company 6");
        seedCompany("company7", "1234567", "Company 7");
        seedCompany("company8", "1234568", "Company 8");
        seedCompany("company9", "1234569", "Company 9");
        seedCompany("company10", "12345610", "Company 10");
        seedCompany("teampulse", "teampulse@#me", "teampulse");
    }

    private void seedCompany(String username, String password, String companyName) {
        // 1. Ensure User exists
        User user = userRepository.findByUsername(username)
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .email(username + "@example.com")
                            .username(username)
                            .password(password)
                            .name(username)
                            .company(companyName)
                            .role(Role.ADMIN)
                            .build();
                    return userRepository.save(newUser);
                });

        // 2. Ensure Workspace exists
        Workspace workspace = workspaceRepository.findByName(companyName)
                .orElseGet(() -> {
                    Workspace newWorkspace = Workspace.builder()
                            .name(companyName)
                            .owner(user)
                            .build();
                    return workspaceRepository.save(newWorkspace);
                });

        // 3. Ensure User is a member of the Workspace
        List<WorkspaceMember> memberships = workspaceMemberRepository.findByUserId(user.getId());
        boolean isMember = memberships.stream()
                .anyMatch(member -> member.getWorkspace().getId().equals(workspace.getId()));

        if (!isMember) {
            WorkspaceMember member = WorkspaceMember.builder()
                    .workspace(workspace)
                    .user(user)
                    .rankPosition(1)
                    .build();
            workspaceMemberRepository.save(member);
        }

        // 4. Ensure # general Channel exists
        List<Channel> channels = channelRepository.findByWorkspaceId(workspace.getId());
        Channel generalChannel;
        if (channels.isEmpty()) {
            generalChannel = Channel.builder()
                    .name("general")
                    .workspace(workspace)
                    .build();
            generalChannel = channelRepository.save(generalChannel);
        } else {
            generalChannel = channels.get(0);
        }

        // 5. Ensure User is a member of the Channel
        final Channel finalGeneralChannel = generalChannel;
        List<ChannelMember> channelMemberships = channelMemberRepository.findByUserId(user.getId());
        boolean isChannelMember = channelMemberships.stream()
                .anyMatch(cm -> cm.getChannel().getId().equals(finalGeneralChannel.getId()));

        if (!isChannelMember) {
            ChannelMember cm = ChannelMember.builder()
                    .channel(finalGeneralChannel)
                    .user(user)
                    .build();
            channelMemberRepository.save(cm);
        }
    }
}

package io.github.campbellbartlett.mergecheck;

import com.atlassian.bitbucket.permission.Permission;
import com.atlassian.bitbucket.permission.PermissionService;
import com.atlassian.bitbucket.pull.PullRequest;
import com.atlassian.bitbucket.repository.Repository;
import com.atlassian.bitbucket.user.UserService;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import io.github.campbellbartlett.mergecheck.activeobjects.PullRequestRiskAccepted;
import io.github.campbellbartlett.mergecheck.repository.PullRequestRiskAcceptedRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component("adminRiskAcceptedService")
public class AdminRiskAcceptedService {
    // External components
    private final UserService userService;
    private final PermissionService permissionService;

    // Project components
    private final PullRequestRiskAcceptedRepository pullRequestRiskAcceptedRepository;

    @Autowired
    public AdminRiskAcceptedService(@ComponentImport UserService userService,
                                    @ComponentImport PermissionService permissionService,
                                    PullRequestRiskAcceptedRepository pullRequestRiskAcceptedRepository) {
        this.userService = userService;
        this.permissionService = permissionService;
        this.pullRequestRiskAcceptedRepository = pullRequestRiskAcceptedRepository;
    }

    boolean hasAdminAcceptedRisk(PullRequest pullRequest) {
        Repository repository = pullRequest.getFromRef().getRepository();

        // Find any risk accepted records relating to this PR.
        List<PullRequestRiskAccepted> riskAcceptedForThisPr = pullRequestRiskAcceptedRepository.findAllForPullRequest(pullRequest.getId());

        // Check that the user that created the record is a repo admin. If they are then return true, else false.
        // If there are no records in the list then this stream will resolve to false.
        return riskAcceptedForThisPr.stream()
                .map(record -> userService.getUserBySlug(record.getAuthenticatingUserSlug()))
                .filter(Objects::nonNull)
                .anyMatch(user -> permissionService.hasRepositoryPermission(user, repository, Permission.REPO_ADMIN));
    }
}

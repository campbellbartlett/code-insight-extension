package io.github.campbellbartlett.codeinsightextension.service;

import com.atlassian.bitbucket.auth.AuthenticationContext;
import com.atlassian.bitbucket.permission.Permission;
import com.atlassian.bitbucket.permission.PermissionService;
import com.atlassian.bitbucket.repository.Repository;
import com.atlassian.bitbucket.user.ApplicationUser;
import com.atlassian.bitbucket.user.UserService;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import io.github.campbellbartlett.codeinsightextension.activeobjects.PullRequestRiskAccepted;
import io.github.campbellbartlett.codeinsightextension.repository.PullRequestRiskAcceptedRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Objects;

@Component("adminRiskAcceptedService")
public class AdminRiskAcceptedService {

    // External components
    private final UserService userService;
    private final PermissionService permissionService;
    private final AuthenticationContext authenticationContext;

    // Project components
    private final PullRequestRiskAcceptedRepository pullRequestRiskAcceptedRepository;

    @Autowired
    public AdminRiskAcceptedService(@ComponentImport UserService userService,
                                    @ComponentImport PermissionService permissionService,
                                    @ComponentImport AuthenticationContext authenticationContext,
                                    PullRequestRiskAcceptedRepository pullRequestRiskAcceptedRepository) {
        this.userService = userService;
        this.permissionService = permissionService;
        this.authenticationContext = authenticationContext;
        this.pullRequestRiskAcceptedRepository = pullRequestRiskAcceptedRepository;
    }

    public boolean hasAdminAcceptedRisk(Repository repository, String commitHash) {
        // Find any risk accepted records relating to this PR.
        List<PullRequestRiskAccepted> riskAcceptedForThisPr = pullRequestRiskAcceptedRepository.findAllForPullRequest(repository.getProject().getKey(), repository.getSlug(), commitHash);

        // Check that the user that created the record is a repo admin. If they are then return true, else false.
        // If there are no records in the list then this stream will resolve to false.
        return riskAcceptedForThisPr.stream()
                .map(record -> userService.getUserBySlug(record.getAuthenticatingUserSlug()))
                .filter(Objects::nonNull)
                .anyMatch(user -> permissionService.hasRepositoryPermission(user, repository, Permission.REPO_ADMIN));
    }

    public void createOrUpdateAdminOverrideForCommit(String projectId, String slug, String commitHash, boolean revoke) {
        if (revoke) {
            pullRequestRiskAcceptedRepository.delete(commitHash, slug, projectId);
            return;
        }
        ApplicationUser userAuthorising = authenticationContext.getCurrentUser();
        pullRequestRiskAcceptedRepository.add(commitHash, slug, projectId, userAuthorising.getSlug(), new Date());
    }
}

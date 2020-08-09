package io.github.campbellbartlett.codeinsightextension.service;

import com.atlassian.bitbucket.auth.AuthenticationContext;
import com.atlassian.bitbucket.permission.Permission;
import com.atlassian.bitbucket.permission.PermissionService;
import com.atlassian.bitbucket.project.Project;
import com.atlassian.bitbucket.repository.Repository;
import com.atlassian.bitbucket.user.ApplicationUser;
import com.atlassian.bitbucket.user.UserService;
import io.github.campbellbartlett.codeinsightextension.activeobjects.PullRequestRiskAccepted;
import io.github.campbellbartlett.codeinsightextension.repository.PullRequestRiskAcceptedRepository;
import io.github.campbellbartlett.codeinsightextension.stub.PullRequestRiskAcceptedStub;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AdminRiskAcceptedServiceTest {

    private static final String FOO_KEY = "fooKey";
    private static final String FOO_SLUG = "fooSlug";
    private static final String FOO_HASH = "fooHash";
    private static final String FOO_USER = "fooUser";

    @Mock
    private UserService userService;

    @Mock
    private PermissionService permissionService;

    @Mock
    private AuthenticationContext authenticationContext;

    @Mock
    private PullRequestRiskAcceptedRepository pullRequestRiskAcceptedRepository;

    @InjectMocks
    private AdminRiskAcceptedService adminRiskAcceptedService;

    @Mock
    private ApplicationUser applicationUser;

    private final Repository repository;
    private final Project project;

    public AdminRiskAcceptedServiceTest() {
        repository = mock(Repository.class);
        project = mock(Project.class);

        when(project.getKey()).thenReturn(FOO_KEY);
        when(repository.getProject()).thenReturn(project);
        when(repository.getSlug()).thenReturn(FOO_SLUG);
    }

    @Test
    public void hasAdminAcceptedRiskNoRecordsPresentTest() {
        when(pullRequestRiskAcceptedRepository
                .findAllForPullRequest(FOO_KEY, FOO_SLUG, FOO_HASH))
                .thenReturn(new ArrayList<>());

        boolean acceptedRisk = adminRiskAcceptedService.hasAdminAcceptedRisk(repository, FOO_HASH);

        Assert.assertFalse(
                "When no PullRequestRiskAccepted records exists for a pull request then admin accepted risk should always be false",
                acceptedRisk
        );
    }

    @Test
    public void hasAdminAcceptedRiskNonRepoAdminAcceptsRiskTest() {
        PullRequestRiskAccepted record = new PullRequestRiskAcceptedStub(FOO_USER);

        when(pullRequestRiskAcceptedRepository
                .findAllForPullRequest(FOO_KEY, FOO_SLUG, FOO_HASH))
                .thenReturn(Collections.singletonList(record));

        when(userService.getUserBySlug(FOO_USER)).thenReturn(applicationUser);
        when(permissionService.hasRepositoryPermission(applicationUser, repository, Permission.REPO_ADMIN))
                .thenReturn(false);

        boolean acceptedRisk = adminRiskAcceptedService.hasAdminAcceptedRisk(repository, FOO_HASH);

        Assert.assertFalse(
                "When a PullRequestRiskAccepted record exists for a PR but the authorising user is not a repo admin then admin accepted risk should be false",
                acceptedRisk
        );
    }

    @Test
    public void hasAdminAcceptedRiskRepoAdminAcceptsRiskTest() {
        PullRequestRiskAccepted record = new PullRequestRiskAcceptedStub(FOO_USER);

        when(pullRequestRiskAcceptedRepository
                .findAllForPullRequest(FOO_KEY, FOO_SLUG, FOO_HASH))
                .thenReturn(Collections.singletonList(record));

        when(userService.getUserBySlug(FOO_USER)).thenReturn(applicationUser);
        when(permissionService.hasRepositoryPermission(applicationUser, repository, Permission.REPO_ADMIN))
                .thenReturn(true);

        boolean acceptedRisk = adminRiskAcceptedService.hasAdminAcceptedRisk(repository, FOO_HASH);

        Assert.assertTrue(
                "When a PullRequestRiskAccepted record exists for a PR and the authorising user is a repo admin then admin accepted risk should be true",
                acceptedRisk
        );
    }

    @Test
    public void hasAdminAcceptedRiskAuthorisingUserNotFoundTest() {
        PullRequestRiskAccepted record = new PullRequestRiskAcceptedStub(FOO_USER);

        when(pullRequestRiskAcceptedRepository
                .findAllForPullRequest(FOO_KEY, FOO_SLUG, FOO_HASH))
                .thenReturn(Collections.singletonList(record));

        when(userService.getUserBySlug(FOO_USER)).thenReturn(null);

        boolean acceptedRisk = adminRiskAcceptedService.hasAdminAcceptedRisk(repository, FOO_HASH);

        Assert.assertFalse(
                "When a PullRequestRiskAccepted record exists for a PR but the authorising user is not found then accepted risk should be false",
                acceptedRisk
        );
    }

    @Test
    public void revokeExistingAdminOverrideTest() {
        adminRiskAcceptedService.createOrUpdateAdminOverrideForCommit(FOO_KEY, FOO_SLUG, FOO_HASH, true);

        verify(pullRequestRiskAcceptedRepository, times(1)).delete(FOO_HASH, FOO_SLUG, FOO_KEY);
    }

    @Test
    public void createExistingAdminOverrideTest() {
        when(authenticationContext.getCurrentUser()).thenReturn(applicationUser);
        when(applicationUser.getSlug()).thenReturn(FOO_USER);
        adminRiskAcceptedService.createOrUpdateAdminOverrideForCommit(FOO_KEY, FOO_SLUG, FOO_HASH, false);

        verify(pullRequestRiskAcceptedRepository, never()).delete(FOO_HASH, FOO_SLUG, FOO_KEY);
        verify(pullRequestRiskAcceptedRepository, times(1)).add(eq(FOO_HASH), eq(FOO_SLUG), eq(FOO_KEY), eq(FOO_USER), any(Date.class));
    }
}

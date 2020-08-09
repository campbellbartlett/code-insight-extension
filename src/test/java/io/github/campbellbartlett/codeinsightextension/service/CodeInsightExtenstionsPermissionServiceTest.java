package io.github.campbellbartlett.codeinsightextension.service;

import com.atlassian.bitbucket.auth.AuthenticationContext;
import com.atlassian.bitbucket.permission.Permission;
import com.atlassian.bitbucket.permission.PermissionService;
import com.atlassian.bitbucket.repository.Repository;
import com.atlassian.bitbucket.repository.RepositoryService;
import com.atlassian.bitbucket.user.ApplicationUser;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CodeInsightExtenstionsPermissionServiceTest {

    @Mock
    private PermissionService permissionService;

    @Mock
    private RepositoryService repositoryService;

    @Mock
    @SuppressWarnings("unused")
    private AuthenticationContext authenticationContext;

    @InjectMocks
    private CodeInsightExtensionsPermissionService codeInsightExtensionsPermissionService;

    @Mock
    Repository repository;

    @Mock
    ApplicationUser applicationUser;

    @Test
    public void doesUserHaveViewPermissionRepositoryNotFoundTest() {
        when(repositoryService.getBySlug(any(), any())).thenReturn(null);

        boolean permission = codeInsightExtensionsPermissionService.doesUserHaveRepoViewPermission("foo", "bar");

        assertFalse("When repository is not found then view permissions should be false.", permission);
    }

    @Test
    public void doesUserHaveAdminPermissionRepositoryNotFoundTest() {
        when(repositoryService.getBySlug(any(), any())).thenReturn(null);

        boolean permission = codeInsightExtensionsPermissionService.doesUserHaveRepoAdminPermission("foo", "bar");

        assertFalse("When repository is not found then admin permissions should be false.", permission);
    }

    @Test
    public void doesUserHaveViewPermissionRepositoryExistsButNotPermittedTest() {
        when(authenticationContext.getCurrentUser()).thenReturn(applicationUser);
        when(repositoryService.getBySlug("foo", "bar")).thenReturn(repository);
        when(permissionService.hasRepositoryPermission(applicationUser, repository, Permission.REPO_READ)).thenReturn(false);

        boolean permission = codeInsightExtensionsPermissionService.doesUserHaveRepoViewPermission("foo", "bar");

        assertFalse("When repository is found but user does not have read permissions then view permissions should be false.", permission);
    }

    @Test
    public void doesUserHaveViewPermissionRepositoryExistsIsPermittedTest() {
        when(authenticationContext.getCurrentUser()).thenReturn(applicationUser);
        when(repositoryService.getBySlug("foo", "bar")).thenReturn(repository);
        when(permissionService.hasRepositoryPermission(applicationUser, repository, Permission.REPO_READ)).thenReturn(true);

        boolean permission = codeInsightExtensionsPermissionService.doesUserHaveRepoViewPermission("foo", "bar");

        assertTrue("When repository is found and user has read permissions then view permissions should be true.", permission);
    }

    @Test
    public void doesUserHaveAdminPermissionRepositoryExistsButNotPermittedTest() {
        when(authenticationContext.getCurrentUser()).thenReturn(applicationUser);
        when(repositoryService.getBySlug("foo", "bar")).thenReturn(repository);
        when(permissionService.hasRepositoryPermission(applicationUser, repository, Permission.REPO_ADMIN)).thenReturn(false);

        boolean permission = codeInsightExtensionsPermissionService.doesUserHaveRepoAdminPermission("foo", "bar");

        assertFalse("When repository is found but user does not have read permissions then view permissions should be false.", permission);
    }

    @Test
    public void doesUserHaveAdminPermissionRepositoryExistsIsPermittedTest() {
        when(authenticationContext.getCurrentUser()).thenReturn(applicationUser);
        when(repositoryService.getBySlug("foo", "bar")).thenReturn(repository);
        when(permissionService.hasRepositoryPermission(applicationUser, repository, Permission.REPO_ADMIN)).thenReturn(true);

        boolean permission = codeInsightExtensionsPermissionService.doesUserHaveRepoAdminPermission("foo", "bar");

        assertTrue("When repository is found and user has read permissions then view permissions should be true.", permission);
    }
}

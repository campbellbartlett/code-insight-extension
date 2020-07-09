package io.github.campbellbartlett.codeinsightextension.service;

import com.atlassian.bitbucket.auth.AuthenticationContext;
import com.atlassian.bitbucket.permission.Permission;
import com.atlassian.bitbucket.permission.PermissionService;
import com.atlassian.bitbucket.repository.Repository;
import com.atlassian.bitbucket.repository.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CodeInsightExtensionsPermissionService {

    private final PermissionService permissionService;
    private final RepositoryService repositoryService;
    private final AuthenticationContext authenticationContext;

    @Autowired
    public CodeInsightExtensionsPermissionService(PermissionService permissionService, AuthenticationContext authenticationContext, RepositoryService repositoryService) {
        this.permissionService = permissionService;
        this.authenticationContext = authenticationContext;
        this.repositoryService = repositoryService;
    }

    public boolean doesUserHaveRepoViewPermission(String projectId, String slug) {
        Repository repository = repositoryService.getBySlug(projectId, slug);

        if (repository == null) {
            return false;
        }

        return permissionService.hasRepositoryPermission(authenticationContext.getCurrentUser(), repository, Permission.REPO_READ);
    }

    public boolean doesUserHaveRepoAdminPermission(String projectId, String slug) {
        Repository repository = repositoryService.getBySlug(projectId, slug);

        if (repository == null) {
            return false;
        }

        return permissionService.hasRepositoryPermission(authenticationContext.getCurrentUser(), repository, Permission.REPO_ADMIN);
    }
}

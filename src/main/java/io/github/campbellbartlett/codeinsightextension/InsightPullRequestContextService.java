package io.github.campbellbartlett.codeinsightextension;

import com.atlassian.bitbucket.auth.AuthenticationContext;
import com.atlassian.bitbucket.permission.Permission;
import com.atlassian.bitbucket.permission.PermissionService;
import com.atlassian.bitbucket.pull.PullRequest;
import com.atlassian.bitbucket.pull.PullRequestSearchRequest;
import com.atlassian.bitbucket.pull.PullRequestService;
import com.atlassian.bitbucket.repository.Repository;
import com.atlassian.bitbucket.util.PageRequestImpl;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import io.github.campbellbartlett.codeinsightextension.rest.pojo.PullRequestContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Service
public class InsightPullRequestContextService {

    private final PullRequestService pullRequestService;
    private final PermissionService permissionService;
    private final AuthenticationContext authenticationContext;

    private final RepositoryResolveService repositoryResolveService;
    private final InsightReportStatusService insightReportStatusService;
    private final AdminRiskAcceptedService adminRiskAcceptedService;

    @Autowired
    public InsightPullRequestContextService(@ComponentImport PullRequestService pullRequestService,
                                            @ComponentImport PermissionService permissionService,
                                            @ComponentImport AuthenticationContext authenticationContext,
                                            RepositoryResolveService repositoryResolveService,
                                            InsightReportStatusService insightReportStatusService,
                                            AdminRiskAcceptedService adminRiskAcceptedService) {
        this.pullRequestService = pullRequestService;
        this.permissionService = permissionService;
        this.authenticationContext = authenticationContext;
        this.repositoryResolveService = repositoryResolveService;
        this.insightReportStatusService = insightReportStatusService;
        this.adminRiskAcceptedService = adminRiskAcceptedService;
    }

    public PullRequestContext createContextForCommit(String projectId, String slug, String commitHash) {
        PullRequest pullRequest = repositoryResolveService.getPullRequest(projectId, slug, commitHash);
        Repository repository = pullRequest.getFromRef().getRepository();

        Set<Map<String, Object>> insightReports = createInsightReportContexts(pullRequest);

        return PullRequestContext.builder()
                .projectKey(projectId)
                .repositorySlug(slug)
                .commitHash(commitHash)
                .codeInsightReports(insightReports)
                .isAdminOverride(adminRiskAcceptedService.hasAdminAcceptedRisk(repository, commitHash))
                .isUserAdmin(isUserAdmin(repository))
                .build();
    }

    private PullRequest getPullRequest(long prId, Repository repository) {
        PullRequestSearchRequest prSearchRequest = new PullRequestSearchRequest.Builder()
                .fromRepositoryId(repository.getId())
                .build();

        return pullRequestService.search(prSearchRequest, new PageRequestImpl(0, 100))
                .stream()
                .filter(pr -> pr.getId() == prId)
                .findFirst()
                .orElse(null);
    }

    private boolean isUserAdmin(Repository repository) {
        return permissionService.hasRepositoryPermission(authenticationContext.getCurrentUser(), repository, Permission.REPO_ADMIN);
    }

    private Set<Map<String, Object>> createInsightReportContexts(PullRequest pullRequest) {
        Set<Map<String, Object>> insightReports = new HashSet<>();
        for (String key :insightReportKeysForPr(pullRequest)) {
            Map<String, Object> insightReportStatus = new HashMap<>();
            InsightReportStatus status = insightReportStatusService.getResultForPullRequestInsight(pullRequest, key);
            insightReportStatus.put("name", key);
            insightReportStatus.put("status", status);

            insightReports.add(insightReportStatus);
        }
        return insightReports;
    }

    private Set<String> insightReportKeysForPr(PullRequest pullRequest) {
        Set<String> keys = new HashSet<>();
        keys.add("theKey");
        keys.add("fortify");

        return keys;
    }
}

package io.github.campbellbartlett.codeinsightextension.service;

import com.atlassian.bitbucket.auth.AuthenticationContext;
import com.atlassian.bitbucket.codeinsights.report.InsightReport;
import com.atlassian.bitbucket.codeinsights.report.InsightResult;
import com.atlassian.bitbucket.permission.Permission;
import com.atlassian.bitbucket.permission.PermissionService;
import com.atlassian.bitbucket.pull.PullRequest;
import com.atlassian.bitbucket.repository.Repository;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import io.github.campbellbartlett.codeinsightextension.InsightReportStatus;
import io.github.campbellbartlett.codeinsightextension.rest.pojo.PullRequestContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Service
public class InsightPullRequestContextService {

    private final PermissionService permissionService;
    private final AuthenticationContext authenticationContext;

    private final RepositoryResolveService repositoryResolveService;
    private final InsightReportStatusService insightReportStatusService;
    private final AdminRiskAcceptedService adminRiskAcceptedService;

    @Autowired
    public InsightPullRequestContextService(@ComponentImport PermissionService permissionService,
                                            @ComponentImport AuthenticationContext authenticationContext,
                                            RepositoryResolveService repositoryResolveService,
                                            InsightReportStatusService insightReportStatusService,
                                            AdminRiskAcceptedService adminRiskAcceptedService) {
        this.permissionService = permissionService;
        this.authenticationContext = authenticationContext;
        this.repositoryResolveService = repositoryResolveService;
        this.insightReportStatusService = insightReportStatusService;
        this.adminRiskAcceptedService = adminRiskAcceptedService;
    }

    public PullRequestContext createContextForPullRequest(PullRequest pullRequest) {
        Repository repository = pullRequest.getFromRef().getRepository();

        String projectId = repository.getProject().getKey();
        String slug = repository.getSlug();
        String commitHash = pullRequest.getFromRef().getLatestCommit();

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

    public PullRequestContext createContextForCommit(String projectId, String slug, String commitHash) {
        PullRequest pullRequest = repositoryResolveService.getPullRequest(projectId, slug, commitHash);
        return createContextForPullRequest(pullRequest);
    }

    private boolean isUserAdmin(Repository repository) {
        return permissionService.hasRepositoryPermission(authenticationContext.getCurrentUser(), repository, Permission.REPO_ADMIN);
    }

    private Set<Map<String, Object>> createInsightReportContexts(PullRequest pullRequest) {
        Set<InsightReport> insightReports = insightReportStatusService.getAllReportsForPullRequest(pullRequest);

        Set<Map<String, Object>> insightReportContext = new HashSet<>();
        for (InsightReport report: insightReports) {
            Map<String, Object> insightReportStatus = new HashMap<>();

            InsightReportStatus status = getInsightReportStatus(report);

            insightReportStatus.put("name", report.getKey());
            insightReportStatus.put("status", status);

            insightReportContext.add(insightReportStatus);
        }
        return insightReportContext;
    }

    private InsightReportStatus getInsightReportStatus(InsightReport report) {
        if (report.getResult().isPresent()) {
            if (report.getResult().get().equals(InsightResult.FAIL)) {
                return InsightReportStatus.FAIL;
            }
            return InsightReportStatus.PASS;
        }
        return InsightReportStatus.WAITING;
    }
}

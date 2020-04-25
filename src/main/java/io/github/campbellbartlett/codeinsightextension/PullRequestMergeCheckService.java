package io.github.campbellbartlett.codeinsightextension;

import com.atlassian.bitbucket.hook.repository.PreRepositoryHookContext;
import com.atlassian.bitbucket.hook.repository.PullRequestMergeHookRequest;
import com.atlassian.bitbucket.hook.repository.RepositoryHookResult;
import com.atlassian.bitbucket.hook.repository.RepositoryMergeCheck;
import com.atlassian.bitbucket.pull.PullRequest;
import io.github.campbellbartlett.codeinsightextension.rest.pojo.PullRequestContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;

@Component
public class PullRequestMergeCheckService implements RepositoryMergeCheck {

    private final AdminRiskAcceptedService adminRiskAcceptedService;

    private final InsightPullRequestContextService insightPullRequestContextService;

    @Autowired
    public PullRequestMergeCheckService(AdminRiskAcceptedService adminRiskAcceptedService, InsightPullRequestContextService insightPullRequestContextService) {
        this.adminRiskAcceptedService = adminRiskAcceptedService;
        this.insightPullRequestContextService = insightPullRequestContextService;
    }

    @Nonnull
    @Override
    public RepositoryHookResult preUpdate(@Nonnull PreRepositoryHookContext context, @Nonnull PullRequestMergeHookRequest request) {
        PullRequest pullRequest = request.getPullRequest();

        if (adminRiskAcceptedService.hasAdminAcceptedRisk(request.getRepository(), pullRequest.getFromRef().getLatestCommit())) {
            return RepositoryHookResult.accepted();
        }

        PullRequestContext insightsContext = insightPullRequestContextService.createContextForPullRequest(pullRequest);

        if (insightsContext.getCodeInsightReports().stream()
                .map(report -> ((InsightReportStatus) report.get("status")))
                .anyMatch(status -> status.equals(InsightReportStatus.FAIL))
        ) {
            return RepositoryHookResult.rejected("Unable to merge due to failing code insight", "Unable to merge until all code insight checks have passed");
        }

        if (insightsContext.getCodeInsightReports().stream()
                .map(report -> ((InsightReportStatus) report.get("status")))
                .anyMatch(status -> status.equals(InsightReportStatus.WAITING))
        ){
            return RepositoryHookResult.rejected("Waiting for code insight status to be reported", "Merging is blocked until all code insights have reported their status");
        }

        return RepositoryHookResult.accepted();
    }
}

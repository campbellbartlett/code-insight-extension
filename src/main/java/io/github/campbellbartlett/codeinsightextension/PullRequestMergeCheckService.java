package io.github.campbellbartlett.codeinsightextension;

import com.atlassian.bitbucket.hook.repository.PreRepositoryHookContext;
import com.atlassian.bitbucket.hook.repository.PullRequestMergeHookRequest;
import com.atlassian.bitbucket.hook.repository.RepositoryHookResult;
import com.atlassian.bitbucket.hook.repository.RepositoryMergeCheck;
import com.atlassian.bitbucket.pull.PullRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;

@Component
public class PullRequestMergeCheckService implements RepositoryMergeCheck {

    private final InsightReportStatusService insightReportStatusService;
    private final AdminRiskAcceptedService adminRiskAcceptedService;

    @Autowired
    public PullRequestMergeCheckService(AdminRiskAcceptedService adminRiskAcceptedService, InsightReportStatusService insightReportStatusService) {
        this.adminRiskAcceptedService = adminRiskAcceptedService;
        this.insightReportStatusService = insightReportStatusService;
    }

    @Nonnull
    @Override
    public RepositoryHookResult preUpdate(@Nonnull PreRepositoryHookContext context, @Nonnull PullRequestMergeHookRequest request) {
        PullRequest pullRequest = request.getPullRequest();

        if (adminRiskAcceptedService.hasAdminAcceptedRisk(request.getRepository(), pullRequest.getFromRef().getLatestCommit())) {
            return RepositoryHookResult.accepted();
        }

        InsightReportStatus reportStatus = insightReportStatusService.getResultForPullRequestInsight(pullRequest, "theKey");

        if (reportStatus.equals(InsightReportStatus.FAIL)) {
            return RepositoryHookResult.rejected("Unable to merge due to failing code insight", "Unable to merge until all code insight checks have passed");
        }

        if (reportStatus.equals(InsightReportStatus.WAITING)) {
            return RepositoryHookResult.rejected("Waiting for code insight status to be reported", "Merging is blocked until all code insights have reported their status");
        }

        return RepositoryHookResult.accepted();
    }
}

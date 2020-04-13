package io.github.campbellbartlett.codeinsightextension;

import com.atlassian.bitbucket.codeinsights.report.GetInsightReportRequest;
import com.atlassian.bitbucket.codeinsights.report.InsightReport;
import com.atlassian.bitbucket.codeinsights.report.InsightReportService;
import com.atlassian.bitbucket.codeinsights.report.InsightResult;
import com.atlassian.bitbucket.pull.PullRequest;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class InsightReportStatusService {

    private final InsightReportService insightReportService;

    @Autowired
    public InsightReportStatusService(@ComponentImport InsightReportService insightReportService) {
        this.insightReportService = insightReportService;
    }

    public InsightReportStatus getResultForPullRequestInsight(PullRequest pullRequest, String insightKey) {
        GetInsightReportRequest insightReportRequest = new GetInsightReportRequest.Builder(pullRequest, insightKey).build();
        Optional<InsightReport> optionalInsight = insightReportService.get(insightReportRequest);

        if (!optionalInsight.isPresent() || !optionalInsight.get().getResult().isPresent()) {
            return InsightReportStatus.WAITING;
        }

        InsightResult insightResult = optionalInsight.get().getResult().get();

        if (insightResult.equals(InsightResult.PASS)) {
            return InsightReportStatus.PASS;
        }

        return InsightReportStatus.FAIL;
    }

}

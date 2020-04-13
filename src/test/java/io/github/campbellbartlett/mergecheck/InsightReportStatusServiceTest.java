package io.github.campbellbartlett.mergecheck;

import com.atlassian.bitbucket.codeinsights.report.InsightReport;
import com.atlassian.bitbucket.codeinsights.report.InsightReportService;
import com.atlassian.bitbucket.codeinsights.report.InsightResult;
import com.atlassian.bitbucket.pull.PullRequest;
import com.atlassian.bitbucket.pull.PullRequestRef;
import com.atlassian.bitbucket.repository.Repository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class InsightReportStatusServiceTest {

    private InsightReportService insightReportService;

    private InsightReport insightReport;

    private PullRequest pullRequest;
    private PullRequestRef fromRef;
    private PullRequestRef toRef;

    private InsightReportStatusService insightReportStatusService;

    @Before
    public void setUp() {
        insightReportService = Mockito.mock(InsightReportService.class);
        insightReportStatusService = new InsightReportStatusService(insightReportService);

        pullRequest = Mockito.mock(PullRequest.class);
        fromRef = Mockito.mock(PullRequestRef.class);
        toRef = Mockito.mock(PullRequestRef.class);

        when(pullRequest.getFromRef()).thenReturn(fromRef);
        when(pullRequest.getToRef()).thenReturn(toRef);

        insightReport = Mockito.mock(InsightReport.class);
    }

    @Test
    public void insightReportNotAvailableTest() {
        when(insightReportService.get(any())).thenReturn(Optional.empty());

        when(toRef.getRepository()).thenReturn(Mockito.mock(Repository.class));
        when(fromRef.getLatestCommit()).thenReturn("foo-commit-id");

        InsightReportStatus reportStatus = insightReportStatusService.getResultForPullRequestInsight(pullRequest, "bar");

        assertEquals("When the insightService returns an empty optional then the status should be 'WAITING'",
                InsightReportStatus.WAITING, reportStatus);
    }

    @Test
    public void insightReportResultNotAvailableTest() {
        when(insightReportService.get(any())).thenReturn(Optional.ofNullable(insightReport));
        when(insightReport.getResult()).thenReturn(Optional.empty());

        when(toRef.getRepository()).thenReturn(Mockito.mock(Repository.class));
        when(fromRef.getLatestCommit()).thenReturn("foo-commit-id");

        InsightReportStatus reportStatus = insightReportStatusService.getResultForPullRequestInsight(pullRequest, "bar");

        assertEquals("When the insightReport.getResult() is an empty optional then the status should be 'WAITING'",
                InsightReportStatus.WAITING, reportStatus);
    }

    @Test
    public void insightReportResultFailTest() {
        when(insightReportService.get(any())).thenReturn(Optional.ofNullable(insightReport));
        when(insightReport.getResult()).thenReturn(Optional.of(InsightResult.FAIL));

        when(toRef.getRepository()).thenReturn(Mockito.mock(Repository.class));
        when(fromRef.getLatestCommit()).thenReturn("foo-commit-id");

        InsightReportStatus reportStatus = insightReportStatusService.getResultForPullRequestInsight(pullRequest, "bar");

        assertEquals("When the insightReport.getResult() is a FAIL then the status should be 'FAIL'",
                InsightReportStatus.FAIL, reportStatus);
    }

    @Test
    public void insightReportResultPassTest() {
        when(insightReportService.get(any())).thenReturn(Optional.ofNullable(insightReport));
        when(insightReport.getResult()).thenReturn(Optional.of(InsightResult.PASS));

        when(toRef.getRepository()).thenReturn(Mockito.mock(Repository.class));
        when(fromRef.getLatestCommit()).thenReturn("foo-commit-id");

        InsightReportStatus reportStatus = insightReportStatusService.getResultForPullRequestInsight(pullRequest, "bar");

        assertEquals("When the insightReport.getResult() is a PASS then the status should be 'FAIL'",
                InsightReportStatus.PASS, reportStatus);
    }

}
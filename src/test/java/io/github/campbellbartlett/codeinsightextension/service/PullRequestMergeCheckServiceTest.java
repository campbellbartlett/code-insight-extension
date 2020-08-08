package io.github.campbellbartlett.codeinsightextension.service;

import com.atlassian.bitbucket.codeinsights.report.InsightResult;
import com.atlassian.bitbucket.hook.repository.PreRepositoryHookContext;
import com.atlassian.bitbucket.hook.repository.PullRequestMergeHookRequest;
import com.atlassian.bitbucket.hook.repository.RepositoryHookResult;
import com.atlassian.bitbucket.hook.repository.RepositoryHookVeto;
import com.atlassian.bitbucket.pull.PullRequest;
import com.atlassian.bitbucket.pull.PullRequestRef;
import com.atlassian.bitbucket.repository.Repository;
import io.github.campbellbartlett.codeinsightextension.rest.pojo.PullRequestContext;
import io.github.campbellbartlett.codeinsightextension.util.PullRequestContextTestUtils;
import io.github.campbellbartlett.codeinsightextension.util.ReportStatusBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class PullRequestMergeCheckServiceTest {

    @Mock
    private AdminRiskAcceptedService adminRiskAcceptedService;

    @Mock
    private InsightPullRequestContextService insightPullRequestContextService;

    @Mock
    private PreRepositoryHookContext context;

    @InjectMocks
    private PullRequestMergeCheckService pullRequestMergeCheckService;

    @Mock
    private PullRequestRef mockPullRequestRef;

    @Mock
    private PullRequest mockPullRequest;

    @Mock
    private PullRequestMergeHookRequest mockMergeHookRequest;

    @Mock
    private Repository mockRepository;

    @Before
    public void setUp() {
        Mockito.reset(
                adminRiskAcceptedService,
                insightPullRequestContextService,
                context,
                mockPullRequest,
                mockMergeHookRequest,
                mockRepository
        );

        // Create a mock merge hook request, that contains a mock repository,
        // that contains a pull request, that contains a mock pull request ref
        when(mockMergeHookRequest.getPullRequest()).thenReturn(mockPullRequest);
        when(mockPullRequest.getFromRef()).thenReturn(mockPullRequestRef);
        when(mockPullRequestRef.getLatestCommit()).thenReturn("baz");

        when(mockMergeHookRequest.getRepository()).thenReturn(mockRepository);
    }

    @Test
    public void adminHasAcceptedRiskReturnsAcceptedHookTest() {
        // Return true when performing adminRiskAccepted check
        when(adminRiskAcceptedService.hasAdminAcceptedRisk(any(), any())).thenReturn(true);

        // Perform preUpdate check
        RepositoryHookResult result = pullRequestMergeCheckService.preUpdate(context, mockMergeHookRequest);

        // Expect hasAdminAcceptedRisk to be called with the correct arguments
        verify(adminRiskAcceptedService).hasAdminAcceptedRisk(mockRepository, "baz");

        assertTrue("Result of preUpdate hook should be true when admin has accepted risk", result.isAccepted());
    }

    @Test
    public void adminHasNotAcceptedRiskNoReportsReturnsAcceptedTest() {
        // Return false when performing adminRiskAccepted check
        when(adminRiskAcceptedService.hasAdminAcceptedRisk(any(), any())).thenReturn(false);

        // Create a pull request context that contains no insight reports
        PullRequestContext pullRequestContext = PullRequestContextTestUtils.getPullRequestContextWithReports(new HashMap<>());
        when(insightPullRequestContextService.createContextForPullRequest(mockPullRequest)).thenReturn(pullRequestContext);

        // Perform preUpdate check
        RepositoryHookResult result = pullRequestMergeCheckService.preUpdate(context, mockMergeHookRequest);

        assertTrue("Result of preUpdate hook should be true when there are no insight reports associated with pull request", result.isAccepted());
    }

    @Test
    public void adminHasNotAcceptedRiskPassedReportReturnsAcceptedTest() {
        // Return false when performing adminRiskAccepted check
        when(adminRiskAcceptedService.hasAdminAcceptedRisk(any(), any())).thenReturn(false);

        // Create a single insight report object with 1 report that is status PASS
        Map<String, InsightResult> reports = ReportStatusBuilder.builder()
                .addReport("report1", InsightResult.PASS)
                .build();

        // Create a pull request context that contains only 1 report that is status PASS
        PullRequestContext pullRequestContext = PullRequestContextTestUtils.getPullRequestContextWithReports(reports);
        when(insightPullRequestContextService.createContextForPullRequest(mockPullRequest)).thenReturn(pullRequestContext);

        // Perform preUpdate check
        RepositoryHookResult result = pullRequestMergeCheckService.preUpdate(context, mockMergeHookRequest);

        assertTrue("Result of preUpdate hook should be true when there is only one report and it is status PASS", result.isAccepted());
    }

    @Test
    public void adminHasNotAcceptedRiskFailedReportReturnsNotAcceptedTest() {
        // Return false when performing adminRiskAccepted check
        when(adminRiskAcceptedService.hasAdminAcceptedRisk(any(), any())).thenReturn(false);

        // Create a single insight report object with 1 report that is status FAIL
        Map<String, InsightResult> reports = ReportStatusBuilder.builder()
                .addReport("report1", InsightResult.FAIL)
                .build();

        // Create a pull request context that contains only 1 report that is status FAIL
        PullRequestContext pullRequestContext = PullRequestContextTestUtils.getPullRequestContextWithReports(reports);
        when(insightPullRequestContextService.createContextForPullRequest(mockPullRequest)).thenReturn(pullRequestContext);

        // Perform preUpdate check
        RepositoryHookResult result = pullRequestMergeCheckService.preUpdate(context, mockMergeHookRequest);

        assertFalse("Result of preUpdate hook should be false when there is only one report and it is status FAIL", result.isAccepted());
    }

    @Test
    public void adminHasNotAcceptedRiskManyReportsOneFailedReportReturnsNotAcceptedTest() {
        // Return false when performing adminRiskAccepted check
        when(adminRiskAcceptedService.hasAdminAcceptedRisk(any(), any())).thenReturn(false);

        // Create an insight report object with 1 report that is status FAIL and others that are PASS and WAITING
        Map<String, InsightResult> reports = ReportStatusBuilder.builder()
                .addReport("report1", InsightResult.FAIL)
                .addWaitingReport("report2")
                .addReport("report3", InsightResult.PASS)
                .addReport("report4", InsightResult.PASS)
                .build();

        // Create a pull request context that contains the above reports
        PullRequestContext pullRequestContext = PullRequestContextTestUtils.getPullRequestContextWithReports(reports);
        when(insightPullRequestContextService.createContextForPullRequest(mockPullRequest)).thenReturn(pullRequestContext);

        // Perform preUpdate check
        RepositoryHookResult result = pullRequestMergeCheckService.preUpdate(context, mockMergeHookRequest);

        assertFalse("Result of preUpdate hook should be false when there is at least one report that is status FAIL", result.isAccepted());
        assertEquals(
                "Only one veto should be present",
                1,
                result.getVetoes().size()
        );
        assertEquals(
                "Rejection message should match expected for FAIL reports",
                "Unable to merge due to failing code insight",
                result.getVetoes()
                        .stream()
                        .map(RepositoryHookVeto::getSummaryMessage)
                        .findFirst()
                        .orElse("")
        );
    }

    @Test
    public void adminHasNotAcceptedRiskManyReportsNoFailedOneWaitingReportReturnsNotAcceptedTest() {
        // Return false when performing adminRiskAccepted check
        when(adminRiskAcceptedService.hasAdminAcceptedRisk(any(), any())).thenReturn(false);

        // Create an insight report object with 1 report that is status WAITING and others that are PASS
        Map<String, InsightResult> reports = ReportStatusBuilder.builder()
                .addReport("report1", InsightResult.PASS)
                .addWaitingReport("report2")
                .addReport("report3", InsightResult.PASS)
                .addReport("report4", InsightResult.PASS)
                .build();

        // Create a pull request context that contains the above reports
        PullRequestContext pullRequestContext = PullRequestContextTestUtils.getPullRequestContextWithReports(reports);
        when(insightPullRequestContextService.createContextForPullRequest(mockPullRequest)).thenReturn(pullRequestContext);

        // Perform preUpdate check
        RepositoryHookResult result = pullRequestMergeCheckService.preUpdate(context, mockMergeHookRequest);

        assertFalse("Result of preUpdate hook should be false when there is at least one report that is status WAITING", result.isAccepted());
        assertEquals(
                "Only one veto should be present",
                1,
                result.getVetoes().size()
        );
        assertEquals(
                "Rejection message should match expected for WAITING reports",
                "Waiting for code insight status to be reported",
                result.getVetoes()
                        .stream()
                        .map(RepositoryHookVeto::getSummaryMessage)
                        .findFirst()
                        .orElse("")
        );
    }
}

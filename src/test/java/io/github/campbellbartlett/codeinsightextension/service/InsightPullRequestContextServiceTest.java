package io.github.campbellbartlett.codeinsightextension.service;

import com.atlassian.bitbucket.auth.AuthenticationContext;
import com.atlassian.bitbucket.codeinsights.report.InsightReport;
import com.atlassian.bitbucket.codeinsights.report.InsightResult;
import com.atlassian.bitbucket.permission.PermissionService;
import com.atlassian.bitbucket.project.Project;
import com.atlassian.bitbucket.pull.PullRequest;
import com.atlassian.bitbucket.pull.PullRequestRef;
import com.atlassian.bitbucket.repository.Repository;
import io.github.campbellbartlett.codeinsightextension.InsightReportStatus;
import io.github.campbellbartlett.codeinsightextension.rest.pojo.PullRequestContext;
import io.github.campbellbartlett.codeinsightextension.stub.InsightReportStub;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class InsightPullRequestContextServiceTest {

    @Mock
    private PermissionService permissionService;

    @Mock
    @SuppressWarnings("unused") // Required to prevent NPE when calling authenticationContext.getCurrentUser()
    private AuthenticationContext authenticationContext;

    @Mock
    private RepositoryResolveService repositoryResolveService;

    @Mock
    private InsightReportStatusService insightReportStatusService;

    @Mock
    private AdminRiskAcceptedService adminRiskAcceptedService;

    @InjectMocks
    private InsightPullRequestContextService insightPullRequestContextService;

    @Mock
    private PullRequest pullRequest;

    @Mock
    private Repository repository;

    @Mock
    PullRequestRef fromRef;

    @Mock
    Project project;

    private final static String projectKey = "fooProject";
    private final static String slug = "fooSlug";
    private final static String latestCommit = "fooHash";

    @Before
    public void setUp() {
        when(pullRequest.getFromRef()).thenReturn(fromRef);

        when(fromRef.getRepository()).thenReturn(repository);
        when(fromRef.getLatestCommit()).thenReturn(latestCommit);

        when(repository.getProject()).thenReturn(project);
        when(repository.getSlug()).thenReturn(slug);

        when(project.getKey()).thenReturn(projectKey);
    }

    @Test
    public void noInsightReportsContextIsAdminAndAdminOverrideTest() {
        when(insightReportStatusService.getAllReportsForPullRequest(pullRequest)).thenReturn(new HashSet<>());
        when(adminRiskAcceptedService.hasAdminAcceptedRisk(repository, latestCommit)).thenReturn(true);
        when(permissionService.hasRepositoryPermission(any(), any(), any())).thenReturn(true);

        PullRequestContext contextForPullRequest = insightPullRequestContextService.createContextForPullRequest(pullRequest);

        assertTrue("Admin override should be true when admin has accepted risk", contextForPullRequest.isAdminOverride());
        assertTrue("Is user admin should be true when has repository permission is true", contextForPullRequest.isUserAdmin());
    }

    @Test
    public void noInsightReportsContextIsNotAdminOrAdminOverrideTest() {
        when(insightReportStatusService.getAllReportsForPullRequest(pullRequest)).thenReturn(new HashSet<>());
        when(adminRiskAcceptedService.hasAdminAcceptedRisk(repository, latestCommit)).thenReturn(false);
        when(permissionService.hasRepositoryPermission(any(), any(), any())).thenReturn(false);

        PullRequestContext contextForPullRequest = insightPullRequestContextService.createContextForPullRequest(pullRequest);

        assertEquals(
                "Commit hash in report should match mock commit hash returned from pull request",
                latestCommit,
                contextForPullRequest.getCommitHash()
        );
        assertEquals(
                "Project key in report should match mock project key returned from repository",
                projectKey,
                contextForPullRequest.getProjectKey()
        );
        assertEquals(
                "Repo slug in report should match mock repo slug returned from repository",
                slug,
                contextForPullRequest.getRepositorySlug()
        );
        assertFalse("Admin override should be false when admin has not accepted risk", contextForPullRequest.isAdminOverride());
        assertFalse("Is user admin should be false when has repository permission is false", contextForPullRequest.isUserAdmin());
    }

    @Test
    public void noInsightReportsContextContainsCorrectCommitProjectKeyAndSlugTest() {
        when(insightReportStatusService.getAllReportsForPullRequest(pullRequest)).thenReturn(new HashSet<>());
        when(adminRiskAcceptedService.hasAdminAcceptedRisk(repository, latestCommit)).thenReturn(false);
        when(permissionService.hasRepositoryPermission(any(), any(), any())).thenReturn(false);

        PullRequestContext contextForPullRequest = insightPullRequestContextService.createContextForPullRequest(pullRequest);

        assertEquals(
                "Commit hash in report should match mock commit hash returned from pull request",
                latestCommit,
                contextForPullRequest.getCommitHash()
        );
        assertEquals(
                "Project key in report should match mock project key returned from repository",
                projectKey,
                contextForPullRequest.getProjectKey()
        );
        assertEquals(
                "Repo slug in report should match mock repo slug returned from repository",
                slug,
                contextForPullRequest.getRepositorySlug()
        );
    }

    @Test
    public void oneInsightReportContextContainsReportTest() {
        String passKey = "pass-key";
        InsightReport report = new InsightReportStub(passKey, InsightResult.PASS);


        when(insightReportStatusService.getAllReportsForPullRequest(pullRequest)).thenReturn(Collections.singleton(report));
        when(adminRiskAcceptedService.hasAdminAcceptedRisk(repository, latestCommit)).thenReturn(false);
        when(permissionService.hasRepositoryPermission(any(), any(), any())).thenReturn(false);

        PullRequestContext contextForPullRequest = insightPullRequestContextService.createContextForPullRequest(pullRequest);

        Set<Map<String, Object>> codeInsightReports = contextForPullRequest.getCodeInsightReports();
        assertEquals("Only one report should be in the context", 1, codeInsightReports.size());
        assertEquals(
                "The context should contain a report with 'pass-key'",
                passKey,
                codeInsightReports.iterator().next().get("name")
        );
        assertEquals(
                "The context should contain a report with status PASS",
                InsightReportStatus.PASS,
                codeInsightReports.iterator().next().get("status")
        );
    }

    @Test
    public void multipleInsightReportContextContainsReportTest() {
        String passKey1 = "pass-key1";
        InsightReport report1 = new InsightReportStub(passKey1, InsightResult.PASS);

        String failKey = "fail-key";
        InsightReport report2 = new InsightReportStub(failKey, InsightResult.FAIL);

        String waitingKey = "waiting-key";
        InsightReport report3 = new InsightReportStub(waitingKey, null);

        Set<InsightReport> reports = new HashSet<>();
        reports.add(report1);
        reports.add(report2);
        reports.add(report3);

        when(insightReportStatusService.getAllReportsForPullRequest(pullRequest)).thenReturn(reports);
        when(adminRiskAcceptedService.hasAdminAcceptedRisk(repository, latestCommit)).thenReturn(false);
        when(permissionService.hasRepositoryPermission(any(), any(), any())).thenReturn(false);

        PullRequestContext contextForPullRequest = insightPullRequestContextService.createContextForPullRequest(pullRequest);

        Set<Map<String, Object>> codeInsightReports = contextForPullRequest.getCodeInsightReports();
        assertEquals("All three reports should be in the context", 3, codeInsightReports.size());

        reports.forEach(report -> {
            InsightReportStatus expectedStatus = getInsightReportStatus(report.getResult());
            assertTrue(
                    "The context should contain a report with the matching key",
                    codeInsightReports.stream()
                        .anyMatch(reportStatus -> reportStatus.get("name").equals(report1.getKey()))
            );
            assertTrue(
                    "The context should contain a report with the matching status",
                    codeInsightReports.stream()
                        .anyMatch(reportStatus -> reportStatus.get("status").equals(expectedStatus))
            );
        });
    }

    @Test
    public void multipleInsightReportContextForCommitContainsReportTest() {
        String passKey1 = "pass-key1";
        InsightReport report1 = new InsightReportStub(passKey1, InsightResult.PASS);

        String failKey = "fail-key";
        InsightReport report2 = new InsightReportStub(failKey, InsightResult.FAIL);

        String waitingKey = "waiting-key";
        InsightReport report3 = new InsightReportStub(waitingKey, null);

        Set<InsightReport> reports = new HashSet<>();
        reports.add(report1);
        reports.add(report2);
        reports.add(report3);

        when(insightReportStatusService.getAllReportsForPullRequest(pullRequest)).thenReturn(reports);
        when(adminRiskAcceptedService.hasAdminAcceptedRisk(repository, latestCommit)).thenReturn(false);
        when(permissionService.hasRepositoryPermission(any(), any(), any())).thenReturn(false);
        when(repositoryResolveService.getPullRequest(projectKey, slug, latestCommit)).thenReturn(pullRequest);

        PullRequestContext contextForPullRequest = insightPullRequestContextService.createContextForCommit(projectKey, slug, latestCommit);

        Set<Map<String, Object>> codeInsightReports = contextForPullRequest.getCodeInsightReports();
        assertEquals("All three reports should be in the context", 3, codeInsightReports.size());

        reports.forEach(report -> {
            InsightReportStatus expectedStatus = getInsightReportStatus(report.getResult());
            assertTrue(
                    "The context should contain a report with the matching key",
                    codeInsightReports.stream()
                            .anyMatch(reportStatus -> reportStatus.get("name").equals(report1.getKey()))
            );
            assertTrue(
                    "The context should contain a report with the matching status",
                    codeInsightReports.stream()
                            .anyMatch(reportStatus -> reportStatus.get("status").equals(expectedStatus))
            );
        });
    }

    private static InsightReportStatus getInsightReportStatus(Optional<InsightResult> resultOptional) {
        InsightReportStatus expectedStatus;
        if (!resultOptional.isPresent()) {
            expectedStatus = InsightReportStatus.WAITING;
        } else {
            expectedStatus = resultOptional.map(result -> result.equals(InsightResult.FAIL) ? InsightReportStatus.FAIL : InsightReportStatus.PASS).get();
        }
        return expectedStatus;
    }
}

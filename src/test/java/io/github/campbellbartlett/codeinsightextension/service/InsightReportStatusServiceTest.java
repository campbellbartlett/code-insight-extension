package io.github.campbellbartlett.codeinsightextension.service;

import com.atlassian.bitbucket.codeinsights.report.InsightReport;
import com.atlassian.bitbucket.codeinsights.report.InsightReportService;
import com.atlassian.bitbucket.pull.PullRequest;
import com.atlassian.bitbucket.pull.PullRequestRef;
import com.atlassian.bitbucket.repository.Repository;
import com.atlassian.bitbucket.util.Page;
import io.github.campbellbartlett.codeinsightextension.stub.InsightReportStub;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static io.github.campbellbartlett.codeinsightextension.util.PageTestUtils.getEmptyPage;
import static io.github.campbellbartlett.codeinsightextension.util.PageTestUtils.getLastPageWithItems;
import static io.github.campbellbartlett.codeinsightextension.util.PageTestUtils.getNotLastPageWithItems;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class InsightReportStatusServiceTest {

    @Mock
    private InsightReportService insightReportService;

    @InjectMocks
    private InsightReportStatusService insightReportStatusService;

    @Mock
    private PullRequest pullRequest;

    @Mock
    private Repository repository;

    @Mock
    private PullRequestRef toRef;

    @Mock
    private PullRequestRef fromRef;

    @Before
    public void setUp() {
        when(pullRequest.getFromRef()).thenReturn(fromRef);
        when(pullRequest.getToRef()).thenReturn(toRef);

        when(fromRef.getLatestCommit()).thenReturn("1234");
        when(toRef.getRepository()).thenReturn(repository);
    }

    @Test
    public void noInsightReportsForPullRequestReturnsEmptySetTest() {
        Page<InsightReport> emptyPage = getEmptyPage();
        when(insightReportService.search(any(), any())).thenReturn(emptyPage);

        Set<InsightReport> reportsForPullRequest = insightReportStatusService.getAllReportsForPullRequest(pullRequest);

        assertNotNull("Response from getAllReportsForPullRequest should never be null", reportsForPullRequest);
        assertTrue(
                "When the insightReportService returns an empty page then the set returned by getAllReportsForPullRequest should be empty",
                reportsForPullRequest.isEmpty()
        );
    }

    @Test
    public void singleInsightReportsForPullRequestReturnsCorrectSetTest() {
        InsightReport report = new InsightReportStub();
        Page<InsightReport> page = getLastPageWithItems(Collections.singleton(report));
        when(insightReportService.search(any(), any())).thenReturn(page);

        Set<InsightReport> reportsForPullRequest = insightReportStatusService.getAllReportsForPullRequest(pullRequest);

        assertNotNull("Response from getAllReportsForPullRequest should never be null", reportsForPullRequest);
        assertEquals(
                "When the insightReportService returns one page with one item, then the reportsForPullRequests should be length 1",
                1,
                reportsForPullRequest.size()
        );
        assertTrue("The set of reports returned should contain the stubbed report", reportsForPullRequest.contains(report));
    }

    @Test
    public void singlePageMultipleInsightReportsForPullRequestReturnsCorrectSetTest() {
        List<InsightReport> reports = getInsightReports(3);

        Page<InsightReport> page = getLastPageWithItems(reports.get(0), reports.get(1), reports.get(2));
        when(insightReportService.search(any(), any())).thenReturn(page);

        Set<InsightReport> reportsForPullRequest = insightReportStatusService.getAllReportsForPullRequest(pullRequest);

        assertNotNull("Response from getAllReportsForPullRequest should never be null", reportsForPullRequest);
        assertEquals(
                "When the insightReportService returns one page with 3 items, then the reportsForPullRequests should be length 3",
                reports.size(),
                reportsForPullRequest.size()
        );
        assertTrue("The set of reports returned should contain the stubbed reports", reportsForPullRequest.containsAll(reports));
    }

    @Test
    public void multiplePagesSingleInsightReportsForPullRequestReturnsCorrectSetTest() {
        List<InsightReport> reports = getInsightReports(3);

        Page<InsightReport> page1 = getNotLastPageWithItems(reports.get(0));
        Page<InsightReport> page2 = getNotLastPageWithItems(reports.get(1));
        Page<InsightReport> page3 = getLastPageWithItems(reports.get(2));

        when(insightReportService.search(any(), any())).thenReturn(page1, page2, page3);

        Set<InsightReport> reportsForPullRequest = insightReportStatusService.getAllReportsForPullRequest(pullRequest);

        assertNotNull("Response from getAllReportsForPullRequest should never be null", reportsForPullRequest);
        assertEquals(
                "When the insightReportService returns 3 pages with 1 item each, then the reportsForPullRequests should be length 3",
                reports.size(),
                reportsForPullRequest.size()
        );
        assertTrue("The set of reports returned should contain the stubbed reports", reportsForPullRequest.containsAll(reports));
    }

    @Test
    public void multiplePagesMultipleInsightReportsForPullRequestReturnsCorrectSetTest() {
        List<InsightReport> reports = getInsightReports(6);

        Page<InsightReport> page1 = getNotLastPageWithItems(reports.get(0), reports.get(1));
        Page<InsightReport> page2 = getNotLastPageWithItems(reports.get(2), reports.get(3));
        Page<InsightReport> page3 = getLastPageWithItems(reports.get(4), reports.get(5));

        when(insightReportService.search(any(), any())).thenReturn(page1, page2, page3);

        Set<InsightReport> reportsForPullRequest = insightReportStatusService.getAllReportsForPullRequest(pullRequest);

        assertNotNull("Response from getAllReportsForPullRequest should never be null", reportsForPullRequest);
        assertEquals(
                "When the insightReportService returns  3 pages with two items each, then the reportsForPullRequests should be length 6",
                reports.size(),
                reportsForPullRequest.size()
        );
        assertTrue("The set of reports returned should contain the stubbed reports", reportsForPullRequest.containsAll(reports));
    }

    public static List<InsightReport> getInsightReports(int numberOfReports) {
        List<InsightReport> reports = new ArrayList<>();
        for (int i = 0; i < numberOfReports; i++) {
            reports.add(new InsightReportStub());
        }
        return reports;
    }
}

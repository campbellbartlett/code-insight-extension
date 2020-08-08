package io.github.campbellbartlett.codeinsightextension.service;

import com.atlassian.bitbucket.pull.PullRequest;
import com.atlassian.bitbucket.pull.PullRequestRef;
import com.atlassian.bitbucket.pull.PullRequestService;
import com.atlassian.bitbucket.repository.Repository;
import com.atlassian.bitbucket.repository.RepositoryService;
import com.atlassian.bitbucket.util.Page;
import io.github.campbellbartlett.codeinsightextension.rest.exeption.PullRequestNotFoundException;
import io.github.campbellbartlett.codeinsightextension.rest.exeption.RepositoryNotFoundException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static io.github.campbellbartlett.codeinsightextension.util.PageTestUtils.getEmptyPage;
import static io.github.campbellbartlett.codeinsightextension.util.PageTestUtils.getPageWithItems;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class RepositoryResolveServiceTest {

    @Mock
    private RepositoryService repositoryService;

    @Mock
    private PullRequestService pullRequestService;

    @InjectMocks
    private RepositoryResolveService repositoryResolveService;

    @Before
    public void setUp() {
        Mockito.reset(repositoryService, pullRequestService);
    }

    @Test(expected = RepositoryNotFoundException.class)
    public void unknownRepositoryTest() {
        // Setup the repository service to return no results
        when(repositoryService.getBySlug(any(), any())).thenReturn(null);

        // Expect this method call to throw a RepositoryNotFound exception.
        repositoryResolveService.getPullRequest("foo", "bar", "baz");
    }

    @Test(expected = PullRequestNotFoundException.class)
    public void repositoryServiceReturnsEmptyPageTest() {
        // Setup the repository service to return a valid repository
        Repository repository = mock(Repository.class);
        when(repository.getId()).thenReturn(1);
        when(repositoryService.getBySlug(any(), any())).thenReturn(repository);

        // Setup the pull request service to return no pull requests
        Page<PullRequest> emptyPage = getEmptyPage();
        when(pullRequestService.search(any(), any())).thenReturn(emptyPage);

        // Expect this method to throw a PullRequestNotFound exception
        repositoryResolveService.getPullRequest("foo", "bar", "baz");
    }

    @Test(expected = PullRequestNotFoundException.class)
    public void pullRequestWithCommitHashDoesNotExistTest() {
        // Setup the repository service to return a valid repository
        Repository repository = mock(Repository.class);
        when(repository.getId()).thenReturn(1);
        when(repositoryService.getBySlug(any(), any())).thenReturn(repository);

        // Create a valid pull request but with a different commit hash to the one being searched for
        PullRequestRef pullRequestRef = mock(PullRequestRef.class);
        when(pullRequestRef.getLatestCommit()).thenReturn("12345");
        PullRequest pullRequestWithWrongHash = mock(PullRequest.class);
        when(pullRequestWithWrongHash.getFromRef()).thenReturn(pullRequestRef);

        // Setup the pull request service to return a page including the pullRequestWithWrongHash
        List<PullRequest> pullRequests = Collections.singletonList(pullRequestWithWrongHash);
        Page<PullRequest> pageOfPullRequests = getPageWithItems(pullRequests);
        when(pullRequestService.search(any(), any())).thenReturn(pageOfPullRequests);

        // Expect this method to throw PullRequestNotFound exception
        repositoryResolveService.getPullRequest("foo", "bar", "baz");
    }


    @Test
    public void matchingPullRequestFoundTest() {
        // Setup the repository service to return a valid repository
        Repository repository = mock(Repository.class);
        when(repository.getId()).thenReturn(1);
        when(repositoryService.getBySlug(any(), any())).thenReturn(repository);

        // Create a pull request with a pull request ref that has the correct hash
        PullRequestRef pullRequestRef = mock(PullRequestRef.class);
        when(pullRequestRef.getLatestCommit()).thenReturn("baz");
        PullRequest mockPullRequest = mock(PullRequest.class);
        when(mockPullRequest.getFromRef()).thenReturn(pullRequestRef);

        // Setup the pull request service to return a page including the pull request
        List<PullRequest> pullRequests = Collections.singletonList(mockPullRequest);
        Page<PullRequest> pageOfPullRequests = getPageWithItems(pullRequests);
        when(pullRequestService.search(any(), any())).thenReturn(pageOfPullRequests);

        // Search for the pull request
        PullRequest pullRequest = repositoryResolveService.getPullRequest("foo", "bar", "baz");

        // Expect the pull request to be returned
        assertNotNull("Pull request returned from request should never be null", pullRequest);
        assertEquals("Pull request returned should match expected", mockPullRequest, pullRequest);
    }

    @Test
    public void multiplePullRequestFoundReturnCorrectBasedOnHashTest() {
        // Setup repository service to return the mock repository.
        Repository repository = mock(Repository.class);
        when(repository.getId()).thenReturn(1);
        when(repositoryService.getBySlug(any(), any())).thenReturn(repository);

        // Create correct PullRequest and PullRequestRef mocks
        PullRequestRef correctPullRequestRef = mock(PullRequestRef.class);
        when(correctPullRequestRef.getLatestCommit()).thenReturn("baz");
        PullRequest correctPullRequest = mock(PullRequest.class);
        when(correctPullRequest.getFromRef()).thenReturn(correctPullRequestRef);

        // Create wrong PullRequest and PullRequestRef mocks
        // marked as Mockito.lenient because we will not perform any direct assertions over these mocks
        // because the expected behaviors is that the repositoryResolveService filters this out.
        PullRequestRef wrongPullRequestRef = mock(PullRequestRef.class, withSettings().lenient());
        when(wrongPullRequestRef.getLatestCommit()).thenReturn("12345");
        PullRequest wrongPullRequest = mock(PullRequest.class, withSettings().lenient());
        when(wrongPullRequest.getFromRef()).thenReturn(wrongPullRequestRef);

        // Setup pullRequest service to return both correct and incorrect pull requests
        List<PullRequest> pullRequests = new ArrayList<>();
        pullRequests.add(correctPullRequest);
        pullRequests.add(wrongPullRequest);
        Page<PullRequest> pageOfPullRequests = getPageWithItems(pullRequests);
        when(pullRequestService.search(any(), any())).thenReturn(pageOfPullRequests);

        // Request pull request with commit hash baz
        PullRequest pullRequest = repositoryResolveService.getPullRequest("foo", "bar", "baz");

        // Expect the correct pull request to be returned
        assertNotNull("Pull request returned from request should never be null", pullRequest);
        assertEquals("Pull request returned should match expected", correctPullRequest, pullRequest);
    }
}

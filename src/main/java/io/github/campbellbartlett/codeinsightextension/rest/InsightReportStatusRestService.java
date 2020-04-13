package io.github.campbellbartlett.codeinsightextension.rest;

import com.atlassian.bitbucket.auth.AuthenticationContext;
import com.atlassian.bitbucket.pull.PullRequest;
import com.atlassian.bitbucket.pull.PullRequestSearchRequest;
import com.atlassian.bitbucket.pull.PullRequestService;
import com.atlassian.bitbucket.repository.Repository;
import com.atlassian.bitbucket.repository.RepositoryService;
import com.atlassian.bitbucket.util.PageRequestImpl;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import io.github.campbellbartlett.codeinsightextension.InsightReportStatus;
import io.github.campbellbartlett.codeinsightextension.InsightReportStatusService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Path("/")
@Service
public class InsightReportStatusRestService {

    private static final Logger logger = LoggerFactory.getLogger(InsightReportStatusService.class);

    private final RepositoryService repositoryService;
    private final PullRequestService pullRequestService;
    private final AuthenticationContext authenticationContext;

    private final InsightReportStatusService insightReportStatusService;

    @Autowired
    public InsightReportStatusRestService(@ComponentImport AuthenticationContext authenticationContext,
                                          @ComponentImport PullRequestService pullRequestService,
                                          @ComponentImport RepositoryService repositoryService,
                                          InsightReportStatusService insightReportStatusService) {
        this.repositoryService = repositoryService;
        this.pullRequestService = pullRequestService;
        this.authenticationContext = authenticationContext;
        this.insightReportStatusService = insightReportStatusService;
    }

    @GET
    @Path("pullRequest/{repoSlug}/{prId}/insightKeys")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getInsightKeysForPullRequest(@PathParam("repoSlug") String repositorySlug, @PathParam("prId") String pullRequestId) {
        Set<String> keySet = new HashSet<>();
        keySet.add("theKey");
        return Response.status(Response.Status.OK)
                .entity(keySet)
                .build();
    }

    @GET
    @Path("pullRequest/{projectId}/{slug}/{prId}/insightReportStatus/{reportKey}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getInsightReportStatus(
            @PathParam("projectId") String projectId,
            @PathParam("slug") String slug,
            @PathParam("prId") long prId,
            @PathParam("reportKey") String insightReportKey) {
        Repository repository = repositoryService.getBySlug(projectId, slug);

        if (repository == null) {
            logger.warn("Cannot find repository in project [{}] with slug [{}]", projectId, slug);
            return Response.status(Response.Status.NOT_FOUND)
                    .build();
        }

        PullRequestSearchRequest prSearchRequest = new PullRequestSearchRequest.Builder()
                .fromRepositoryId(repository.getId())
                .build();

        PullRequest pullRequest = pullRequestService.search(prSearchRequest, new PageRequestImpl(0, 100))
                .stream()
                .filter(pr -> pr.getId() == prId)
                .findFirst()
                .orElse(null);

        if (pullRequest == null) {
            logger.warn("Cannot find PR in repository [{}] with id [{}]", repository.getName(), prId);
            return Response.status(Response.Status.NOT_FOUND)
                    .build();
        }

        InsightReportStatus reportStatus = insightReportStatusService.getResultForPullRequestInsight(pullRequest, insightReportKey);

        Map<String, Object> response = new HashMap<>();
        response.put("status", reportStatus);

        return Response.status(Response.Status.OK)
                .entity(response)
                .build();
    }
}

package io.github.campbellbartlett.codeinsightextension.rest;

import io.github.campbellbartlett.codeinsightextension.AdminRiskAcceptedService;
import io.github.campbellbartlett.codeinsightextension.CodeInsightExtensionsPermissionService;
import io.github.campbellbartlett.codeinsightextension.InsightPullRequestContextService;
import io.github.campbellbartlett.codeinsightextension.rest.exeption.PullRequestNotFoundException;
import io.github.campbellbartlett.codeinsightextension.rest.exeption.RepositoryNotFoundException;
import io.github.campbellbartlett.codeinsightextension.rest.pojo.PullRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashSet;
import java.util.Set;

@Path("/")
@Service
public class InsightReportStatusRestService {

    private static final Logger logger = LoggerFactory.getLogger(InsightReportStatusRestService.class);

    private final AdminRiskAcceptedService adminRiskAcceptedService;
    private final InsightPullRequestContextService insightPullRequestContextService;
    private final CodeInsightExtensionsPermissionService codeInsightExtensionsPermissionService;

    @Autowired
    public InsightReportStatusRestService(AdminRiskAcceptedService adminRiskAcceptedService,
                                          InsightPullRequestContextService insightPullRequestContextService,
                                          CodeInsightExtensionsPermissionService codeInsightExtensionsPermissionService) {
        this.codeInsightExtensionsPermissionService = codeInsightExtensionsPermissionService;
        this.insightPullRequestContextService = insightPullRequestContextService;
        this.adminRiskAcceptedService = adminRiskAcceptedService;
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
    @Path("pullRequest/{projectId}/{slug}/{commitHash}/context")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getInsightExtensionContextForCommit(@PathParam("projectId") String projectId, @PathParam("slug") String slug, @PathParam("commitHash") String commitHash) {

        if (!codeInsightExtensionsPermissionService.doesUserHaveRepoViewPermission(projectId, slug)) {
            return Response.status(Response.Status.FORBIDDEN)
                    .build();
        }

        try {
            PullRequestContext pullRequestContext = insightPullRequestContextService.createContextForCommit(projectId, slug, commitHash);
            return Response.status(Response.Status.OK)
                    .entity(pullRequestContext)
                    .build();
        } catch (RepositoryNotFoundException | PullRequestNotFoundException e) {
            logger.error(e.getMessage());
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @PUT
    @Path("pullRequest/{projectId}/{slug}/{commitHash}/override")
    @Produces({MediaType.APPLICATION_JSON})
    public Response putAdminOverrideForPr(
            @PathParam("projectId") String projectId,
            @PathParam("slug") String slug,
            @PathParam("commitHash") String commitHash,
            @QueryParam("revoke") @DefaultValue("false") boolean revoke) {
        if (!codeInsightExtensionsPermissionService.doesUserHaveRepoAdminPermission(projectId, slug)) {
            return Response.status(Response.Status.FORBIDDEN)
                    .build();
        }
        adminRiskAcceptedService.createOrUpdateAdminOverrideForCommit(projectId, slug, commitHash, revoke);
        return Response.status(Response.Status.OK).build();
    }
}

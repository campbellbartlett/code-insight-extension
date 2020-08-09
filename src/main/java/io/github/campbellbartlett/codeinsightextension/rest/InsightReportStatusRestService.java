package io.github.campbellbartlett.codeinsightextension.rest;

import io.github.campbellbartlett.codeinsightextension.rest.exeption.PullRequestNotFoundException;
import io.github.campbellbartlett.codeinsightextension.rest.exeption.RepositoryNotFoundException;
import io.github.campbellbartlett.codeinsightextension.rest.pojo.PullRequestContext;
import io.github.campbellbartlett.codeinsightextension.service.AdminRiskAcceptedService;
import io.github.campbellbartlett.codeinsightextension.service.CodeInsightExtensionsPermissionService;
import io.github.campbellbartlett.codeinsightextension.service.InsightPullRequestContextService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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

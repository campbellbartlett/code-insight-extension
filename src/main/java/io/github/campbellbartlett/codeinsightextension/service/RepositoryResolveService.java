package io.github.campbellbartlett.codeinsightextension.service;

import com.atlassian.bitbucket.pull.PullRequest;
import com.atlassian.bitbucket.pull.PullRequestSearchRequest;
import com.atlassian.bitbucket.pull.PullRequestService;
import com.atlassian.bitbucket.repository.Repository;
import com.atlassian.bitbucket.repository.RepositoryService;
import com.atlassian.bitbucket.util.PageRequestImpl;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import io.github.campbellbartlett.codeinsightextension.rest.exeption.PullRequestNotFoundException;
import io.github.campbellbartlett.codeinsightextension.rest.exeption.RepositoryNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;

@Service
public class RepositoryResolveService {

    private final RepositoryService repositoryService;
    private final PullRequestService pullRequestService;

    @Autowired
    public RepositoryResolveService(@ComponentImport RepositoryService repositoryService,
                                    @ComponentImport PullRequestService pullRequestService) {
        this.repositoryService = repositoryService;
        this.pullRequestService = pullRequestService;
    }

    public PullRequest getPullRequest(String projectId, String slug, String commitHash) {
        Repository repository = repositoryService.getBySlug(projectId, slug);

        if (repository == null) {
            throw new RepositoryNotFoundException(MessageFormat.format("Repository not found with projectId [{0}] and slug [{1}]", projectId, slug));
        }

        PullRequestSearchRequest prSearchRequest = new PullRequestSearchRequest.Builder()
                .fromRepositoryId(repository.getId())
                .build();

        return pullRequestService.search(prSearchRequest, new PageRequestImpl(0, 100))
                .stream()
                .filter(pr -> pr.getFromRef().getLatestCommit().equals(commitHash))
                .findFirst()
                .orElseThrow(() -> new PullRequestNotFoundException(MessageFormat.format("PullRequest not found for repository with projectId [{0}] and slug [{1}] and commitHash [{2}]", projectId, slug, commitHash)));
    }
}

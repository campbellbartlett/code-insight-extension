package io.github.campbellbartlett.codeinsightextension.repository;

import com.atlassian.activeobjects.tx.Transactional;
import io.github.campbellbartlett.codeinsightextension.activeobjects.PullRequestRiskAccepted;

import java.util.Date;
import java.util.List;

@Transactional
public interface PullRequestRiskAcceptedRepository {

    PullRequestRiskAccepted add(String commitHash, String repoSlug, String projectId, String authenticatingUserSlug, Date createDate);

    /**
     * Soft deletes a record by marking it as revoked.
     * Records that are revoked are not used to determine the 'mergability' of a pull request
     */
    void delete(String commitHash, String repoSlug, String projectId);

    List<PullRequestRiskAccepted> findAll();

    List<PullRequestRiskAccepted> findAllForPullRequest(String projectId, String repoSlug, String commitHash);
}

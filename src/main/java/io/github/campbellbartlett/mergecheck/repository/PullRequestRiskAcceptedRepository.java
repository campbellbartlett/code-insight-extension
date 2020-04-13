package io.github.campbellbartlett.mergecheck.repository;

import com.atlassian.activeobjects.tx.Transactional;
import io.github.campbellbartlett.mergecheck.activeobjects.PullRequestRiskAccepted;

import java.util.Date;
import java.util.List;

@Transactional
public interface PullRequestRiskAcceptedRepository {

    PullRequestRiskAccepted add(long pullRequestId, String repoSlug, String authenticatingUserSlug, Date createDate);

    List<PullRequestRiskAccepted> findAll();

    List<PullRequestRiskAccepted> findAllForPullRequest(long pullRequestId);
}

import { Context } from '@atlassian/clientside-extensions-registry/lib/types';
import { PullRequestInsightsContext } from '../PullRequestInsightsContext';
import getFetchOptions from './get-fetch-options';

const toggleAdminOverride = async (
    context: Context<{}>,
    insightsContext: PullRequestInsightsContext
) => {
    if (!context) {
        // eslint-disable-next-line no-console
        console.error('Context is required');
        return Promise.reject(new Error('Context is required'));
    }

    const projectKey = context.project.key;
    const repoSlug = context.repository.slug;
    const commitHash = context.pullRequest.fromRef.latestCommit;
    const revoke = insightsContext && insightsContext.adminOverride ? 'true' : 'false';

    await fetch(
        `/rest/code-insight-extension/1.0/pullRequest/${projectKey}/${repoSlug}/${commitHash}/override?revoke=${revoke}`,
        getFetchOptions('PUT')
    );

    return Promise.resolve();
};

export default toggleAdminOverride;

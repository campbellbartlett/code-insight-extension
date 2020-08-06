import { Context } from '@atlassian/clientside-extensions-registry/lib/types';
import { PullRequestInsightsContext } from '../PullRequestInsightsContext';
import getFetchOptions from './get-fetch-options';

const getPullRequestInsightExtensionsContextUri = (context: Context<{}>) => {
    if (!context) {
        // eslint-disable-next-line no-console
        console.error('Context is required');
        return '';
    }

    const projectKey = context.project.key;
    const repoSlug = context.repository.slug;
    const commitHash = context.pullRequest.fromRef.latestCommit;

    return `/rest/code-insight-extension/1.0/pullRequest/${projectKey}/${repoSlug}/${commitHash}/context`;
};

const getCodeInsightExtensionsContext = async (context: Context<{}>) => {
    const data = await fetch(
        getPullRequestInsightExtensionsContextUri(context),
        getFetchOptions('GET')
    );
    return Promise.resolve<PullRequestInsightsContext>(data.json());
};

export default getCodeInsightExtensionsContext;

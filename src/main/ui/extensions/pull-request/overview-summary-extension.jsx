import { ModalExtension, renderElementAsReact } from '@atlassian/clientside-extensions';
import React, { useState, useEffect } from 'react';
import { AdminOverrideStatus } from './admin-override-status';
import { ReportStatus } from './report-status';
import { OverviewAdmonition } from './overview-admonition';

const getFetchOptions = methodType => ({
    method: methodType,
    headers: {
        'Content-Type': 'application/json',
    },
});

const getPullRequestInsightExtensionsContextUri = context => {
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

function putPullRequestAdminOverrideUri(context, loaded, insightsContext) {
    if (!context) {
        // eslint-disable-next-line no-console
        console.error('Context is required');
        return '';
    }

    const projectKey = context.project.key;
    const repoSlug = context.repository.slug;
    const commitHash = context.pullRequest.fromRef.latestCommit;
    const revoke = loaded && insightsContext.adminOverride ? 'true' : 'false';

    return `/rest/code-insight-extension/1.0/pullRequest/${projectKey}/${repoSlug}/${commitHash}/override?revoke=${revoke}`;
}

const createMainButton = (isOverridden, loaded, userAdmin, onOverride) => ({
    text: isOverridden ? 'Disable merge override' : 'Enable merging',
    onClick: () => {
        onOverride();
    },
    isDisabled: loaded && !userAdmin,
});

const createCloseButton = (modalApi, updated) => ({
    text: 'Close',
    onClick: () => {
        modalApi.closeModal();
        if (updated) {
            // Refresh the page to show new status of merge button
            window.location.reload();
        }
    },
});

const getAppearance = (loaded, adminOverride) =>
    loaded && adminOverride ? ModalExtension.Appearance.warning : ModalExtension.Appearance.danger;

// Export default required for Atlassian Client Side Extensions to be discovered.
// noinspection JSUnusedGlobalSymbols
/**
 * @clientside-extension
 *
 * @extension-point bitbucket.ui.pullrequest.overview.summary
 */
export default ModalExtension.factory((api, context) => {
    return {
        label: `Code Insight Extension`,
        onAction(modalApi) {
            modalApi.setTitle('Code Insights Extension').setWidth(ModalExtension.Width.large);

            const ModalComponent = () => {
                const [loaded, setLoaded] = useState(false);
                const [updated, setUpdated] = useState(false);
                const [insightsContext, setInsightsContext] = useState({});

                useEffect(() => {
                    setActionButtons();
                    getCodeInsightExtensionsContext();
                });

                const getOnMainClick = () => {
                    return () => {
                        fetch(
                            putPullRequestAdminOverrideUri(context, loaded, insightsContext),
                            getFetchOptions('PUT')
                        ).then(() => {
                            getCodeInsightExtensionsContext();
                            setUpdated(true);
                        });
                    };
                };

                const setActionButtons = () => {
                    const onMainClick = getOnMainClick();
                    const isOverridden = loaded && insightsContext.adminOverride;
                    const mainButton = createMainButton(
                        isOverridden,
                        loaded,
                        loaded && insightsContext.userAdmin,
                        onMainClick
                    );
                    const closeButton = createCloseButton(modalApi, updated);
                    modalApi.setAppearance(
                        getAppearance(loaded, loaded && insightsContext.adminOverride)
                    );
                    modalApi.setActions([mainButton, closeButton]);
                };

                const getCodeInsightExtensionsContext = () => {
                    fetch(
                        getPullRequestInsightExtensionsContextUri(context),
                        getFetchOptions('GET')
                    )
                        .then(res => res.json())
                        .then(res => {
                            setInsightsContext(res);
                            setLoaded(true);
                            setActionButtons();
                        });
                };

                return (
                    <div data-testid="modal-with-action-callback">
                        {loaded && (
                            <AdminOverrideStatus
                                isOverride={
                                    insightsContext.userAdmin && insightsContext.adminOverride
                                }
                            />
                        )}
                        {loaded && (
                            <ReportStatus codeInsightReports={insightsContext.codeInsightReports} />
                        )}
                        {loaded && <OverviewAdmonition userAdmin={insightsContext.userAdmin} />}
                    </div>
                );
            };

            renderElementAsReact(modalApi, ModalComponent);
        },
    };
});

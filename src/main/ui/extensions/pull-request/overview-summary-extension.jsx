import { ModalExtension, renderElementAsReact } from '@atlassian/clientside-extensions';
import React from 'react';
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

function putPullRequestAdminOverrideUri(context, state) {
    if (!context) {
        // eslint-disable-next-line no-console
        console.error('Context is required');
        return '';
    }

    const projectKey = context.project.key;
    const repoSlug = context.repository.slug;
    const commitHash = context.pullRequest.fromRef.latestCommit;
    const revoke = state && state.loaded && state.insightsContext.adminOverride ? 'true' : 'false';

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

            class ModalComponent extends React.Component {
                updated = false;

                constructor(props) {
                    super(props);
                    this.state = { loaded: false, admin: false };
                }

                componentDidMount() {
                    this.setActionButtons(false);
                    this.getCodeInsightExtensionsContext();
                }

                setActionButtons() {
                    const onMainClick = this.getOnMainClick();
                    const { updated } = this;
                    const { loaded, insightsContext } = this.state;
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
                }

                getOnMainClick() {
                    return () => {
                        fetch(
                            putPullRequestAdminOverrideUri(context, this.state),
                            getFetchOptions('PUT')
                        ).then(() => {
                            this.getCodeInsightExtensionsContext();
                            this.updated = true;
                        });
                    };
                }

                getCodeInsightExtensionsContext() {
                    fetch(
                        getPullRequestInsightExtensionsContextUri(context),
                        getFetchOptions('GET')
                    )
                        .then(res => res.json())
                        .then(insightsContext => {
                            this.setState({ insightsContext });
                            this.setState({ loaded: true });
                            this.setActionButtons();
                        });
                }

                render() {
                    const { loaded } = this.state;
                    const { insightsContext } = this.state;
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
                                <ReportStatus
                                    codeInsightReports={insightsContext.codeInsightReports}
                                />
                            )}
                            {loaded && <OverviewAdmonition userAdmin={insightsContext.userAdmin} />}
                        </div>
                    );
                }
            }

            renderElementAsReact(modalApi, ModalComponent);
        },
    };
});

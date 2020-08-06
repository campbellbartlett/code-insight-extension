import { ModalExtension, renderElementAsReact } from '@atlassian/clientside-extensions';
import React, { useState, useEffect } from 'react';
import { Context, PluginAPI } from '@atlassian/clientside-extensions-registry/lib/types';
import { AdminOverrideStatus } from './admin-override-status';
import { ReportStatus } from './report-status/report-status';
import { OverviewAdmonition } from './overview-admonition';
import { PullRequestInsightsContext } from './PullRequestInsightsContext';
import getCodeInsightExtensionsContext from './insight-context-api/get-insight-context';
import toggleAdminOverride from './insight-context-api/toggle-admin-override';

const createMainButton = (
    isOverridden: boolean,
    loaded: boolean,
    userAdmin: boolean,
    onOverride: () => void
) => ({
    text: isOverridden ? 'Disable merge override' : 'Enable merging',
    onClick: () => {
        onOverride();
    },
    isDisabled: loaded && !userAdmin,
});

const createCloseButton = (modalApi: ModalExtension.Api, updated: boolean) => ({
    text: 'Close',
    onClick: () => {
        modalApi.closeModal();
        if (updated) {
            // Refresh the page to show new status of merge button
            window.location.reload();
        }
    },
});

const getAppearance = (loaded: boolean, adminOverride: boolean) =>
    loaded && adminOverride ? ModalExtension.Appearance.warning : ModalExtension.Appearance.danger;

// Export default required for Atlassian Client Side Extensions to be discovered.
// noinspection JSUnusedGlobalSymbols
/**
 * @clientside-extension
 *
 * @extension-point bitbucket.ui.pullrequest.overview.summary
 */
export default ModalExtension.factory((api: PluginAPI, context: Context<{}>) => {
    return {
        label: `Code Insight Extension`,
        onAction: async (modalApi: ModalExtension.Api) => {
            modalApi.setTitle('Code Insights Extension').setWidth(ModalExtension.Width.large);

            const ModalComponent = () => {
                const [loaded, setLoaded] = useState(false);
                const [updated, setUpdated] = useState(false);
                const [
                    insightsContext,
                    setInsightsContext,
                ] = useState<PullRequestInsightsContext | null>(null);

                useEffect(() => {
                    setActionButtons();
                    getCodeInsightExtensionsContext(context).then(
                        (codeInsightContext: PullRequestInsightsContext) => {
                            setInsightsContext(codeInsightContext);
                            setLoaded(true);
                            setActionButtons();
                        }
                    );
                });

                const getOnMainClick = () => {
                    return async () => {
                        await toggleAdminOverride(context, loaded, insightsContext);
                        const codeInsightContext = await getCodeInsightExtensionsContext(context);
                        setInsightsContext(codeInsightContext);
                        setLoaded(true);
                        setUpdated(true);
                        setActionButtons();
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

                return (
                    <div data-testid="modal-with-action-callback">
                        {insightsContext && (
                            <AdminOverrideStatus
                                isOverride={
                                    insightsContext.userAdmin && insightsContext.adminOverride
                                }
                            />
                        )}
                        {insightsContext && (
                            <ReportStatus codeInsightReports={insightsContext.codeInsightReports} />
                        )}
                        {insightsContext && (
                            <OverviewAdmonition userAdmin={insightsContext.userAdmin} />
                        )}
                    </div>
                );
            };

            renderElementAsReact(modalApi, ModalComponent);
        },
    };
});

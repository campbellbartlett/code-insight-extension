import { ModalExtension, renderElementAsReact } from '@atlassian/clientside-extensions';
import React from 'react';
import { Context, PluginAPI } from '@atlassian/clientside-extensions-registry/lib/types';
import { ModalAction } from '@atlassian/clientside-extensions-components/lib/handlers/ModalHandler';
import { ModalAppearance } from '@atlassian/clientside-extensions-components/dist/handlers/ModalHandler';
import OverviewModal from './OverviewModal';

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

            const createCloseButton = (updated: boolean) => ({
                text: 'Close',
                onClick: () => {
                    modalApi.closeModal();
                    if (updated) {
                        // Refresh the page to show new status of merge button
                        window.location.reload();
                    }
                },
            });

            const setActions = (actions: ModalAction[]) => {
                modalApi.setActions(actions);
            };

            const setIsOverride = (isOverride: boolean) => {
                if (isOverride) {
                    modalApi.setAppearance(ModalAppearance.warning);
                    return;
                }
                modalApi.setAppearance(ModalAppearance.danger);
            };

            renderElementAsReact(modalApi, () => (
                <OverviewModal
                    context={context}
                    setActions={setActions}
                    createCloseButton={createCloseButton}
                    setIsOverride={setIsOverride}
                />
            ));
        },
    };
});

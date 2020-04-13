import {ModalExtension} from '@atlassian/clientside-extensions';
import {ModalAppearance, ModalWidth} from "@atlassian/clientside-extensions-components/dist/handlers/ModalHandler";

/**
 * @clientside-extension
 * @extension-point bitbucket.ui.pullrequest.overview.summary
 */
export default ModalExtension.factory((extensionAPI, context) => {
    function getModalContent(param?) {
        return `<h1>This is my modal: ${param}</h1>`;
    }

    return {
        label: 'Open Modal',
        onAction(modalAPI: ModalExtension.Api) {
            modalAPI.setTitle('A cool modal with JS');
            modalAPI.setWidth(ModalWidth.small);
            modalAPI.setAppearance(ModalAppearance.warning);

            modalAPI.onMount(container => {
                // append your content for the modal
                container.innerHTML = getModalContent();

                // set actions for the modal
                modalAPI.setActions([
                    {
                        text: 'Primary',
                        onClick: () => {
                            primaryAction().then(() => {
                                modalAPI.closeModal();
                            });
                        },
                    },
                    {
                        text: 'Secondary',
                        onClick: () => {
                            container.innerHTML = getModalContent({ secondaryClicked: true });
                        },
                    },
                ]);

                fetch('/rest/code-insight-extension/1.0/pullRequest/TP/docker-compose-for-bitbucket/3/insightReportStatus/theKey', {
                    method: 'GET',
                    headers: {
                        'Content-Type': 'application/json'
                    }
                }).then(json => console.log(json))
                    .catch(err => console.error(err));

                // listen when the modal is about to be closed by the user
                modalAPI.onClose(() => {
                    return confirm('Are you sure you want to close this modal?');
                });
            });
        },
    };
});
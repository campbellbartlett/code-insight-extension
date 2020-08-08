import React from 'react';
import fetchMock from 'jest-fetch-mock';
import { act } from '@testing-library/react';
import { mount, ReactWrapper } from 'enzyme';
import { mountToJson } from 'enzyme-to-json';
import { ModalAction } from '@atlassian/clientside-extensions-components/dist/handlers/ModalHandler';
import OverviewModal from './OverviewModal';

const context = {
    project: {
        key: 'projectKey',
    },
    repository: {
        slug: 'repoSlug',
    },
    pullRequest: {
        fromRef: {
            latestCommit: 'commitHash',
        },
    },
};

const mockReports = [
    {
        name: 'integrationtests',
        status: 'FAIL',
    },
    {
        name: 'blackduck',
        status: 'FAIL',
    },
    {
        name: 'sonar',
        status: 'PASS',
    },
];

const getMockReportResponse = (isUserAdmin: boolean, hasBeenOveridden: boolean) => {
    return `{
              "codeInsightReports": ${JSON.stringify(mockReports)},
              "commitHash": "edc172925cf340b88c499fe401f9597289142b8e",
              "repositorySlug": "docker-compose-for-bitbucket",
              "projectKey": "TP",
              "userAdmin": ${isUserAdmin},
              "adminOverride": ${hasBeenOveridden}
            }`;
};

let actions: ModalAction[] = [];
const setActions = jest.fn((newActions: ModalAction[]) => {
    actions = [...newActions];
});

const createCloseButton = jest.fn((hasBeenUpdated: boolean) => hasBeenUpdated);
const setIsAdminOverride = jest.fn();

async function mountModal() {
    const overviewModal = (
        <OverviewModal
            context={context}
            setActions={setActions}
            createCloseButton={createCloseButton}
            setIsOverride={setIsAdminOverride}
        />
    );

    let container: ReactWrapper;
    await act(async () => {
        container = mount(overviewModal);
    });
    container.update();
    return container;
}

describe('OverView modal renders correctly', () => {
    it('should render correctly', async (done: any) => {
        fetchMock.mockOnce(async () => {
            return Promise.resolve({
                body: getMockReportResponse(false, false),
            });
        });

        const container = await mountModal();

        expect(container).not.toBeNull();
        expect(mountToJson(container)).toMatchSnapshot();

        done();
    });

    it('should fetch and pass admin prop onto children correctly', async (done: any) => {
        fetchMock.mockOnce(async () => {
            return Promise.resolve({
                body: getMockReportResponse(true, false),
            });
        });

        const container = await mountModal();

        expect(container).not.toBeNull();
        expect(container.find('OverviewAdmonition').props().userAdmin).toBeTruthy();

        done();
    });

    it('should fetch and pass override prop onto children correctly', async (done: any) => {
        fetchMock.mockOnce(async () => {
            return Promise.resolve({
                body: getMockReportResponse(false, true),
            });
        });

        const container = await mountModal();

        expect(container).not.toBeNull();
        expect(container.find('AdminOverrideStatus').props().isOverride).toBeTruthy();

        done();
    });

    it('should fetch and pass reports prop onto children correctly', async (done: any) => {
        fetchMock.mockOnce(async () => {
            return Promise.resolve({
                body: getMockReportResponse(false, true),
            });
        });

        const container = await mountModal();

        expect(container).not.toBeNull();
        expect(container.find('ReportStatus').props().codeInsightReports).toEqual(mockReports);

        done();
    });

    it('should show the correct text on the override button when merging is disabled', async (done: any) => {
        fetchMock.mockOnce(async () => {
            return Promise.resolve({
                body: getMockReportResponse(true, false),
            });
        });

        const container = await mountModal();

        expect(container).not.toBeNull();
        expect(actions.length).toBe(2);
        // Expect one of the actions to be the button with text 'enable merging'
        expect(actions.some(action => action.text === 'Enable merging')).toBeTruthy();
        // Expect the other action to be the result of passing false into the createCloseButton mocked function
        expect(actions.some(action => action === false)).toBeTruthy();

        done();
    });

    it('should show the correct text on the override button when merging is enabled', async (done: any) => {
        fetchMock.mockOnce(async () => {
            return Promise.resolve({
                body: getMockReportResponse(true, true),
            });
        });

        const container = await mountModal();

        expect(container).not.toBeNull();
        expect(actions.length).toBe(2);
        // Expect one of the actions to be the button with text 'enable merging'
        expect(actions.some(action => action.text === 'Disable merge override')).toBeTruthy();
        // Expect the other action to be the result of passing false into the createCloseButton mocked function
        expect(actions.some(action => action === false)).toBeTruthy();

        done();
    });

    it('should show the override button as disabled when the user is not admin', async (done: any) => {
        fetchMock.mockOnce(async () => {
            return Promise.resolve({
                body: getMockReportResponse(false, true),
            });
        });

        const container = await mountModal();

        expect(container).not.toBeNull();
        expect(actions.length).toBe(2);
        // Expect that one of the action buttons is disabled
        expect(actions.find(action => action.isDisabled)).not.toBeNull();
        // Expect the other action to be the result of passing false into the createCloseButton mocked function
        expect(actions.some(action => action === false)).toBeTruthy();

        done();
    });

    it('should switch override button text and style when toggling admin authority to merge', async (done: any) => {
        fetchMock.mockOnce(async () => {
            return Promise.resolve({
                body: getMockReportResponse(true, true),
            });
        });

        const container = await mountModal();

        expect(container).not.toBeNull();
        expect(actions.some(action => action.text === 'Disable merge override')).toBeTruthy();

        // Mock the post request to update the admin override
        fetchMock.mockOnce(async () => {
            return Promise.resolve({
                body: {},
            });
        });

        // Mock the next fetch which gets the latest insight reports from the backend
        fetchMock.mockOnce(async () => {
            return Promise.resolve({
                body: getMockReportResponse(true, false),
            });
        });

        await act(async () => {
            // When performing the onClick function, update the status of the admin override
            // in the backend, refetch the latest reports and update the UI
            const button = actions.find(action => action.text === 'Disable merge override');
            expect(button).not.toBeNull();
            button.onClick();
        });

        expect(actions.length).toBe(2);
        // Expect that one of the action buttons is 'Enable Merging'
        expect(actions.some(action => action.text === 'Enable merging')).toBeTruthy();
        // Expect the other action to be the result of passing true into the createCloseButton mocked function
        expect(actions.some(action => action)).toBeTruthy();

        done();
    });
});

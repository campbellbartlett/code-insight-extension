import React from 'react';
import fetchMock from 'jest-fetch-mock';
import { act } from '@testing-library/react';
import { mount, ReactWrapper } from 'enzyme';
import { mountToJson } from 'enzyme-to-json';
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

const getMockReportRespose = (isUserAdmin: boolean, hasBeenOveridden: boolean) => {
    return `{
              "codeInsightReports": ${JSON.stringify(mockReports)},
              "commitHash": "edc172925cf340b88c499fe401f9597289142b8e",
              "repositorySlug": "docker-compose-for-bitbucket",
              "projectKey": "TP",
              "userAdmin": ${isUserAdmin},
              "adminOverride": ${hasBeenOveridden}
            }`;
};

const setActions = jest.fn();
const createCloseButton = jest.fn();
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
                body: getMockReportRespose(false, false),
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
                body: getMockReportRespose(true, false),
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
                body: getMockReportRespose(false, true),
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
                body: getMockReportRespose(false, true),
            });
        });

        const container = await mountModal();

        expect(container).not.toBeNull();
        expect(container.find('ReportStatus').props().codeInsightReports).toEqual(mockReports);

        done();
    });
});

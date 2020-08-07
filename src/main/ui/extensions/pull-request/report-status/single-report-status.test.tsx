import React from 'react';
import { shallow } from 'enzyme';
import shallowToJson from 'enzyme-to-json';
import SingleReportStatus from './single-report-status';
import { CodeInsightReport } from '../PullRequestInsightsContext';
import PassedStatus from './passed-status';
import WaitingStatus from './waiting-status';
import FailedStatus from './failed-status';

const testReportTitle = 'Test Report';

const passedReport: CodeInsightReport = {
    name: testReportTitle,
    status: 'PASS',
};

const waitingReport: CodeInsightReport = {
    name: testReportTitle,
    status: 'WAITING',
};

const failedReport: CodeInsightReport = {
    name: testReportTitle,
    status: 'FAIL',
};

describe('single report status shows correct data', () => {
    it('should render passed status correctly', () => {
        const notePreview = shallow(<SingleReportStatus report={passedReport} />);

        expect(notePreview).not.toBeNull();
        expect(notePreview.contains(<PassedStatus />)).toBeTruthy();
        expect(notePreview.find('#report-name').text()).toEqual(testReportTitle);
    });

    it('should render waiting status correctly', () => {
        const notePreview = shallow(<SingleReportStatus report={waitingReport} />);

        expect(notePreview).not.toBeNull();
        expect(notePreview.contains(<WaitingStatus />)).toBeTruthy();
        expect(notePreview.find('#report-name').text()).toEqual(testReportTitle);
    });

    it('should render failed status correctly', () => {
        const notePreview = shallow(<SingleReportStatus report={failedReport} />);

        expect(notePreview).not.toBeNull();
        expect(notePreview.contains(<FailedStatus />)).toBeTruthy();
        expect(notePreview.find('#report-name').text()).toEqual(testReportTitle);
    });

    it('should match snapshot', () => {
        const notePreview = shallow(<SingleReportStatus report={passedReport} />);

        expect(notePreview).not.toBeNull();
        expect(shallowToJson(notePreview)).toMatchSnapshot();
    });
});

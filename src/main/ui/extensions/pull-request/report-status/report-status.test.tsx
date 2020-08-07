import React from 'react';
import { shallow } from 'enzyme';
import shallowToJson from 'enzyme-to-json';
import { ReportStatus } from './report-status';
import { CodeInsightReport } from '../PullRequestInsightsContext';

const insightReports: Array<CodeInsightReport> = [
    {
        status: 'PASS',
        name: 'Report 1',
    },
    {
        status: 'FAIL',
        name: 'Report 2',
    },
    {
        status: 'WAITING',
        name: 'Report 3',
    },
];

describe('report status component shows correct data', () => {
    it('should render correctly', () => {
        const reportStatus = shallow(<ReportStatus codeInsightReports={insightReports} />);

        expect(shallowToJson(reportStatus)).toMatchSnapshot();
    });

    it('should render the correct number of report statuses', () => {
        const reportStatus = shallow(<ReportStatus codeInsightReports={insightReports} />);

        const reportStatusItems = reportStatus.find('li');
        expect(reportStatusItems.length).toEqual(3);

        expect(reportStatus.find('#Report1').length).toEqual(1);
        expect(reportStatus.find('#Report2').length).toEqual(1);
        expect(reportStatus.find('#Report3').length).toEqual(1);
    });
});

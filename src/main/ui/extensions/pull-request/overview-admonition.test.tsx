import React from 'react';
import { shallow } from 'enzyme';
import shallowToJson from 'enzyme-to-json';
import { OverviewAdmonition } from './overview-admonition';

describe('overview admonition renders correctly', () => {
    it('should render correctly', () => {
        const overviewAdmonition = shallow(<OverviewAdmonition userAdmin />);

        expect(shallowToJson(overviewAdmonition)).toMatchSnapshot();
    });

    it('should show correct message for admin', () => {
        const overviewAdmonition = shallow(<OverviewAdmonition userAdmin />);

        expect(overviewAdmonition.find('Admonition').length).toEqual(1);
    });

    it('should not show a message for non-admin', () => {
        const overviewAdmonition = shallow(<OverviewAdmonition userAdmin={false} />);

        expect(overviewAdmonition.find('Admonition').length).toEqual(0);
    });
});

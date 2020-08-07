import React from 'react';
import { shallow } from 'enzyme';
import shallowToJson from 'enzyme-to-json';
import { AdminOverrideStatus } from './admin-override-status';

describe('admin override status renders correctly', () => {
    it('should render correctly', () => {
        const adminOverrideStatus = shallow(<AdminOverrideStatus isOverride />);

        expect(shallowToJson(adminOverrideStatus)).toMatchSnapshot();
    });

    it('should show correct message when admin', () => {
        const adminOverrideStatus = shallow(<AdminOverrideStatus isOverride />);

        expect(adminOverrideStatus.find('#admin-message').length).toEqual(1);
        expect(adminOverrideStatus.find('#standard-message').length).toEqual(0);
    });

    it('should show correct message when not an admin', () => {
        const adminOverrideStatus = shallow(<AdminOverrideStatus isOverride={false} />);

        expect(adminOverrideStatus.find('#admin-message').length).toEqual(0);
        expect(adminOverrideStatus.find('#standard-message').length).toEqual(1);
    });
});

import React from 'react';
import { shallow } from 'enzyme';
import shallowToJson from 'enzyme-to-json';
import StatusInParagraph from './status-in-paragraph';

describe('status in paragraph renders correctly', () => {
    it('should match snapshot', () => {
        const statusInParagraph = shallow(<StatusInParagraph text="Test Status" color="red" />);

        expect(statusInParagraph).not.toBeNull();
        expect(shallowToJson(statusInParagraph)).toMatchSnapshot();
    });
});

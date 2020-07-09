import React from 'react';
import PropTypes from 'prop-types';
import Admonition from 'react-admonitions';

export const OverviewAdmonition = ({ userAdmin }) => (
    <div>
        {userAdmin && (
            <Admonition type="tip">
                As an administrator of this repository you can enable merging of this pull request
                before all code insight quality reports have passed.
            </Admonition>
        )}
    </div>
);

OverviewAdmonition.propTypes = {
    userAdmin: PropTypes.bool,
};

OverviewAdmonition.defaultProps = {
    userAdmin: false,
};

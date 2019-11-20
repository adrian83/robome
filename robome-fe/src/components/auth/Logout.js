import React from 'react';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';
import { Redirect } from 'react-router-dom';

import Base from '../Base';


class Logout extends Base {

    static propTypes = {
        authToken: PropTypes.string,
        onLogout: PropTypes.func
    };

    componentDidMount() {
        // TODO send signout request to backend
        this.props.onLogout();
    }

    render() {
        return <Redirect to='/' />
    }
}

const mapStateToProps = (state) => {
    return { authToken: state.authToken };
};

const mapDispatchToProps = (dispatch) => {
    return {
        onLogout: () => dispatch({ type: 'REMOVE_JWT_TOKEN' })
    }
};

Logout = connect(mapStateToProps, mapDispatchToProps)(Logout);

export default Logout;

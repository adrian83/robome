import React from 'react';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';
import { Redirect } from 'react-router-dom';

import Base from '../Base';


class Logout extends Base {

    static propTypes = {
        authToken: PropTypes.string,
        onLogout: PropTypes.func,
        userId: PropTypes.string
    };

    componentDidMount() {   
        this.props.onLogout();
    }

    render() {
        return <Redirect to='/' />
    }
}

const mapStateToProps = (state) => {
    return {
        authToken: state.authToken,
        userId: state.userId
    };
};

const mapDispatchToProps = (dispatch) => {
    return {
        onLogout: () => dispatch({ type: 'REMOVE_JWT_TOKEN' })
    }
};

Logout = connect(mapStateToProps, mapDispatchToProps)(Logout);

export default Logout;

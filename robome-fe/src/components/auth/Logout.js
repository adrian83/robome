import React, { Component } from 'react';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';
import { Redirect } from 'react-router-dom';


class Logout extends Component {

    static propTypes = {
        jwtToken: PropTypes.string,
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
    return { jwtToken: state.jwtToken };
};

const mapDispatchToProps = (dispatch) => {
    return {
        onLogout: () => dispatch({ type: 'REMOVE_JWT_TOKEN' })
    }
};

Logout = connect(mapStateToProps, mapDispatchToProps)(Logout);


export default Logout;

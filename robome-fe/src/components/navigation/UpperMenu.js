import React, { Component } from 'react';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';
import { Link } from 'react-router-dom';

import { listTablesUrl, loginUrl, logoutUrl, registerUrl, healthUrl } from '../../web/url';

const loginLink = <Link className="btn btn-outline-primary" to={loginUrl()}>Login</Link>;
const logoutLink = <Link className="btn btn-outline-primary" to={logoutUrl()}>Logout</Link>;
const tablesListLink = <Link key="1" className="p-2 text-dark" to={listTablesUrl()}>List tables</Link>
const registerLink = <Link key="1" className="p-2 text-dark" to={registerUrl()}>Register</Link>;
const healthLink = <Link key="0" className="p-2 text-dark" to={healthUrl()}>Health</Link>;

class UpperMenu extends Component {

    static propTypes = {
        authToken: PropTypes.string
    };

    render() {

        const authenticated = this.props.authToken;
        const highlitedLink = authenticated ? logoutLink : loginLink; 

        var links = [];

        if(authenticated){
            links.push(tablesListLink);
        } else {
            links.push(registerLink);
        }

        return (
            <div className="d-flex flex-column flex-md-row align-items-center p-3 px-md-4 mb-3 bg-white border-bottom shadow-sm">
                <h5 className="my-0 mr-md-auto font-weight-normal">robome</h5>
                <nav className="my-2 my-md-0 mr-md-3">
                    {healthLink}
                    {links}
                </nav>
                {highlitedLink}
            </div>
        );
    }
}

const mapStateToProps = (state) => {
    return { authToken: state.authToken };
};

const mapDispatchToProps = (dispatch) => {
    return {}
};

UpperMenu = connect(mapStateToProps, mapDispatchToProps)(UpperMenu);

export default UpperMenu;

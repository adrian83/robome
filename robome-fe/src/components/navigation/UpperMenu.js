import React, { Component } from 'react';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';
import { Link } from 'react-router-dom';

class UpperMenu extends Component {

    static propTypes = {
        authenticated: PropTypes.bool
    };

    render() {

        const authenticated = this.props.authenticated;
        
        var highlitedLink = authenticated ? <Link className="btn btn-outline-primary" to="/logout/">Logout</Link> : <Link className="btn btn-outline-primary" to="/login/">Login</Link>; 

        var links = [];
        if(authenticated){
            links.push(<Link key="1" className="p-2 text-dark" to="/tables/list/">List tables</Link>);
        } else {
            links.push(<Link key="1" className="p-2 text-dark" to="/register">Register</Link>);
        }

        return (
            <div className="d-flex flex-column flex-md-row align-items-center p-3 px-md-4 mb-3 bg-white border-bottom shadow-sm">
                <h5 className="my-0 mr-md-auto font-weight-normal">robome</h5>
                <nav className="my-2 my-md-0 mr-md-3">
                    <Link key="0" className="p-2 text-dark" to="/health/">Health</Link>
                    {links}
                </nav>
                {highlitedLink}
            </div>
        );
    }
}

const mapStateToProps = (state) => {
    return { authenticated: state.authenticated };
};

const mapDispatchToProps = (dispatch) => {
    return {}
};

UpperMenu = connect(mapStateToProps, mapDispatchToProps)(UpperMenu);

export default UpperMenu;

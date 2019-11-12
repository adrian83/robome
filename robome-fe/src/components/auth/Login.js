import React, { Component } from 'react';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';
import { Redirect } from 'react-router-dom';

import Error from '../error/Error';
import Title from '../tiles/Title';


import { login } from '../../web/ajax';

class Login extends Component {

    static propTypes = {
        authenticated: PropTypes.bool,
        onLogin: PropTypes.func
    };

    constructor(props) { 
        super(props);

        this.state = {email: '', password: ''};

        this.handleSubmit = this.handleSubmit.bind(this);

        this.handleEmailChange = this.handleEmailChange.bind(this);
        this.handlePasswordChange = this.handlePasswordChange.bind(this);

        this.hideError = this.hideError.bind(this);
    }

    handleEmailChange(event) {
        this.setState({email: event.target.value});
    }

    handlePasswordChange(event) {
        this.setState({password: event.target.value});
    }

    handleSubmit(event) {

        const self = this;
        const backendHost = process.env.REACT_APP_BACKEND_HOST;

        var form = {
            email: this.state.email,
            password: this.state.password
        }

        login(backendHost + "/auth/login", form)
            .then(request => self.props.onLogin(true))
            .catch(error => self.setState({error: error}));

        // unsecuredPost(backendHost + "/auth/login", form)
        //     .then(function(response){
        //         var authToken = response.headers.get('Authorization');
        //         console.log(authToken);
        //         self.props.onLogin(authToken);
        //     })
        //     .catch(error => self.setState({error: error}));

        event.preventDefault();
    }

    isErrorPresent(){
        return this.state.error && this.state.error !== {};
    }

    hideError(event){
        this.setState({error: null});
        event.preventDefault();
    }

    showError(){
        return this.isErrorPresent() ? (<Error error={this.state.error} onClose={this.hideError}></Error>) : "";
    }

    render() {

        if (this.props.authenticated) {
            return <Redirect to='/' />
        }

        return (
            <div>
                <Title title="Login" description="login"></Title>

                <div>{this.showError()}</div>

                <form onSubmit={this.handleSubmit}>

                    <div className="form-group">

                        <label htmlFor="emailInput">Email address</label>

                        <input type="email" 
                                className="form-control" 
                                id="emailInput" 
                                aria-describedby="emailHelp" 
                                placeholder="Enter email" 
                                value={this.state.email}
                                onChange={this.handleEmailChange} />

                    </div>

                    <div className="form-group">

                        <label htmlFor="passwordInput">Password</label>

                        <input type="password" 
                                className="form-control" 
                                id="passwordInput" 
                                placeholder="Password" 
                                value={this.state.password}
                                onChange={this.handlePasswordChange}/>
                    </div>

                    <button type="submit" 
                            className="btn btn-primary">Login</button>

                </form>
            </div>

        );
    }
}

const mapStateToProps = (state) => {
    return { authenticated: state.authenticated };
};

const mapDispatchToProps = (dispatch) => {
    return {
        onLogin: (authenticated) => dispatch({ type: 'STORE_JWT_TOKEN', authenticated: authenticated })
    }
};

Login = connect(mapStateToProps, mapDispatchToProps)(Login);

export default Login;

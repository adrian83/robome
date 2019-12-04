import React from 'react';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';
import { Redirect } from 'react-router-dom';

import Error from '../notification/Error';
import Title from '../tiles/Title';
import Base from '../Base';

import { unsecuredPost } from '../../web/ajax';
import { loginBeUrl } from '../../web/url';


class Login extends Base {

    static propTypes = {
        authToken: PropTypes.string,
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

        var form = {
            email: this.state.email,
            password: this.state.password
        }

        unsecuredPost(loginBeUrl(), form)
            .then(function(response){
                var authToken = response.headers.get('Authorization');
                if(authToken) {
                    console.log(authToken);
                    self.props.onLogin(authToken);
                } else {
                    throw "User is not authenticated";
                }
            })
            .catch(error => self.registerError(error));

        event.preventDefault();
    }

    render() {

        if (this.props.authToken) {
            return <Redirect to='/' />
        }

        return (
            <div>
                <Title title="Login" description="login"></Title>

                <Error errors={this.errors()} hideError={this.hideError} ></Error>

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
    return {authToken: state.authToken};
};

const mapDispatchToProps = (dispatch) => {
    return {
        onLogin: (token) => dispatch({ type: 'STORE_JWT_TOKEN', authToken: token })
    }
};

Login = connect(mapStateToProps, mapDispatchToProps)(Login);

export default Login;

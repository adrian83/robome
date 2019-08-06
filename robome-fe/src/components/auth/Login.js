
import React, { Component } from 'react';
import { connect, Provider } from 'react-redux';
import PropTypes from 'prop-types';

import { Redirect } from 'react-router-dom';

class Login extends Component {

    static propTypes = {
        jwtToken: PropTypes.string,
        onLogin: PropTypes.func
    };

    constructor(props) { 
        super(props);

        this.state = {email: '', password: ''};

        this.handleSubmit = this.handleSubmit.bind(this);

        this.handleEmailChange = this.handleEmailChange.bind(this);
        this.handlePasswordChange = this.handlePasswordChange.bind(this);
    }

    handleEmailChange(event) {
        this.setState({email: event.target.value});
    }

    handlePasswordChange(event) {
        this.setState({password: event.target.value});
    }

    handleSubmit(event) {

        const { jwtToken, onLogin } = this.props;

        var form = {
            email: this.state.email,
            password: this.state.password
        }

        var self = this;

        fetch('http://localhost:6060/auth/login', {
            method: 'POST',
            mode: 'cors',
            body: JSON.stringify(form),
            headers: {
                "Content-type": "application/json; charset=UTF-8"
            }
        })
        .then(function(response){


            console.log("headers", response.headers)
            var authToken = response.headers.get('Authorization');
            console.log("authToken", authToken)

            onLogin(authToken);

            self.setState({toHome: true});
        });

        event.preventDefault();
    }

    render() {

        if (this.state.toHome === true) {
            return <Redirect to='/' />
        }

        const { jwtToken, onLogin } = this.props;

        return (
            <form onSubmit={this.handleSubmit}>

                <div>{jwtToken}</div>

                <div className="form-group">

                    <label htmlFor="emailInput">Email address</label>

                    <input type="email" 
                            className="form-control" 
                            id="emailInput" 
                            aria-describedby="emailHelp" 
                            placeholder="Enter email" 
                            value={this.state.email}
                            onChange={this.handleEmailChange} />

                    <small id="emailHelp" 
                            className="form-text text-muted">We'll never share your email with anyone else.</small>
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
                        className="btn btn-primary">Submit</button>

            </form>
        );
    }
}

const mapStateToProps = (state) => {
    return { jwtToken: state.jwtToken };
};

const mapDispatchToProps = (dispatch) => {
    return {
        onLogin: (jwtToken) => dispatch({ type: 'STORE_JWT_TOKEN', jwtToken: jwtToken })
    }
};

Login = connect(mapStateToProps, mapDispatchToProps)(Login);


export default Login;

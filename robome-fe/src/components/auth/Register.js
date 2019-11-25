import React from 'react';

import Error from '../notification/Error';
import Title from '../tiles/Title';
import Base from '../Base';

import { unsecuredPost } from '../../web/ajax';
import { registerBeUrl } from '../../web/url';

class Register extends Base {

    constructor(props) {
        super(props);

        this.state = {email: '', password1: '', password2: ''};

        this.handleSubmit = this.handleSubmit.bind(this);

        this.handleEmailChange = this.handleEmailChange.bind(this);
        this.handlePassword1Change = this.handlePassword1Change.bind(this);
        this.handlePassword2Change = this.handlePassword2Change.bind(this);

        this.hideError = this.hideError.bind(this);
    }

    handleEmailChange(event) {
        this.setState({email: event.target.value});
    }

    handlePassword1Change(event) {
        this.setState({password1: event.target.value});
    }

    handlePassword2Change(event) {
        this.setState({password2: event.target.value});
    }

    handleSubmit(event) {

        const self = this;

        var form = {
            email: this.state.email,
            password: this.state.password1,
            repeatedPassword: this.state.password2
        }

        unsecuredPost(registerBeUrl(), form)
        .catch(error => self.registerError(error));

        event.preventDefault();
    }

    render() {
        return (
            <div>
                <Title title="Register" description="create account in robome"></Title>

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

                        <small id="emailHelp" 
                                className="form-text text-muted">We'll never share your email with anyone else.</small>
                    </div>
                
                    <div className="form-group">

                        <label htmlFor="password1Input">Password</label>

                        <input type="password" 
                                className="form-control" 
                                id="password1Input" 
                                placeholder="Password" 
                                value={this.state.password1}
                                onChange={this.handlePassword1Change}/>
                    </div>

                    <div className="form-group">

                        <label htmlFor="password2Input">Password</label>

                        <input type="password" 
                                className="form-control" 
                                id="password2Input" 
                                placeholder="Repeat password" 
                                value={this.state.password2}
                                onChange={this.handlePassword2Change}/>
                    </div>

                    <button type="submit" 
                            className="btn btn-primary">Register</button>

                </form>
            </div>
        );
    }
}

export default Register;

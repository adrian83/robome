
import React, { Component } from 'react';

class Register extends Component {

    constructor(props) {
        super(props);

        this.state = {email: '', password1: '', password2: ''};

        this.handleSubmit = this.handleSubmit.bind(this);

        this.handleEmailChange = this.handleEmailChange.bind(this);
        this.handlePassword1Change = this.handlePassword1Change.bind(this);
        this.handlePassword2Change = this.handlePassword2Change.bind(this);
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

        var form = {
            email: this.state.email,
            password: this.state.password1,
            repeatedPassword: this.state.password2
        }

        fetch('http://localhost:6060/auth/register', {
            method: 'POST',
            mode: 'cors',
            body: JSON.stringify(form),
            headers: {
                "Content-type": "application/json; charset=UTF-8"
            }
        });
        //.then(response => response.json())
        //.then(data => console.log(JSON.stringify(data)));


        event.preventDefault();
    }

    render() {
        return (
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
                        className="btn btn-primary">Submit</button>

            </form>
        );
    }
}

export default Register;

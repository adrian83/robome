import React, { Component } from 'react';



class Base extends Component {

    constructor(props) { 
        super(props);

        this.hideError = this.hideError.bind(this);
        this.hideInfo = this.hideInfo.bind(this);
    }

    registerError(err) {
        var errs = this.errors();
        errs.push(err);
        this.setState({errors: errs});
    }

    errors() {
        return this.state && this.state.errors ? this.state.errors : [];
    }

    hideError(errId) {
        var filteredErrors = this.errors().filter((err, index, arr) => err.id !== errId);
        this.setState({errors: filteredErrors});
        
    }

    registerInfo(msg) {
        var info = this.info();
        info.push(msg);
        this.setState({info: info});
    }

    info() {
        return this.state && this.state.info ? this.state.info : [];
    }

    hideInfo(msg) {
        var filteredInfo = this.info().filter((i, index, arr) => i !== msg);
        this.setState({info: filteredInfo});
    }

    render() {
        return (<div></div>);
    }
}

export default Base;

import React, { Component } from 'react';

class Error extends Component {

    constructor(props) { 
        super(props);

        this.hideError = this.hideError.bind(this);
    }

    hideError(event){

        var onClose = this.props.onClose;
        onClose(event);

        event.preventDefault();
    }

    renderError(message, details=[]){

        var i = 0;
        var detailsList = details.map(msg => (<div key={i++}>{msg}</div>));

        return (
            <div className="alert alert-danger alert-dismissible fade show" role="alert">
                <div><b>{message}</b></div>
                {detailsList}
                <button type="button" className="close" aria-label="Close" onClick={this.hideError}>
                    <span aria-hidden="true">&times;</span>
                </button>
            </div>);
    }

    render() {


        if(this.props.error) {
            var error = this.props.error;

            if(error.status) {

                if(error.status === 500) {
                    return this.renderError(error.body);

                } else if(error.status === 400) {
                    var msgs = error.body.map(v => v.message);
                    return this.renderError("Invalida data", msgs);
                }

                return this.renderError(error.body);
            }
        }
        return(<div></div>);
    }
}

export default Error;
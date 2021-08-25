import React, { Component } from 'react';

import uuidv4 from '../../web/uuid';

class Error extends Component {

    constructor(props) { 
        super(props);

        this.hideError = this.hideError.bind(this);
    }

    hideError(errId){
        const self = this;
        return function(event) {
            self.props.hideError(errId);
            event.preventDefault();
        }
    }

    renderError(id, message, details=[]){
        var detailsList = details.map(msg => (<div key={uuidv4()}>{msg}</div>));

        return (
            <div className="alert alert-danger alert-dismissible fade show" role="alert" key={id}>
                <div><b>{message}</b></div>
                {detailsList}
                <button type="button" className="close" aria-label="Close" onClick={this.hideError(id)}>
                    <span aria-hidden="true">&times;</span>
                </button>
            </div>);
    }

    render() {
        if(!this.props || !this.props.errors) {
            return
        }

        var self = this;
        var errors = this.props.errors.map(function(error){ 
            if(error.status) {
                 if(error.status === 400) {
                    var msgs = error.body.map(v => v.message);
                    return self.renderError(error.id, "Invalida data", msgs);
                } 

                return self.renderError(error.id, error.body);
            }
            console.log('error: ', JSON.stringify(error));
            return self.renderError(error.id, "Unknown error, please try again later");
        });
    
        return(<div>{errors}</div>);
    }
}

export default Error;
import React, { Component } from 'react';

import uuidv4 from '../../web/uuid';

class Info extends Component {

    constructor(props) { 
        super(props);

        this.hideInfo = this.hideInfo.bind(this);
    }

    hideInfo(info){
        const self = this;
        return function(event) {
            self.props.hideInfo(info);
            event.preventDefault();
        }
    }

    renderInfo(id, message){
        return (
            <div className="alert alert-info alert-dismissible fade show" role="alert" key={id}>
                <div><b>{message}</b></div>
                <button type="button" className="close" aria-label="Close" onClick={this.hideInfo(message)}>
                    <span aria-hidden="true">&times;</span>
                </button>
            </div>);
    }

    render() {
        if(!this.props || !this.props.info) {
            return
        }

        var self = this;
        var info = this.props.info.map(i => self.renderInfo(uuidv4(), i));
        return(<div>{info}</div>);
    }
}

export default Info;
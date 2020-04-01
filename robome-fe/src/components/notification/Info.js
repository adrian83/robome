import React, { Component } from 'react';

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
        var self = this;
        var id = 1;
        if(this.props && this.props.info) {
            var info = this.props.info.map(i => self.renderInfo(id++, i));
            return(<div>{info}</div>);
        }
        return(<div></div>);
    }
}

export default Info;
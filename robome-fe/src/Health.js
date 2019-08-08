import React, { Component } from 'react';

class Health extends Component {

    constructor(props) {
        super(props);
        this.state = {status: ''};
    }

    componentDidMount() {

        var self = this;

        fetch('http://localhost:6060/health')
            .then(response => response.json())
            .then(data => self.setState({status: data.status}));
    }

    render() {
        if(this.state.status) {
            return (<div>Status: {this.state.status}</div>);
        }
        return (<div>Waiting...</div>);
    }
}

export default Health;

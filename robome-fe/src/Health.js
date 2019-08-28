import React, { Component } from 'react';

import Title from './components/tiles/Title';

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
        var status = (<div>Waiting...</div>);
        if(this.state.status) {
            status = (<div>Status: {this.state.status}</div>);
        }

        return (
            <div>
                <Title title="Backend status" description=""></Title>
                <div>
                    {status}
                </div>
            </div>
        
        );
    }
}

export default Health;

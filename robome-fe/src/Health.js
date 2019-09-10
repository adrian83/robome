import React, { Component } from 'react';

import Title from './components/tiles/Title';

import { unsecuredGet } from './web/ajax';


class Health extends Component {

    componentDidMount() {
        const self = this;
        const backendHost = process.env.REACT_APP_BACKEND_HOST;

        unsecuredGet(backendHost + "/health")
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

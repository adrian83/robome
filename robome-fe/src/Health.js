import React, { Component } from 'react';
import PropTypes from 'prop-types';

import Title from './components/tiles/Title';

import { unsecuredGet } from './web/ajax';


class Health extends Component {

    static propTypes = {
        authToken: PropTypes.string
    };

    componentDidMount() {
        const self = this;

        unsecuredGet("/health")
            .then(response => response.json())
            .then(data => self.setState({status: data.status}));
    }

    render() {
        var status = (<div>Waiting...</div>);
        if(this.state != null && this.state.status) {
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

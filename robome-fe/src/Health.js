import React, { Component } from 'react';

class Health extends Component {

    constructor(props) {
        super(props);
        this.state = {};
    }

    componentDidMount() {

        fetch('http://localhost:6060/health')
            .then(response => response.json())
            .then(data => console.log(JSON.stringify(data)));

    }

    render() {
        return (<div>Health</div>);
    }
}

export default Health;

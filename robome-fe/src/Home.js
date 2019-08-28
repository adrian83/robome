import React, { Component } from 'react';

import Title from './components/tiles/Title';

class Home extends Component {

    render() {
        return (
            <div>
                <Title title="Home" description="welcome"></Title>
                <div>Home</div>
            </div>);
    }
}

export default Home;

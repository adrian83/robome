import React, { Component } from 'react';

class Title extends Component {

    render() {
        return (
            <div className="pricing-header mx-auto text-center">
                <h1 className="display-4">{this.props.title}</h1>
                <p className="lead">{this.props.description}</p>
            </div>);
    }
}

export default Title;
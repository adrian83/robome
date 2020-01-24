import React, { Component } from 'react';

import LinkWithIcon from './LinkWithIcon'

class CreateLink extends Component {

    render() {
        return (<LinkWithIcon 
                    icon="fas fa-plus-square" 
                    to={this.props.to} 
                    text={this.props.text}
                    onClick={this.props.onClick}></LinkWithIcon>);
    }
}

export default CreateLink;
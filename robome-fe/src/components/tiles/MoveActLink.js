import React, { Component } from 'react';

import LinkWithIcon from './LinkWithIcon'

class MoveActLink extends Component {

    render() {
        return (<LinkWithIcon 
                    icon="fas fa-reply" 
                    to={this.props.to} 
                    text={this.props.text}
                    onClick={this.props.onClick}></LinkWithIcon>);
    }
}

export default MoveActLink;
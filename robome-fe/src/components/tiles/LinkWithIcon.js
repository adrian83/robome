import React, { Component } from 'react';
import { Link } from 'react-router-dom';

class LinkWithIcon extends Component {

    render() {
        return (
            <span style={{marginLeft: '10px', marginRight: '10px'}} >
                <Link to={this.props.to} 
                        onClick={this.props.onClick}>
                    <i class={this.props.icon}>{this.props.text}</i>
                </Link>
            </span>);
    }
}

export default LinkWithIcon;
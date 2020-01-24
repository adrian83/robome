import React, { Component } from 'react';
import { Link } from 'react-router-dom';

class LinkWithIcon extends Component {

    render() {
        return (
            <span>
                <Link to={this.props.to} 
                        onClick={this.props.onClick}>
                    <i class={this.props.icon}>&nbsp;&nbsp;{this.props.text}&nbsp;&nbsp;</i>
                </Link>
            </span>);
    }
}

export default LinkWithIcon;
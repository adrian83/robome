import React, { Component } from 'react';
import { Link } from 'react-router-dom';

var margin = {marginLeft: '10px', marginRight: '10px'}

class LinkWithIcon extends Component {

    render() {
        const to = this.props.to ? this.props.to : "";
        const text = this.props.text ? (<span style={margin} >{this.props.text}</span>) : "";

        return (
            <span style={margin} >
                <Link 
                    to={to} 
                    onClick={this.props.onClick}>
                    <i className={this.props.icon}>{text}</i>
                </Link>
            </span>);
    }
}

export default LinkWithIcon;
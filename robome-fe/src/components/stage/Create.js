import React, { Component } from 'react';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';
import { Redirect, Link } from 'react-router-dom';

import { securedPost } from '../../web/ajax';


class CreateStage extends Component {

    static propTypes = {
        jwtToken: PropTypes.string
    };

    constructor(props) { 
        super(props);

        this.state = {name: ''};

        this.handleSubmit = this.handleSubmit.bind(this);
        this.handleNameChange = this.handleNameChange.bind(this);
    }

    handleNameChange(event) {
        this.setState({name: event.target.value});
    }


    handleSubmit(event) {

        const self = this;
        const backendHost = process.env.REACT_APP_BACKEND_HOST;
        const jwtToken = this.props.jwtToken;
        const tableId = this.props.match.params.tableId;

        const editUrl = backendHost + "/tables/" + tableId + "/stages" 

        var form = {
            name: this.state.name
        };

        securedPost(editUrl, jwtToken, form)
            .then(response => response.json())
            .then(data => self.setState({key: data.key}))
            .catch(error => self.setState({error: error}));
            
        event.preventDefault();
    }

    render() {

        if(this.state.key && this.state.key.tableId && this.state.key.stageId) {
            var editUrl = "/tables/show/" + this.state.key.tableId + "/stages/edit/" + this.state.key.stageId;
            return (<Redirect to={editUrl} />);
        }

        var tableId = this.props.match.params.tableId
        var showTableUrl = "/tables/show/" + tableId;

        return (
            <div>
                <div>
                    <Link to={showTableUrl}>return to table</Link>
                </div>

                <form onSubmit={this.handleSubmit}>

                    <div className="form-group">

                        <label htmlFor="nameInput">Name</label>

                        <input type="name" 
                                className="form-control" 
                                id="nameInput" 
                                placeholder="Enter name" 
                                value={this.state.name}
                                onChange={this.handleNameChange} />
                    </div>

                    <button type="submit" 
                            className="btn btn-primary">Submit</button>

                </form>
            </div>

        );
    }
}

const mapStateToProps = (state) => {
    return { jwtToken: state.jwtToken };
};

const mapDispatchToProps = (dispatch) => {
    return {}
};

CreateStage = connect(mapStateToProps, mapDispatchToProps)(CreateStage);


export default CreateStage;

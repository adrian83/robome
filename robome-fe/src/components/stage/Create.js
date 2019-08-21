import React, { Component } from 'react';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';
import { Redirect } from 'react-router-dom';

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

        event.preventDefault();

        const jwtToken = this.props.jwtToken;
        const tableId = this.props.match.params.tableId;
        const editUrl = "http://localhost:6060/tables/" + tableId + "/stages" 

        var form = {
            name: this.state.name
        };

        var self = this;

        fetch(editUrl, {
            method: 'POST',
            mode: 'cors',
            body: JSON.stringify(form),
            headers: {
                "Content-type": "application/json; charset=UTF-8",
                "Authorization": jwtToken
            }
        })
        .then(response => response.json())
        .then(data => self.setState({key: data.key}))
        .catch(error => console.log("creating stage error", error));

        
    }

    render() {

        if(this.state.key && this.state.key.tableId && this.state.key.stageId) {
            var editUrl = "/tables/show/" + this.state.key.tableId + "/stages/edit/" + this.state.key.stageId;
            return (<Redirect to={editUrl} />);
        }

        return (
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

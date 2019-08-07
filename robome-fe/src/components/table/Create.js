import React, { Component } from 'react';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';


class CreateTable extends Component {

    static propTypes = {
        jwtToken: PropTypes.string
    };

    constructor(props) { 
        super(props);

        this.state = {title: '', description: ''};

        this.handleSubmit = this.handleSubmit.bind(this);

        this.handleTitleChange = this.handleTitleChange.bind(this);
        this.handleDescriptionChange = this.handleDescriptionChange.bind(this);
    }

    handleTitleChange(event) {
        this.setState({title: event.target.value});
    }

    handleDescriptionChange(event) {
        this.setState({description: event.target.value});
    }

    handleSubmit(event) {

        const { jwtToken } = this.props;

        var form = {
            title: this.state.title,
            description: this.state.description
        }

        fetch('http://localhost:6060/tables', {
            method: 'POST',
            mode: 'cors',
            body: JSON.stringify(form),
            headers: {
                "Content-type": "application/json; charset=UTF-8",
                "Authorization": jwtToken
            }
        })
        .then(function(response){
            console.log("headers", response.headers)
            var authToken = response.headers.get('Authorization');
            console.log("authToken", authToken)
        });

        event.preventDefault();
    }

    render() {

        const { jwtToken } = this.props;

        return (
            <form onSubmit={this.handleSubmit}>

                <div>{jwtToken}</div>

                <div className="form-group">

                    <label htmlFor="titleInput">Title</label>

                    <input type="title" 
                            className="form-control" 
                            id="titleInput" 
                            placeholder="Enter title" 
                            value={this.state.title}
                            onChange={this.handleTitleChange} />
                </div>
            
                <div className="form-group">

                    <label htmlFor="descriptionInput">Title</label>

                    <input type="description" 
                            className="form-control" 
                            id="descriptionInput" 
                            placeholder="Enter description" 
                            value={this.state.description}
                            onChange={this.handleDescriptionChange} />
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

CreateTable = connect(mapStateToProps, mapDispatchToProps)(CreateTable);


export default CreateTable;

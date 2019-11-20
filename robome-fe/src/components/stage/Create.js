import React from 'react';
import { connect } from 'react-redux';
import { Redirect } from 'react-router-dom';
import PropTypes from 'prop-types';

import TableLink from '../navigation/TableLink';
import Error from '../notification/Error';
import Title from '../tiles/Title';
import Base from '../Base';

import { securedPost } from '../../web/ajax';
import { stagesBeUrl, editStageUrl } from '../../web/url';


class CreateStage extends Base {

    static propTypes = {
        authToken: PropTypes.string
    };

    constructor(props) { 
        super(props);

        this.state = {name: ''};

        this.handleSubmit = this.handleSubmit.bind(this);
        this.handleNameChange = this.handleNameChange.bind(this);
        this.hideError = this.hideError.bind(this);
    }

    handleNameChange(event) {
        this.setState({name: event.target.value});
    }

    handleSubmit(event) {

        const self = this;
        const authToken = this.props.authToken;
        const editStgUrl = stagesBeUrl(this.props.match.params.tableId);

        var form = {
            name: this.state.name
        };

        securedPost(editStgUrl, authToken, form)
            .then(response => response.json())
            .then(data => self.setState({key: data.key}))
            .catch(error => self.registerError(error));
            
        event.preventDefault();
    }

    render() {

        if(this.state && this.state.key) {
            var editStgUrl = editStageUrl(
                this.state.key.tableId, 
                this.state.key.stageId);

            return (<Redirect to={editStgUrl} />);
        }

        return (
            <div>
                <Title title="Create new stage" description="Fill basic data"></Title>

                <Error errors={this.errors()} hideError={this.hideError} ></Error>

                <div>
                    <TableLink text="show table" tableId={this.props.match.params.tableId}></TableLink>
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
    return {authToken: state.authToken};
};

const mapDispatchToProps = (dispatch) => {
    return {}
};

CreateStage = connect(mapStateToProps, mapDispatchToProps)(CreateStage);


export default CreateStage;

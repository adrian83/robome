import React from 'react';
import { connect } from 'react-redux';
import { Redirect } from 'react-router-dom';
import PropTypes from 'prop-types';

import Error from '../notification/Error';
import BackLink from '../tiles/BackLink';
import Title from '../tiles/Title';
import Base from '../Base';

import { securedPost } from '../../web/ajax';
import { stagesBeUrl, editStageUrl, showTableUrl } from '../../web/url';


class CreateStage extends Base {

    static propTypes = {
        authToken: PropTypes.string,
        userId: PropTypes.string
    };

    constructor(props) { 
        super(props);

        this.handleSubmit = this.handleSubmit.bind(this);
        this.handleNameChange = this.handleNameChange.bind(this);
        this.hideError = this.hideError.bind(this);
    }

    stageFromState() {
        return (this.state && this.state.stage) ? this.state.stage : {};
    }

    handleNameChange(event) {
        var stage = this.stageFromState();
        stage.title = event.target.value;
        this.setState({stage: stage});
    }

    handleSubmit(event) {
        const self = this;
        const authToken = this.props.authToken;
        const editStgUrl = stagesBeUrl(this.props.match.params.tableId);
        const stage = this.stageFromState();

        securedPost(editStgUrl, authToken, stage)
            .then(response => response.json())
            .then(data => self.setState({stage: data}))
            .catch(error => self.registerError(error));
            
        event.preventDefault();
    }

    render() {
        const stage = this.stageFromState();
        if(stage.key) {
            const editStgUrl = editStageUrl(
                stage.key.tableId, 
                stage.key.stageId);

            return (<Redirect to={editStgUrl} />);
        }

        const showTabUrl = showTableUrl(this.props.match.params.tableId);

        return (
            <div>
                <Title title="Create new stage" description="Fill basic data"></Title>

                <Error errors={this.errors()} hideError={this.hideError} ></Error>

                <div>
                    <BackLink to={showTabUrl} text="show table"></BackLink>
                </div>
                <br/>

                <form onSubmit={this.handleSubmit}>
                    <div className="form-group">
                        <label htmlFor="nameInput">Name</label>
                        <input type="name" 
                                className="form-control" 
                                id="nameInput" 
                                placeholder="Enter name" 
                                value={stage.name}
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
    return {
        authToken: state.authToken,
        userId: state.userId
    };
};

const mapDispatchToProps = (dispatch) => {
    return {}
};

CreateStage = connect(mapStateToProps, mapDispatchToProps)(CreateStage);


export default CreateStage;

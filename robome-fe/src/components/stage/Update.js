import React from 'react';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';

import TableLink from '../navigation/TableLink';
import Error from '../notification/Error';
import Title from '../tiles/Title';
import Base from '../Base';

import securedGet, { securedPut } from '../../web/ajax';
import { stageBeUrl } from '../../web/url';

class UpdateStage extends Base {

    static propTypes = {
        authToken: PropTypes.string
    };

    constructor(props) { 
        super(props);

        this.handleSubmit = this.handleSubmit.bind(this);
        this.handleNameChange = this.handleNameChange.bind(this);
        this.hideError = this.hideError.bind(this);
    }

    handleNameChange(event) {
        var stage = this.state.stage;
        stage.title = event.target.value;
        this.setState({stage: stage});
    }

    handleSubmit(event) {

        const self = this;
        const authToken = this.props.authToken;

        const updateStgUrl = stageBeUrl(
            this.props.match.params.tableId,
            this.props.match.params.stageId); 
        
        const stage = {
            title: self.state.stage.title
        };

        securedPut(updateStgUrl, authToken, stage)
            .then(response => response.json())
            .then(data => self.setState({stage: data}))
            .catch(error => self.registerError(error));

        event.preventDefault();
    }

    componentDidMount() {

        const self = this;
        const authToken = this.props.authToken;
        
        const getStgUrl = stageBeUrl(
            this.props.match.params.tableId,
            this.props.match.params.stageId);

        securedGet(getStgUrl, authToken)
            .then(response => response.json())
            .then(data => self.setState({stage: data}))
            .catch(error => self.registerError(error));
    }

    render() {

        if(!this.state || ! this.state.stage){
            return (<div>waiting for data</div>);
        }

        return (
            <div>
                <Title title={this.state.stage.title} description=""></Title>

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
                                value={this.state.stage.title}
                                onChange={this.handleNameChange} />
                    </div>

                    <button type="submit" 
                            className="btn btn-primary">Submit</button>
                </form>
            </div>);
    }
}

const mapStateToProps = (state) => {
    return {authToken: state.authToken};
};

const mapDispatchToProps = (dispatch) => {
    return {};
};

UpdateStage = connect(mapStateToProps, mapDispatchToProps)(UpdateStage);

export default UpdateStage;

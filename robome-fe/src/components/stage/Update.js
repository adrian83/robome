import React, { Component } from 'react';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';

import TableLink from '../navigation/TableLink';
import Error from '../error/Error';
import Title from '../tiles/Title';

import securedGet, { securedPut } from '../../web/ajax';


class UpdateStage extends Component {

    static propTypes = {
        jwtToken: PropTypes.string
    };

    constructor(props) { 
        super(props);

        this.state = {stage: {}};

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
        const jwtToken = this.props.jwtToken;
        const backendHost = process.env.REACT_APP_BACKEND_HOST;
        const tableId = this.props.match.params.tableId;
        const stageId = this.props.match.params.stageId;

        const updateUrl = backendHost + "/tables/" + tableId + "/stages/" + stageId;
        
        securedPut(updateUrl, jwtToken, self.state.stage)
            .then(response => response.json())
            .then(data => self.setState({stage: data}));

        event.preventDefault();
    }

    isErrorPresent(){
        return this.state.error && this.state.error !== {};
    }

    hideError(event){
        this.setState({error: null});
        event.preventDefault();
    }

    showError(){
        return this.isErrorPresent() ? (<Error error={this.state.error} onClose={this.hideError}></Error>) : "";
    }

    componentDidMount() {

        const self = this;
        const jwtToken = this.props.jwtToken;
        const backendHost = process.env.REACT_APP_BACKEND_HOST;
        const tableId = this.props.match.params.tableId;
        const stageId = this.props.match.params.stageId
        
        const getStageUrl = backendHost + "/tables/" + tableId + "/stages/" + stageId;

        securedGet(getStageUrl, jwtToken)
            .then(response => response.json())
            .then(data => self.setState({stage: data}));
    }

    dataAvailable() {
        return this.state.stage.key;
    }

    render() {
        return this.dataAvailable() ? this.renderPage() : this.renderDummyPage();
    }

    renderPage() {
        return (
            <div>
                <Title title={this.state.stage.title} description=""></Title>

                <div>{this.showError()}</div>

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

    renderDummyPage() {
        return (<div>waiting for data</div>);
    }


}

const mapStateToProps = (state) => {
    return { jwtToken: state.jwtToken };
};

const mapDispatchToProps = (dispatch) => {
    return {}
};

UpdateStage = connect(mapStateToProps, mapDispatchToProps)(UpdateStage);


export default UpdateStage;

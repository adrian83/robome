import React from 'react';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';

import Error from '../notification/Error';
import Info from '../notification/Info';
import BackLink from '../tiles/BackLink';
import Title from '../tiles/Title';
import Base from '../Base';

import securedGet, { securedPut } from '../../web/ajax';
import { stageBeUrl, showTableUrl } from '../../web/url';

class UpdateStage extends Base {

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
        const userId = this.props.userId;
        const stage = this.stageFromState();

        const updateStgUrl = stageBeUrl(
            userId,
            this.props.match.params.tableId,
            this.props.match.params.stageId); 
        
        const updatedStage = {
            title: stage.title
        };

        securedPut(updateStgUrl, authToken, updatedStage)
            .then(response => response.json())
            .then(data => self.setState({stage: data}))
            .then(_ => self.registerInfo("Stage updated"))
            .catch(error => self.registerError(error));

        event.preventDefault();
    }

    componentDidMount() {
        const self = this;
        const authToken = this.props.authToken;
        const userId = this.props.userId;
        
        const getStgUrl = stageBeUrl(
            userId,
            this.props.match.params.tableId,
            this.props.match.params.stageId);

        securedGet(getStgUrl, authToken)
            .then(response => response.json())
            .then(data => self.setState({stage: data}))
            .catch(error => self.registerError(error));
    }

    render() {
        const stage = this.stageFromState();
        if(!stage.key){
            return (<div>waiting for data</div>);
        }

        const showTabUrl = showTableUrl(this.props.match.params.tableId);

        return (
            <div>
                <Title title={stage.title} description=""></Title>

                <Error errors={this.errors()} hideError={this.hideError} ></Error>
                <Info info={this.info()} hideInfo={this.hideInfo} ></Info>

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
                                value={stage.title}
                                onChange={this.handleNameChange} />
                    </div>

                    <button type="submit" 
                            className="btn btn-primary">Submit</button>
                </form>
            </div>);
    }
}

const mapStateToProps = (state) => {
    return {
        authToken: state.authToken,
        userId: state.userId
    };
};

const mapDispatchToProps = (dispatch) => {
    return {};
};

UpdateStage = connect(mapStateToProps, mapDispatchToProps)(UpdateStage);

export default UpdateStage;

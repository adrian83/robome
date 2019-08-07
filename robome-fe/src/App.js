import React, { Component } from 'react';
import { BrowserRouter, Route, Redirect, Link, Switch } from 'react-router-dom';
import { Provider } from 'react-redux';

import Home from './Home';
import Health from './Health';
import Login from './components/auth/Login';
import Register from './components/auth/Register';

import CreateTable from './components/table/Create';
import ListTables from './components/table/List';

import { createStore } from 'redux';

import Reducer from './Reducer'; 

import './App.css';

const store = createStore(Reducer, { jwtToken: "" });

class App extends Component {

  render() {

    return (
      <Provider store={store}>
        <BrowserRouter>
          <div className="App">

            <header>
              <ul>
                <li><Link to="/login/">Login</Link></li>
                <li><Link to="/register/">Register</Link></li>
                <li><Link to="/health/">Health</Link></li>
                <li><Link to="/tables/create/">Create table</Link></li>
                <li><Link to="/tables/list/">List tables</Link></li>
              </ul>
            </header>

            <main role="main" className="inner cover">
              <Switch>
                <Route exact path="/" component={Home} />
                <Route path="/login" component={() => <Login/>} />
                <Route path="/register" component={() => <Register/>} />
                <Route path="/health" component={() => <Health/>} />
                <Route path="/tables/list" component={() => <ListTables/>} />
                <Route path="/tables/create" component={() => <CreateTable/>} />
                <Redirect to="/" />
              </Switch>
            </main>

          </div>
        </BrowserRouter>
      </Provider>
    );
  }
}

export default App;

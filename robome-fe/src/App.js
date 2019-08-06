import React, { Component } from 'react';
import logo from './logo.svg';
import './App.css';
import {
  BrowserRouter as Router,
  Route,
  Redirect,
  Link,
  Switch
} from 'react-router-dom';
import { connect, Provider } from 'react-redux';

import Home from './Home';
import Health from './Health';
import Login from './components/auth/Login';
import Register from './components/auth/Register';

import { createStore } from 'redux';

import Reducer from './Reducer'; 

const store = createStore(Reducer, { jwtToken: "abc" });

class App extends Component {
  render() {

    return (
      <Provider store={store}>
      <Router>
    <div className="App">
      <header>
        
        <ul>
          <li><Link to="/login/">Login</Link></li>
          <li><Link to="/register/">Register</Link></li>
          <li><Link to="/health/">Health</Link></li>
        </ul>

      </header>

      <main role="main" className="inner cover">
          
        <Switch>
          <Route exact path="/" component={Home} />
          <Route path="/login" component={() => <Login/>} />
          <Route path="/register" component={() => <Register/>} />
          <Route path="/health" component={() => <Health/>} />
          <Redirect to="/" />
        </Switch>
          
        </main>

    </div>

      </Router>
      </Provider>
    );
  }
}


export default App;

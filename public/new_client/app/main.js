import React from 'react'
import ReactDOM from 'react-dom'
import { Router, Route, hashHistory } from 'react-router'

import App from './app'
import About from './modules/About'
import Feed from './modules/Feed'

ReactDOM.render((
  <Router history={hashHistory}>
    <Route path="/" component={App}/>
    <Route path="/about" component={About}/>
    <Route path="/feeds" component={Feed}>
      <Route path="/feeds/:feedName" component={Feed}/>
    </Route>
  </Router>
), document.getElementById('main'))

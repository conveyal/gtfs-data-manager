import React from 'react'
import $ from 'jquery'

import { Link } from 'react-router'

import NavigationBar from './navbar'
import UserList from './userlist'
import FeedList from './feedlist'
import PermissionData from './permissiondata'

import config from './config'

import { Grid, Row, Col } from 'react-bootstrap'

export default class App extends React.Component {

  constructor(props) {
    super(props)

    this.state = {
      profile : null
    }

    // this.lock = new Auth0Lock(config.auth0ClientID, config.auth0Domain)

    // try {
    //   // Get the user token if we've saved it in localStorage before
    //   var userToken = localStorage.getItem('userToken');

    //   if (userToken) {
    //     this.getProfile(userToken)
    //   }
    // } catch(err) {
    //   console.log(err)
    //   console.log('error retrieving user data from localStorage, clearing and starting over')
    //   this.logOut()
    // }

  }

  logIn () {
    this.lock.show((err, profile, token) => {
      if (err) {
        console.log("There was an error :/", err)
        return
      }

      // save token to localStorage
      localStorage.setItem('userToken', token);

      this.userLoggedIn(token, profile)
    })
  }

  logOut () {
    localStorage.removeItem('userToken')
    window.location.replace('https://' + config.auth0Domain + '/v2/logout?returnTo=' + window.location.href)
  }

  getProfile (token) {
    // retreive the user profile from Auth0
    $.post( "https://" + config.auth0Domain + "/tokeninfo", { id_token: token })
      .done( (profile) => {
        this.userLoggedIn(token, profile)
      })
  }

  userLoggedIn (token, profile) {

    this.permissionData = new PermissionData(profile.app_metadata.datatools)

    console.log('getting feed IDs')
    $.ajax({
      url : config.dataManagerURL + "/api/feedsources",
      data : {
        feedcollection: config.projectID
      },
      headers: {
        'Authorization' : 'Bearer ' + token
      },
      success: (data) => {
        console.log('got feed sources', data)

        // initialize the feed listing, including a catch-all entry at the beginning
        this.feeds = data
        this.feeds.unshift({
          id: "*",
          name: "All Agencies"
        })

        this.setState({
          profile: profile,
          token: token
        })
      },
      error: (err) => {
        console.log('error getting feed sources', err)
      }
    })
  }

  isAdmin () {
    return this.permissionData && this.permissionData.isProjectAdmin(config.projectID)
  }

  render () {

    return (
      <div>
        <NavigationBar
          title = "MTC 511 Transit Data Manager"
          logIn = {this.logIn.bind(this)}
          logOut = {this.logOut.bind(this)}
          profile = {this.state.profile}
        />
        <FeedList/>
      </div>
    )
  }
}

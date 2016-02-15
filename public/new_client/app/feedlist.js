import React from 'react'
import $ from 'jquery'
import moment from 'moment'

import { Panel, Grid, Row, Col, Button, Table, Glyphicon } from 'react-bootstrap'

import CreateUser from './createuser'
import UserSettings from './usersettings'
import PermissionData from './permissiondata'

import config from './config'

import styles from './style.css'

export default class UserList extends React.Component {

  constructor (props) {
    super(props)

    this.state = {
      users: [],
      feeds: []
    }
  }

  componentDidMount () {
    this.fetchFeeds()
  }

  fetchFeeds () {
    $.ajax({
      url : '/api/public/feedsources?feedcollection=06ddd58a-3275-48d6-9b50-1d46c7b24c8e',
      headers: {
//        'Authorization': 'Bearer ' + this.props.token
      }
    }).done((data) => {
      console.log(data);
      var feeds = data;
//      var users = JSON.parse(data).map((user) => {
//        user.permissionData = new PermissionData(user.app_metadata ? user.app_metadata.datatools : null)
//        return user
//      })

      this.setState({
//        users: users,
        feeds: feeds,
        collectionName: feeds[0].feedCollection.name
      })
    })
  }

  updateUser (user, permissions) {

    var dtMetadata = user.app_metadata.datatools
    for(var project of dtMetadata.projects) {
      if (project.project_id === config.projectID) project.permissions = permissions
    }

    var payload = {
      user_id: user.user_id,
      data: dtMetadata
    }

    $.ajax({
      url : '/secured/updateUser',
      data: payload,
      method: 'post',
      headers: {
        'Authorization': 'Bearer ' + this.props.token
      }
    }).done((data) => {
      console.log('update user ok', data)
      this.fetchUsers()
    })
  }

  createUser (email, password, permissions) {
    var projects = []
    projects.push({
      project_id: config.projectID,
      permissions: permissions
    })

    var payload = {
      email: email,
      password: password,
      projects: projects
    }

    $.ajax({
      url : '/secured/createUser',
      data: payload,
      method: 'post',
      headers: {
        'Authorization': 'Bearer ' + this.props.token
      }
    }).done((data) => {
      this.fetchUsers()
    })

  }

  render () {
    return (

      <Grid>
        <Row>
          <Col xs={12}>
            <h2>List of Feeds</h2>
          </Col>
        </Row>

        <Row>
          <Col xs={12}>
            <h3>{this.state.collectionName}</h3>
          </Col>
        </Row>
        <Table striped hover>
          <thead>
            <tr>
              <th>Name</th>
              <th>Updated</th>
              <th>Link</th>
            </tr>
          </thead>
          <tbody>
          {this.state.feeds.map((feed, i) => {
            return <FeedRow
              feed={feed}
              key={i}
            />
          })}
          </tbody>
        </Table>
      </Grid>
    )
  }
}

class FeedRow extends React.Component {

  constructor (props) {
    super(props)
    this.state = {
      isEditing : false
    }
  }

  render () {
    var buttons;
    if (this.props.feed.url){
      // buttons = <Button {this.props.feed.url ? disabled} href={this.props.feed.url}><Glyphicon glyph="new-window" /></Button>
    }
    return (
      <tr>
        <td>{this.props.feed.name}</td>
        <td>{moment(this.props.feed.lastUpdated).format('MMMM Do YYYY, h:mm:ss a')}</td>
        <td>
          <Button disabled={this.props.feed.url ? false : true} href={this.props.feed.url}><Glyphicon glyph="new-window" /></Button>
          <Button href="http://localhost:9001/"><Glyphicon glyph="edit" /></Button>
        </td>
      </tr>
    )
  }
}

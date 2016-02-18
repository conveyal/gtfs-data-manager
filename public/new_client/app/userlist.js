import React from 'react'
import $ from 'jquery'

import { Panel, Grid, Row, Col, Button } from 'react-bootstrap'

import CreateUser from './createuser'
import UserSettings from './usersettings'
import PermissionData from './permissiondata'

import config from './config'

import styles from './style.css'

export default class UserList extends React.Component {

  constructor (props) {
    super(props)

    this.state = {
      users: []
    }
  }

  componentDidMount () {
    this.fetchUsers()
  }

  fetchUsers () {
    $.ajax({
      url : '/secured/getUsers',
      headers: {
        'Authorization': 'Bearer ' + this.props.token
      }
    }).done((data) => {
      var users = JSON.parse(data).map((user) => {
        user.permissionData = new PermissionData(user.app_metadata ? user.app_metadata.datatools : null)
        return user
      })

      this.setState({
        users: users
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
            <h2>User Management</h2>
          </Col>
        </Row>

        <Row>
          <Col xs={8}>
            <h3>All Users</h3>
          </Col>
          <Col xs={4}>
            <CreateUser
              createUser={this.createUser.bind(this)}
              feeds={this.props.feeds}
            />
          </Col>
        </Row>

        {this.state.users.filter((user) => {
          return user.permissionData.hasProject(config.projectID)
        }).map((user, i) => {
          return <UserRow
            feeds={this.props.feeds}
            user={user}
            key={i}
            updateUser={this.updateUser.bind(this)}
          />
        })}

      </Grid>
    )
  }
}

class UserRow extends React.Component {

  constructor (props) {
    super(props)
    this.state = {
      isEditing : false
    }
  }

  toggleExpansion () {
    if (this.state.isEditing) {
      this.save()
    }

    this.setState({
      isEditing : !this.state.isEditing
    })
  }

  save () {
    console.log('saving ', this.props.user)
    this.props.updateUser(this.props.user, this.refs.userSettings.getSettings())
  }

  render () {
    var projectPermissions = this.props.user.permissionData.getProjectPermissions(config.projectID)
    return (
      <Panel>
        <Row>
          <Col xs={8}>
            <h4>{this.props.user.email}</h4>
          </Col>
          <Col xs={4}>
            <Button className='pull-right' onClick={this.toggleExpansion.bind(this)}>
              { this.state.isEditing ? 'Save' : 'Edit'}
            </Button>
          </Col>
        </Row>
        { this.state.isEditing ? (
          <UserSettings ref="userSettings"
            feeds={this.props.feeds}
            userPermissions={projectPermissions}
          />
        ) : null }
      </Panel>
    )
  }
}

import React from 'react'

import { Button, Modal, Input } from 'react-bootstrap'

import UserSettings from './usersettings'

export default class CreateUser extends React.Component {

  constructor (props) {
    super(props)
    this.state = {
      showModal: false
    }
  }

  close () {
    this.setState({
      showModal: false
    })
    this.props.createUser(this.refs.email.getValue(), this.refs.password.getValue(), this.refs.userSettings.getSettings())
  }

  open () {
    console.log('opening..')
    this.setState({
      showModal: true
    })
  }

  render () {

    return (
      <div>
        <Button
          bsStyle="primary"
          bsSize="large"
          onClick={this.open.bind(this)}
          className="pull-right"
        >
          Create User
        </Button>

        <Modal show={this.state.showModal} onHide={this.close.bind(this)}>
          <Modal.Header closeButton>
            <Modal.Title>Create User</Modal.Title>
          </Modal.Header>
          <Modal.Body>
            <Input ref='email' type="email" label="Email Address" placeholder="Enter email" />
            <Input ref='password' type="password" label="Password" />
            <UserSettings
              feeds={this.props.feeds}
              ref="userSettings"
            />
          </Modal.Body>
          <Modal.Footer>
            <Button onClick={this.close.bind(this)}>Create User</Button>
          </Modal.Footer>
        </Modal>
      </div>
    )
  }
}

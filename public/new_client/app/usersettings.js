import React from 'react'
import { Row, Col, Input } from 'react-bootstrap'

import allPermissions from './permissions'

export default class UserSettings extends React.Component {

  getSettings () {
    var selectedFeeds = this.props.feeds.filter((feed) => {
      let checkbox = this.refs['feed-' + feed.id]
      return checkbox.getChecked()
    }).map((feed) => {
      return feed.id
    })

    var userPermissions = allPermissions.filter((permissionInfo) => {
      let checkbox = this.refs['permission-' + permissionInfo.type]
      return checkbox.getChecked()
    }).map((permissionInfo) => {
      var permission = {
        type : permissionInfo.type,
      }
      if(permissionInfo.feedSpecific) permission.feeds = selectedFeeds
      return permission
    })

    userPermissions.push({
      type: 'view-feed',
      feeds : selectedFeeds
    })

    return userPermissions
  }

  render () {
    var lookup = {}
    console.log('rendering userSettings', this.props.userPermissions)
    if (this.props.userPermissions) {
      for (var permission of this.props.userPermissions) {
        lookup[permission.type] = permission.feeds
      }
    }

    return (
      <Row>
        <Col xs={6}>
          <h4>Agencies</h4>
          {this.props.feeds.map(function(feed, i){
            let name = (feed.name === '') ? '(unnamed feed)' : feed.name
            let ref = 'feed-' + feed.id
            let feedContained = lookup['view-feed'] && lookup['view-feed'].indexOf(feed.id) !== -1
            let checked = feedContained ? 'checked' : ''
            return <Input ref={ref} type="checkbox" defaultChecked={checked} label={name} />;
          })}
        </Col>
        <Col xs={6}>
          <h4>Permissions</h4>
          {allPermissions.map(function(permission, i){
            let ref = 'permission-' + permission.type
            let checked = permission.type in lookup
            return <Input ref={ref} type="checkbox" defaultChecked={checked} label={permission.name} />;
          })}
        </Col>
      </Row>
    )
  }
}

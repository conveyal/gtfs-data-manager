import React from 'react'
import $ from 'jquery'
import moment from 'moment'

import { Link } from 'react-router'

import { Panel, Grid, Row, Col, Button, Table, Glyphicon } from 'react-bootstrap'

import {BootstrapTable, TableHeaderColumn} from 'react-bootstrap-table'

import CreateUser from './createuser'
import UserSettings from './usersettings'
import PermissionData from './permissiondata'

import config from './config'

import styles from './style.css'

import 'react_table_css'

export default class FeedList extends React.Component {

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
      // Hard coded feed collection id
      url : '/api/public/feedsources?feedcollection=' + config.projectID,
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
        <FeedTable
          feeds={this.state.feeds}
        />
      </Grid>
    )
  }
}

class OldFeedTable extends React.Component {
  render () {
    return (
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
    )
  }
}

class FeedTable extends React.Component {
  feedFormat(cell, row){
    return cell ? <Link to={'feed/' + row.id}>{cell}</Link> : ''
  }
  dateFormat(cell, row){
    return cell ? moment(cell).format('MMMM Do YYYY, h:mm a') : ''
  }
  urlFormat(cell, row){
    return cell ? <a href={cell}><Glyphicon glyph="new-window" /></a> : ''
  }
  dateSort(a, b, order){
    return b.lastUpdated - a.lastUpdated
  }
  regionFormat(cell, row){
    return 'San Francisco Bay Area'
  }
  stateFormat(cell, row){
    return 'California'
  }
  countryFormat(cell, row){
    return 'United States'
  }
  constructor (props) {
    super(props)
  }

  render () {
    return (
      <BootstrapTable 
        data={this.props.feeds} 
        pagination={true}
        striped={true} 
        hover={true}
        search={true}
      >
        <TableHeaderColumn isKey={true} dataSort={true} hidden={true} dataField="id">Feed ID</TableHeaderColumn>
        <TableHeaderColumn dataSort={true} dataField="name" dataFormat={this.feedFormat}>Feed Name</TableHeaderColumn>
        <TableHeaderColumn dataSort={true} dataFormat={this.regionFormat} dataField="region">Region</TableHeaderColumn>
        <TableHeaderColumn dataSort={true} dataFormat={this.stateFormat} dataField="state">State or Province</TableHeaderColumn>
        <TableHeaderColumn dataSort={true} dataFormat={this.countryFormat} dataField="country">Country</TableHeaderColumn>
        <TableHeaderColumn dataSort={true} dataField="lastUpdated" sortFunc={this.dateSort} dataFormat={this.dateFormat}>Last Updated</TableHeaderColumn>
        <TableHeaderColumn dataSort={true} dataField="lastUpdated" hidden={true}>last_update</TableHeaderColumn>
        <TableHeaderColumn dataField="url" dataFormat={this.urlFormat}>Link to GTFS</TableHeaderColumn>
      </BootstrapTable>
    )
  }
}

class FeedRow extends React.Component {
  
  constructor (props) {
    super(props)
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
